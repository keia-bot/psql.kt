package one.keia.oss.psql.protocol

public enum class MessageFieldType(public val code: Char) {
    Severity             ('S'),
    SeverityNonLocalized ('V'),
    Code                 ('C'),
    Message              ('M'),
    Detail               ('D'),
    Hint                 ('H'),
    Position             ('P'),
    InternalPosition     ('q'),
    Where                ('W'),
    SchemaName           ('s'),
    TableName            ('t'),
    ColumnName           ('c'),
    DataTypeName         ('d'),
    ConstraintName       ('n'),
    File                 ('F'),
    Line                 ('L'),
    Routine              ('R'),

    Unknown              ('?')
    ;

    public companion object {
        public fun fromCode(code: Char): MessageFieldType? = entries.find { it.code == code }
    }
}