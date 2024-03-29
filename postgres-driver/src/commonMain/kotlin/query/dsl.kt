package one.keia.oss.psql.driver.query

import naibu.common.generateUniqueId
import one.keia.oss.psql.driver.Row
import one.keia.oss.psql.driver.connection.Connection
import one.keia.oss.psql.protocol.FormatCode
import one.keia.oss.psql.protocol.Message
import one.keia.oss.psql.protocol.StatementType
import one.keia.oss.psql.protocol.types.DataType

public suspend fun Connection.prepare(query: String, types: List<DataType<*>> = emptyList()): PreparedStatement {
    val id = generateUniqueId()
    send0(
        listOf(
        Message.Frontend.Parse(id, query, types),
        Message.Frontend.Describe(StatementType.Prepared, id),
        Message.Frontend.Sync
    )
    )

    read0<Message.Backend.ParseComplete>()

    val statement = PreparedStatement(
        this,
        id,
        read0<Message.Backend.ParameterDescription>(),
        read0<Message.Backend.RowDescription>()
    )

    readTillReady()
    return statement
}

public suspend fun Connection.execute(
    query: String,
    vararg arguments: Any,
    limit: Int = 0,
    types: List<DataType<*>> = emptyList(),
    format: FormatCode = FormatCode.Text,
): List<Row> = prepare(query, types).execute(*arguments, limit = limit, close = true, format = format)
