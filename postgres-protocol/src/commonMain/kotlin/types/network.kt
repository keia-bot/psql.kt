package one.keia.oss.psql.protocol.types

public data object CIDR : DataType<Unit>(650, Codec.todo()) {
    public data object Array : DataType.Array<Unit>(651, CIDR)
}

public data object MacAddress : DataType<Unit>(829, Codec.todo()) {
    public data object Array : DataType.Array<Unit>(1040, MacAddress)
}

public data object Inet : DataType<Unit>(869, Codec.todo()) {
    public data object Array : DataType.Array<Unit>(1041, Inet)
}
