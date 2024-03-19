package one.keia.oss.psql.driver

public sealed interface Credentials {
    public val username: String

    /**
     * Basic `username` and `password` credentials.
     */
    public data class Basic(override val username: String, val password: String?) : Credentials
}
