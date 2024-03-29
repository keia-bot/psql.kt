package one.keia.oss.psql.driver.query

import io.ktor.utils.io.core.*
import naibu.ext.into
import one.keia.oss.psql.driver.Row
import one.keia.oss.psql.driver.connection.Connection
import one.keia.oss.psql.protocol.FormatCode
import one.keia.oss.psql.protocol.Message
import one.keia.oss.psql.protocol.StatementType
import one.keia.oss.psql.protocol.types.Codec

public data class PreparedStatement(
    val connection: Connection,
    val id: String,
    val parameters: Message.Backend.ParameterDescription,
    val description: Message.Backend.RowDescription,
) {
    public data class Parameterized(val statement: PreparedStatement, val arguments: List<Any>) {
        public suspend fun execute(
            limit: Int = 0,
            close: Boolean = false,
            format: FormatCode = FormatCode.Text,
        ): List<Row> = statement.execute(arguments, limit, close, format)
    }

    public fun parameterized(vararg arguments: Any): Parameterized = Parameterized(this, arguments.toList())

    public suspend fun execute(
        vararg arguments: Any,
        limit: Int = 0,
        close: Boolean = false,
        format: FormatCode = FormatCode.Text,
    ): List<Row> = execute(arguments.toList(), limit, close, format)

    public suspend fun execute(
        arguments: List<Any>,
        limit: Int = 0,
        close: Boolean = false,
        format: FormatCode = FormatCode.Text,
    ): List<Row> {
        require (parameters.objectIDs.size == arguments.size) {
            "expected ${parameters.objectIDs.size} arguments, got ${arguments.size}"
        }

        val parameters = parameters.objectIDs
            .mapIndexed { i, dt -> dt.codec.into<Codec<Any>>().encode(dt, format, arguments[i]) }
            .map { it.readBytes() }

        connection.send0(listOfNotNull(
            Message.Frontend.Bind("", id, listOf(format), parameters, listOf(format)),
            Message.Frontend.Execute("", limit),
            if (close) Message.Frontend.Close(StatementType.Portal, "") else null,
            Message.Frontend.Sync
        ))

        val rows = mutableListOf<Row>()
        while (true) when (val row = connection.read0()) {
            is Message.Backend.DataRow         -> rows += Row(row, description)
            is Message.Backend.BindComplete    -> {}
            is Message.Backend.CommandComplete -> break
            else                               -> throw IllegalStateException("unexpected message: $row")
        }

        connection.readTillReady()
        return rows
    }
}
