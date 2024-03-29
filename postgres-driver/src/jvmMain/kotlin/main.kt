package one.keia.oss.psql.driver

import io.ktor.utils.io.core.*
import kotlinx.coroutines.coroutineScope
import naibu.collections.SuspendIterator
import naibu.common.generateUniqueId
import naibu.ext.into
import naibu.ext.print
import one.keia.oss.psql.driver.connection.Connection
import one.keia.oss.psql.protocol.FormatCode
import one.keia.oss.psql.protocol.Message
import one.keia.oss.psql.protocol.StatementType
import one.keia.oss.psql.protocol.types.Codec
import one.keia.oss.psql.protocol.types.DataType

public class Row(
    private val inner: Message.Backend.DataRow,
    private val description: Message.Backend.RowDescription
) {
    public fun toMap(): Map<String, Any?> = description.fields.associate { it.name to get(it.name) }

    @JvmName("get0")
    public inline fun <reified T : Any> get(name: String): T? = get(name)?.into()

    public fun get(name: String): Any? = get(description.fields.indexOfFirst { it.name == name })

    public fun get(index: Int): Any? {
        val field = description.fields[index]
        val column = inner.columns[index]
        if (field.dataType is DataType.Companion.External) {
            return field.dataType to column
        }

        return column?.let { bytes -> field.dataType.codec.decode(field.dataType, field.formatCode, ByteReadPacket(bytes)) }
    }
}

public interface RowIterator : SuspendIterator<Row>

public data class PreparedStatement(
    val id: String,
    val parameters: Message.Backend.ParameterDescription,
    val description: Message.Backend.RowDescription,
)

public suspend fun Connection.prepare(query: String, types: List<DataType<*>> = emptyList()): PreparedStatement {
    val id = generateUniqueId()
    send0(listOf(
        Message.Frontend.Parse(id, query, types),
        Message.Frontend.Describe(StatementType.Prepared, id),
        Message.Frontend.Sync
    ))

    read0<Message.Backend.ParseComplete>()

    val statement = PreparedStatement(
        id,
        read0<Message.Backend.ParameterDescription>(),
        read0<Message.Backend.RowDescription>()
    )

    readTillReady()
    return statement
}

public suspend fun Connection.execute(
    query: String,
    limit: Int,
    vararg arguments: Any,
    types: List<DataType<*>> = emptyList(),
    format: FormatCode = FormatCode.Text,
): List<Row> {
    val statement = prepare(query, types)
    require (statement.parameters.objectIDs.size == arguments.size) {
        "expected ${statement.parameters.objectIDs.size} arguments, got ${arguments.size}"
    }

    //
    val parameters = statement.parameters.objectIDs
        .mapIndexed { i, dt -> dt.codec.into<Codec<Any>>().encode(dt, format, arguments[i]) }
        .map { it.readBytes() }

    send0(listOf(
        Message.Frontend.Bind("", statement.id, listOf(format), parameters, listOf(format)),
        Message.Frontend.Execute("", limit),
        Message.Frontend.Close(StatementType.Portal, ""),
        Message.Frontend.Sync
    ))

    read0<Message.Backend.BindComplete>()

    suspend fun row(): Row? = when (val row = read0()) {
        is Message.Backend.DataRow -> Row(row, statement.description)

        is Message.Backend.CommandComplete,
        is Message.Backend.PortalSuspended -> {
            readTillReady()
            null
        }

        else -> throw IllegalStateException("unexpected message: $row")
    }

    val rows = mutableListOf<Row>()
    var row: Row? = null
    do {
        row?.let { rows += it }
        row = row()
    } while (row != null)

    return rows
}

public suspend fun main(): Unit = coroutineScope {
    try {
        val connection = Connection("postgres://keia:keia@127.0.0.1/keia")

        connection.connect()
        connection.execute("SELECT * FROM track;", 1)
            .map { it.toMap() }
            .print()

        //
//        val select = connection.exec(QUERY)
//        val fields = select.firstOfInstance<Message.Backend.RowDescription>().fields
//        select.asSequence()
//            .filterIsInstance<Message.Backend.DataRow>()
//            .map { fields.zip(it.columns) }
//            .map { it.toMap() }
//            .map {
//                it.mapValues { (field, column) ->
//                    if (field.dataType is DataType.Companion.External) {
//                        field.dataType to column
//                    } else column?.let { bytes ->
//                        field.dataType.codec.decode(
//                            field.dataType,
//                            field.formatCode,
//                            ByteReadPacket(bytes)
//                        )
//                    }
//                }
//            }
//            .map { it.mapKeys { (field) -> field.name } }.toList()
//            .forEach { it.print() }

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
