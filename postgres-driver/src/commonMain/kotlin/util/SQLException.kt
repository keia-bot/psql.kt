package one.keia.oss.psql.driver.util

public expect open class SQLException : Exception {
    public constructor(message: String, sqlState: String?)
}
