package one.keia.oss.psql.driver

import kotlinx.coroutines.coroutineScope
import naibu.ext.print
import one.keia.oss.psql.driver.connection.Connection
import one.keia.oss.psql.driver.query.prepare
import one.keia.oss.psql.protocol.Message
import kotlin.time.measureTimedValue

public suspend fun commonMain(): Unit = coroutineScope {
    try {
        val connection = Connection("postgres://keia:keia@127.0.0.1/keia")
        val FIND_TRACK = connection
            .prepare("SELECT * FROM track WHERE isrc = $1;")
            .parameterized("QZFYX2403729")

        for (i in 0..5) {
            measureTimedValue {
                FIND_TRACK.execute()[0].toMap()
            }.let { it.duration to it.value }.print()
        }

        while (true) {
            connection.read0(true)
        }
    } catch (ex: Throwable) {
        ex.printStackTrace()
    }
}

public suspend fun Connection.exec(query: String): MutableList<Message.Backend> {
    send0(Message.Frontend.Query(query))

    val messages = mutableListOf<Message.Backend>()
    readTillReady { messages += it }

    return messages
}
