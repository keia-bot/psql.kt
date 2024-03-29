package one.keia.oss.psql.protocol.types

public data object QChar : DataType<Char>(18, Codec.Char) {
    public data object Array : DataType.Array<Char>(1002, QChar)
}

public data object Name : DataType<String>(19, Codec.Text) {
    public data object Array : DataType.Array<String>(1003, Name)
}

public data object Text : DataType<String>(25, Codec.Text) {
    public data object Array : DataType.Array<String>(1009, Text)
}

public data object BPChar : DataType<Unit>(1042, Codec.todo()) {
    public data object Array : DataType.Array<Unit>(1014, BPChar)
}

public data object Varchar : DataType<String>(1043, Codec.Text) {
    public data object Array : DataType.Array<String>(1015, Varchar)
}