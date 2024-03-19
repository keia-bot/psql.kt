package one.keia.oss.psql.driver.util

public actual open class SQLException actual constructor(message: String, sqlState: String?) : Exception(message)