package one.keia.oss.psql.driver.util

import one.keia.oss.psql.protocol.Message

public fun Message.Backend.ErrorResponse.format(): String {
    return errors.joinToString("\n") { "\t- ${it.type.name}: ${it.value}" }
}
