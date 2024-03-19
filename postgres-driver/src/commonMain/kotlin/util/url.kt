package one.keia.oss.psql.driver.util

import io.ktor.http.*
import io.ktor.network.sockets.*
import io.ktor.util.*
import one.keia.oss.psql.driver.Credentials

public val SUPPORTED_SCHEMES: Set<String> = setOf("postgres", "postgresql")

public data class PSQLUri(
    val database: String? = null,
    val credentials: Credentials.Basic? = null,
    val address: SocketAddress,
    val parameters: Map<String, String>
)

public fun parseURI(value: String): PSQLUri {
    val url = Url(value)
    require (SUPPORTED_SCHEMES.any { it.equals(url.protocol.name, true) }) {
        "Invalid protocol"
    }

    val username = url.user ?: url.parameters["user"]
    val password = url.password ?: url.parameters["password"]

    return PSQLUri(
        url.pathSegments.singleOrNull(),
        username?.let { Credentials.Basic(it, password) },
        InetSocketAddress(url.host, url.port.takeUnless { it == 0 } ?: 5432),
        url.parameters.flattenEntries().toMap()
    )
}
