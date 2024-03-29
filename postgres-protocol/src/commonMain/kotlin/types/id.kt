package one.keia.oss.psql.protocol.types

public data class TID(val blockNumber: Int, val offsetNumber: Int) {
    public companion object : DataType<TID>(27, Codec.todo())

    public data object Array : DataType.Array<TID>(1010, TID)
}

public data object OID : DataType<UInt>(26, Codec.UInt32) {
    public data object Array : DataType.Array<UInt>(1028, OID)
}

public data object XID : DataType<UInt>(28, Codec.UInt32) {
    public data object Array : DataType.Array<UInt>(1011, XID)
}

public data object CID : DataType<UInt>(29, Codec.UInt32) {
    public data object Array : DataType.Array<UInt>(1012, CID)
}
