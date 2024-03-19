package one.keia.oss.psql.driver.util

import one.keia.oss.psql.protocol.Message
import one.keia.oss.psql.protocol.MessageFieldType

public class PSQLException : SQLException {
    public constructor(message: String, sqlState: String? = null) : super(message, sqlState)

    public constructor(serverError: Message.Backend.ErrorResponse) : super(
        "Received error response:\n${serverError.format()}",
        serverError.errors.find { it.type == MessageFieldType.Code }?.value
    )
}