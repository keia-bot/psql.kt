package one.keia.oss.psql.driver.connection

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import naibu.ext.into
import one.keia.oss.psql.driver.util.PSQLException
import one.keia.oss.psql.protocol.Message
import one.keia.oss.psql.protocol.readPostgresMessage
import one.keia.oss.psql.protocol.writePostgresMessage
import kotlin.jvm.JvmName
import io.ktor.network.sockets.Connection as SocketConnection

public class Connection(public val resources: ConnectionResources) {
    private lateinit var connection: SocketConnection
    private val state = MutableStateFlow<State>(State.Idle)
    private val scope = CoroutineScope(resources.dispatcher + SupervisorJob() + CoroutineName("PgConnection"))

    public suspend fun connect() {
        require(state.value == State.Idle) {
            "connect() has already been called on this connection."
        }

        log.debug { "Creating connection to ${resources.address}" }

        connection = aSocket(SelectorManager(scope.coroutineContext))
            .tcp()
            .connect(resources.address, resources.socketConfigurator)
            .connection()

        // Send startup message
        connection.output.writePostgresMessage(startup(resources.credentials.username), false)
        connection.output.flush()

        // Authenticate
        auth(this)

        // finished
        readTillReady { }
    }

    public suspend fun detach() {
        require(state.value is State.Ready) {
            "detach() can only be called on a connected connection."
        }

        state.value = State.Detached
        connection.socket.close()

    }

    internal suspend fun readTillReady(block: suspend (Message.Backend) -> Unit = {}) {
        var msg: Message.Backend? = null
        do {
            msg?.let { block(it) }
            msg = read0(true)
        } while (msg !is Message.Backend.ReadyForQuery)
        state.value = State.Ready(msg.into<Message.Backend.ReadyForQuery>().status)
    }

    internal suspend fun send0(messages: List<Message.Frontend>, flush: Boolean = true) {
        messages.forEach { send0(it, false) }
        if (flush) connection.output.flush()
    }

    internal suspend fun send0(message: Message.Frontend, flush: Boolean = true) {
        log.debug { "<<< $message" }
        connection.output.writePostgresMessage(message)
        if (flush) connection.output.flush()
    }

    internal suspend fun read0(handleErrors: Boolean = false): Message.Backend {
//        connection.input.awaitContent()

        val msg = connection.input.readPostgresMessage()
                  ?: TODO("close connection when an unknown message has been read")

        log.debug { ">>> $msg" }
        if (handleErrors && msg is Message.Backend.ErrorResponse) {
            detach()
            throw PSQLException(msg)
        }

        return msg
    }

    @JvmName("read1")
    internal suspend inline fun <reified T : Message> read0(handleErrors: Boolean = false): T {
        val message = read0(handleErrors)
        require(message is T) {
            "expected ${T::class.simpleName}, got ${message::class.simpleName}"
        }

        return message
    }

    public sealed interface State {
        public data object Idle : State

        public data object Detached : State

        public data class Ready(val status: Message.Backend.ReadyForQuery.Status) : State
    }

    public companion object {
        private val log = KotlinLogging.logger { }
        private const val PROTOCOL_VERSION = 196608

        private fun startup(username: String) = Message.Frontend.StartupMessage(
            PROTOCOL_VERSION,
            mapOf(Message.Frontend.StartupMessage.Parameter.User to username)
        )
    }
}