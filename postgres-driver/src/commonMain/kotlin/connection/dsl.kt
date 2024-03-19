package one.keia.oss.psql.driver.connection

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
public inline fun Connection(block: ConnectionResources.Builder.() -> Unit): Connection {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return ConnectionResources.Builder()
        .apply(block)
        .build()
        .let(::Connection)
}

public inline fun Connection(
    uri: String,
    block: ConnectionResources.Builder.() -> Unit = {},
): Connection = Connection {
    url(uri)
    block()
}
