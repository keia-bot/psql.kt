package one.keia.oss.psql.driver

import io.ktor.utils.io.core.*
import kotlinx.coroutines.coroutineScope
import naibu.ext.collections.firstOfInstance
import naibu.ext.print
import one.keia.oss.psql.driver.connection.Connection
import one.keia.oss.psql.protocol.Message

public suspend fun main(): Unit = coroutineScope {
    try {
        val connection = Connection("postgres://test:test@127.0.0.1/test")

        connection.connect()
//        connection.exec("CREATE TABLE IF NOT EXISTS test (thing text not null, a int);")
//        connection.exec("ALTER TABLE test ADD a int;")
//        connection.exec("INSERT INTO test (thing, a) VALUES ('hi', 42069);")
        val select = connection.exec("SELECT * FROM test;")

        //
        val fields = select.firstOfInstance<Message.Backend.RowDescription>().fields
        select.asSequence()
            .filterIsInstance<Message.Backend.DataRow>()
            .map { fields.zip(it.columns) }
            .map { it.toMap() }
            .map {
                it.mapValues { (field, column) ->
                    column?.let { bytes ->
                        field.dataType.decode(
                            field.formatCode,
                            ByteReadPacket(bytes)
                        )
                    }
                }
            }
            .map { it.mapKeys { (field) -> field.name } }.toList()
            .forEach { it.print() }

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
