package one.keia.oss.psql.driver.auth

import one.keia.oss.psql.driver.util.hmac
import one.keia.oss.psql.driver.util.pbkdf2HmacSHA256DeriveKey
import one.keia.oss.psql.driver.util.sha256
import org.kotlincrypto.SecureRandom
import kotlin.experimental.xor
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.properties.Delegates

// Credit to jackc/pgx, it was a big help in figuring this out
// https://github.com/jackc/pgx/blob/master/pgconn/auth_scram.go

public object SCRAM {
    private val CLIENT_KEY = "Client Key".encodeToByteArray()
    private val SERVER_KEY = "Server Key".encodeToByteArray()
    private val RANDOM = SecureRandom()

    /** The SASL mechanism for this SCRAM implementation */
    public const val SHA_256: String = "SCRAM-SHA-256"

    /** */
    public const val PBKDF2_KEY_LENGTH: Int = 32

    /** The length of the SCRAM client nonce */
    public const val CLIENT_NONCE_LENGTH: Int = 18

    /**
     * Generates a random nonce to use in the SCRAM client-first-message.
     */
    @OptIn(ExperimentalEncodingApi::class)
    public fun generateClientNonce(): String =
        Base64.encode(RANDOM.nextBytesOf(CLIENT_NONCE_LENGTH))

    public data class Client(val nonce: String, val password: String) {
        // `server-first-message` data
        private var receivedServerFirstMessage = false
        private lateinit var serverFirstMessage: String
        private lateinit var clientAndServerNonce: String
        private lateinit var salt: ByteArray
        private var iterations: Int by Delegates.notNull()

        // `client-final-message` data
        private lateinit var saltedPassword: ByteArray

        //
        private val firstMessageBare
            get() = "n=,r=$nonce"
        private val clientFinalMessageWithoutProof
            get() = "c=biws,r=$clientAndServerNonce"
        private val authMessage
            get() = "$firstMessageBare,$serverFirstMessage,$clientFinalMessageWithoutProof".encodeToByteArray()

        /**
         * The first message sent by the client.
         */
        public fun createClientFirstMessage(): ByteArray {
            return "n,,$firstMessageBare".encodeToByteArray()
        }

        /**
         * Create the client-final-message.
         */
        @OptIn(ExperimentalEncodingApi::class)
        public fun createClientFinalMessage(): ByteArray {
            require(receivedServerFirstMessage) {
                "Cannot create client-final-message before receiving server-first-message"
            }

            saltedPassword =
                pbkdf2HmacSHA256DeriveKey(password.encodeToByteArray(), salt, iterations, PBKDF2_KEY_LENGTH)

            val clientProof = run {
                val clientKey = hmac(saltedPassword, CLIENT_KEY)
                val clientSig = hmac(sha256(clientKey), authMessage)

                val clientProof = ByteArray(clientSig.size)
                for (i in clientProof.indices) {
                    clientProof[i] = clientKey[i] xor clientSig[i]
                }

                Base64.encode(clientProof)
            }

            return "$clientFinalMessageWithoutProof,p=$clientProof".encodeToByteArray()
        }

        /**
         * Handle the SCRAM server-first-message.
         */
        public fun handleServerFirstMessage(data: ByteArray) {
            serverFirstMessage = data.decodeToString()

            val sfm = ServerFirstMessage.parse(serverFirstMessage)
            require(sfm.nonce.startsWith(nonce)) {
                "Invalid SCRAM server-first-message: nonce does not start with client nonce"
            }

            clientAndServerNonce = sfm.nonce
            iterations = sfm.iterations
            salt = sfm.salt
            receivedServerFirstMessage = true
        }

        /**
         * Handle the SCRAM server-final-message.
         */
        @OptIn(ExperimentalEncodingApi::class)
        public fun handleServerFinalMessage(data: ByteArray) {
            val message = data.decodeToString()
            require(message.startsWith("v=")) {
                "Invalid SCRAM server-final-message: missing verifier"
            }

            val serverSignature = run {
                val serverKey = hmac(saltedPassword, SERVER_KEY)
                val serverSig = hmac(serverKey, authMessage)
                Base64.encode(serverSig)
            }

            require(serverSignature == message.drop(2)) {
                "Invalid SCRAM server-final-message: server signature isn't valid"
            }
        }
    }

    private data class ServerFirstMessage(val nonce: String, val salt: ByteArray, val iterations: Int) {
        companion object {
            @OptIn(ExperimentalEncodingApi::class)
            fun parse(value: String): ServerFirstMessage {
                val attributes = value
                    .split(',')
                    .map { it.split('=', limit = 2) }
                    .associate { (k, v) -> k[0] to v }

                return ServerFirstMessage(
                    attributes['r']
                    ?: error("Invalid SCRAM server-first-message: missing nonce"),
                    attributes['s']?.let { Base64.decode(it) }
                    ?: error("Invalid SCRAM server-first-message: missing or invalid salt"),
                    attributes['i']?.toIntOrNull()
                    ?: error("Invalid SCRAM server-first-message: missing or invalid iterations"),
                )
            }
        }
    }
}
