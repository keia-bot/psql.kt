package one.keia.oss.psql.protocol.types

public data class TID(val blockNumber: Int, val offsetNumber: Int) {
    public companion object : DataType<TID>(27), Codec<TID> by Codec.None()
}
