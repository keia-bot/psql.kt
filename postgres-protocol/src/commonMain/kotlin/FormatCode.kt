package one.keia.oss.psql.protocol

/**
 * The format code used to identify the format of a field or parameter.
 *
 * - See [Formats and Format Codes](https://www.postgresql.org/docs/current/protocol-overview.html#PROTOCOL-FORMAT-CODES)
 */
public enum class FormatCode(public val code: Short) {
    Text   (0),
    Binary (1);

    public companion object {
        public fun fromCode(code: Short): FormatCode = entries.first { it.code == code }
    }
}