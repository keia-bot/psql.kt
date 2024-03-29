package one.keia.oss.psql.protocol.types

public data object Int2 : DataType<Short>(21, Codec.Int16) {
    public data object Array : DataType.Array<Short>(1005, Int2)
}

public data object Int4 : DataType<Int>(23, Codec.Int32) {
    public data object Array : DataType.Array<Int>(1007, Int4)
}

public data object Int8 : DataType<Long>(20, Codec.Int64) {
    public data object Array : DataType.Array<Long>(1016, Int8)
}

public data object Float4 : DataType<Float>(700, Codec.todo()) {
    public data object Array : DataType.Array<Float>(1021, Float4)
}

public data object Float8 : DataType<Double>(701, Codec.todo()) {
    public data object Array : DataType.Array<Double>(1022, Float8)
}

public data object Bit : DataType<Unit>(1560, Codec.todo()) {
    public data object Array : DataType.Array<Unit>(1561, Bit)
}

public data object VarBit : DataType<Unit>(1562, Codec.todo()) {
    public data object Array : DataType.Array<Unit>(1563, VarBit)
}
