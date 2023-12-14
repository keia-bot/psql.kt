package one.keia.oss.psql.driver

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.util.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.coroutineScope
import one.keia.oss.psql.protocol.Message
import one.keia.oss.psql.protocol.readPostgresMessage
import one.keia.oss.psql.protocol.writePostgresMessage
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Mac
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.xor
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.random.Random

public const val SCRAM_SHA_256: String = "SCRAM-SHA-256"
private val SR = SecureRandom.getInstanceStrong()

/** The length of the SCRAM client nonce */
public const val SCRAM_CLIENT_NONCE_LENGTH: Int = 18

@OptIn(ExperimentalEncodingApi::class)
public fun generateSCRAMNonce(): String =
    Base64.encode(Random.nextBytes(SCRAM_CLIENT_NONCE_LENGTH))

public const val TESTING_USERNAME: String = "postgres"
public const val TESTING_PASSWORD: String = "postgres"

private const val HMAC_ALGORITHM = "HmacSHA256"

private fun hmac(key: ByteArray, value: ByteArray): ByteArray {
    val hmac = Mac.getInstance(HMAC_ALGORITHM)
    hmac.init(SecretKeySpec(key, HMAC_ALGORITHM))

    return hmac.doFinal(value)
}

@OptIn(ExperimentalEncodingApi::class)
public suspend fun main(): Unit = coroutineScope {
    try {
        val connection = aSocket(ActorSelectorManager(coroutineContext))
            .tcp()
            .connect(InetSocketAddress("127.0.0.1", 5431))
            .connection()

        connection.output.writePostgresMessage(
            Message.Frontend.StartupMessage(
                196608,
                mapOf(Message.Frontend.StartupMessage.Parameter.User to "postgres")
            ),
            false
        )
        connection.output.flush()

        //
        when (val auth = connection.input.expectPostgresMessage<Message.Backend.Authentication>()) {
            is Message.Backend.Authentication.SASL -> {
                require(SCRAM_SHA_256 in auth.mechanisms) {
                    "server does not support $SCRAM_SHA_256"
                }

                val clientNonce = generateSCRAMNonce()

                //
                val clientFirstMessageBare = "n=$TESTING_USERNAME,r=$clientNonce"
                connection.output.writePostgresMessage(
                    Message.Frontend.SASLInitialResponse(
                        SCRAM_SHA_256,
                        "n,,$clientFirstMessageBare".encodeToByteArray()
                    )
                )
                connection.output.flush()

                //
                val serverFirstMessage =
                    connection.input.expectPostgresMessage<Message.Backend.Authentication.SASLContinue>()
                        ?.data
                    ?: error("expected SASLContinue")

                val reply = SCRAM.ServerFirstMessage.parse(serverFirstMessage.decodeToString())
                require(reply.nonce.startsWith(clientNonce)) {
                    "server nonce does not start with client nonce"
                }

                val clientFinalMessageWithoutProof = "c=biws,r=${reply.nonce}"

                val saltedPassword = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(PBEKeySpec(TESTING_PASSWORD.toCharArray(), reply.salt, reply.iterations, 32 * 8))
                    .encoded

                val authMessage = buildPacket {
                    writeText(clientFirstMessageBare)
                    writeByte(','.code.toByte())
                    writeFully(serverFirstMessage)
                    writeByte(','.code.toByte())
                    writeText(clientFinalMessageWithoutProof)
                }.readBytes()

                // client proof
                val clientKey = hmac(saltedPassword, "Client Key".encodeToByteArray())
                val storedKey = MessageDigest.getInstance("SHA-256").digest(clientKey)
                val clientSignature = hmac(storedKey, authMessage)

                val clientProof = ByteArray(clientSignature.size)
                for (i in clientProof.indices) {
                    clientProof[i] = clientKey[i] xor clientSignature[i]
                }

                val b64EncodedClientProof = Base64.encode(clientProof)

                //
                connection.output.writePostgresMessage(
                    Message.Frontend.SASLResponse(
                        "$clientFinalMessageWithoutProof,p=$b64EncodedClientProof".encodeToByteArray()
                    )
                )
                connection.output.flush()

                //
                val serverFinalMessage =
                    connection.input.expectPostgresMessage<Message.Backend.Authentication.SASLFinal>()
                        ?.data
                    ?: error("expected SASLFinal")

                serverFinalMessage.decodeToString()
            }

            else                                   -> error("unsupported authorization method: $auth")
        }

        var ready = false
        while (true) {
            connection.input.awaitContent()

            val message = connection.input.readPostgresMessage()
                          ?: continue

            println(message)

            if (!ready && message is Message.Backend.ReadyForQuery) {
                ready = true
                connection.output.writePostgresMessage(Message.Frontend.Query("SELECT 1;"))
                connection.output.flush()
                continue
            }
        }
    } catch (ex: Throwable) {
        ex.printStackTrace()
    }
}

public suspend inline fun <reified T : Message> ByteReadChannel.expectPostgresMessage(): T? {
    val message = readPostgresMessage()
                  ?: return null

    require(message is T) {
        "expected ${T::class.simpleName}, got ${message::class.simpleName}"
    }

    return message
}

public sealed interface SCRAM {
    public fun format(): String

    public data class ServerFirstMessage(
        val nonce: String,
        val salt: ByteArray,
        val iterations: Int,
        val extensions: Map<Char, String>,
    ) {
        public companion object {
            private val NON_EXTENSION_ATTRIBUTES = setOf('r', 's', 'i')

            public fun parse(value: String): ServerFirstMessage {
                val attributes = value
                    .split(',')
                    .map { it.split('=', limit = 2) }
                    .associate { (k, v) -> k[0] to v }

                return ServerFirstMessage(
                    attributes['r'] ?: error("missing nonce"),
                    attributes['s']
                        ?.decodeBase64Bytes()
                    ?: error("missing salt"),
                    attributes['i']?.toInt() ?: error("missing iterations"),
                    attributes.filterKeys { it !in NON_EXTENSION_ATTRIBUTES })
            }
        }
    }

    public data class ClientFirstMessage(
        val gs2: Gs2Header,
        val bare: Bare,
    ) : SCRAM {
        public override fun format(): String {
            return "${gs2.format()}${bare.format()}"
        }

        public data class Gs2Header(val channelBindFlag: ChannelBindingFlag, val authzid: String? = null) : SCRAM {
            override fun format(): String {
                return "${channelBindFlag.format()},${authzid?.let { "a=$it," } ?: ""},"
            }

            public sealed interface ChannelBindingFlag : SCRAM {
                /** Client does support channel binding but thinks the server does not. */
                public data object Yes : ChannelBindingFlag {
                    override fun format(): String = "y"
                }

                /** Client doesn't support channel binding. */
                public data object No : ChannelBindingFlag {
                    override fun format(): String = "n"
                }

                /** Client requires channel binding. */
                @JvmInline
                public value class Required(public val value: String) : ChannelBindingFlag {
                    override fun format(): String = "p=$value"
                }
            }
        }

        public data class Bare(
            val username: String,
            val nonce: String,
            val extensions: Map<String, String> = emptyMap(),
        ) : SCRAM {
            override fun format(): String =
                "n=$username,r=$nonce${extensions.entries.joinToString(",") { "${it.key}=${it.value}" }}"
        }
    }
}
