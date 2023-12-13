package one.keia.oss.psql.protocol

public enum class StatementType(public val code: Char) {
    Prepared('S'),
    Portal('P');

    public companion object {
        public fun fromCode(code: Char): StatementType = entries.first { it.code == code }
    }
}