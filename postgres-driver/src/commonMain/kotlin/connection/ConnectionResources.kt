package one.keia.oss.psql.driver.connection

import io.ktor.network.sockets.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import one.keia.oss.psql.driver.Credentials
import one.keia.oss.psql.driver.util.parseURI

public data class ConnectionResources(
    val dispatcher: CoroutineDispatcher,
    val database: String,
    val credentials: Credentials,
    val address: SocketAddress,
    val socketConfigurator: SocketOptions.TCPClientSocketOptions.() -> Unit = {},
) {
    public class Builder {
        public lateinit var database: String

        public lateinit var address: SocketAddress

        public lateinit var credentials: Credentials

        public var socketConfigurator: SocketOptions.TCPClientSocketOptions.() -> Unit = {}

        public var dispatcher: CoroutineDispatcher = Dispatchers.IO

        public fun url(value: String) {
            val uri = parseURI(value)
            database = uri.database ?: "postgres"
            credentials = uri.credentials ?: Credentials.Basic("postgres", "postgres")
            address = uri.address
        }

        public fun configureSocket(block: SocketOptions.TCPClientSocketOptions.() -> Unit)  {
            socketConfigurator = block
        }

        public fun build(): ConnectionResources = ConnectionResources(
            dispatcher, database, credentials, address, socketConfigurator
        )
    }
}
