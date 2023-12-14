package one.keia.oss.psql.driver

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.coroutineScope
import one.keia.oss.psql.driver.auth.SCRAM
import one.keia.oss.psql.protocol.Message
import one.keia.oss.psql.protocol.readPostgresMessage
import one.keia.oss.psql.protocol.writePostgresMessage
import kotlin.io.encoding.ExperimentalEncodingApi

public const val TESTING_USERNAME: String = "postgres"
public const val TESTING_PASSWORD: String = "postgres"

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
                require(SCRAM.SHA_256 in auth.mechanisms) {
                    "server does not support $SCRAM.SHA_256"
                }

                val client = SCRAM.Client(SCRAM.generateClientNonce(), TESTING_PASSWORD)

                //
                connection.output.writePostgresMessage(Message.Frontend.SASLInitialResponse(SCRAM.SHA_256, client.createClientFirstMessage()))
                connection.output.flush()

                //
                val serverFirstMessage = connection.input.expectPostgresMessage<Message.Backend.Authentication.SASLContinue>()
                    ?.data
                    ?: error("expected SASLContinue")

                client.handleServerFirstMessage(serverFirstMessage)

                //
                connection.output.writePostgresMessage(Message.Frontend.SASLResponse(client.createClientFinalMessage()))
                connection.output.flush()

                //
                val serverFinalMessage = connection.input.expectPostgresMessage<Message.Backend.Authentication.SASLFinal>()
                    ?.data
                    ?: error("expected SASLFinal")

                client.handleServerFinalMessage(serverFinalMessage)
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

