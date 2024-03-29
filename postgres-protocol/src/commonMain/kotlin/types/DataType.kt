package one.keia.oss.psql.protocol.types

public sealed class DataType<T>(
    public val id: Int,
    public val codec: Codec<T>
) {
    public sealed class Array<T>(id: Int, public val element: DataType<T>) : DataType<List<T?>>(id, Codec.Array(element.codec))

    public data object Bool : DataType<Boolean>(16, Codec.Bool) {
        public data object Array : DataType.Array<Boolean>(1000, Bool)
    }

    public data object Unknown : DataType<Unit>(705, Codec.todo())

    public data object Bytea : DataType<ByteArray>(17, Codec.todo()) // byte array
    public data object ByteaArray : DataType<Unit>(1001, Codec.todo())

    public data object ACLItem : DataType<Unit>(1033, Codec.todo())
    public data object ACLItemArray : DataType<Unit>(1034, Codec.todo())

    public data object Timestamp : DataType<Unit>(1114, Codec.todo())
    public data object TimestampArray : DataType<Unit>(1115, Codec.todo())

    public data object Date : DataType<Unit>(1082, Codec.todo())
    public data object DateArray : DataType<Unit>(1182, Codec.todo())

    public data object Time : DataType<Unit>(1083, Codec.todo())
    public data object TimeArray : DataType<Unit>(1183, Codec.todo())

    public data object Timestamptz : DataType<Unit>(1184, Codec.todo())
    public data object TimestamptzArray : DataType<Unit>(1185, Codec.todo())

    public data object Interval : DataType<Unit>(1186, Codec.todo())
    public data object IntervalArray : DataType<Unit>(1187, Codec.todo())

    public data object Numeric : DataType<Unit>(1700, Codec.todo())
    public data object NumericArray : DataType<Unit>(1231, Codec.todo())

    public data object Timetz : DataType<Unit>(1266, Codec.todo())
    public data object TimetzArray : DataType<Unit>(1270, Codec.todo())

    public data object Record : DataType<Unit>(2249, Codec.todo())
    public data object RecordArray : DataType<Unit>(2287, Codec.todo())

    public data object UUID : DataType<Unit>(2950, Codec.todo())
    public data object UUIDArray : DataType<Unit>(2951, Codec.todo())

    public data object Daterange : DataType<Unit>(3912, Codec.todo())
    public data object DaterangeArray : DataType<Unit>(3913, Codec.todo())

    public data object Int4range : DataType<Unit>(3904, Codec.todo())
    public data object Int4rangeArray : DataType<Unit>(3905, Codec.todo())

    public data object Numrange : DataType<Unit>(3906, Codec.todo())
    public data object NumrangeArray : DataType<Unit>(3907, Codec.todo())

    public data object Tsrange : DataType<Unit>(3908, Codec.todo())
    public data object TsrangeArray : DataType<Unit>(3909, Codec.todo())

    public data object Tstzrange : DataType<Unit>(3910, Codec.todo())
    public data object TstzrangeArray : DataType<Unit>(3911, Codec.todo())

    public data object Int8range : DataType<Unit>(3926, Codec.todo())
    public data object Int8rangeArray : DataType<Unit>(3927, Codec.todo())

    public data object JSONPath : DataType<Unit>(4072, Codec.todo())
    public data object JSONPathArray : DataType<Unit>(4073, Codec.todo())

    public data object Int4MultiRange : DataType<Unit>(4451, Codec.todo())
    public data object Int4MultiRangeArray : DataType<Unit>(6150, Codec.todo())

    public data object NumMultiRange : DataType<Unit>(4532, Codec.todo())
    public data object NumMultiRangeArray : DataType<Unit>(6151, Codec.todo())

    public data object TsMultiRange : DataType<Unit>(4533, Codec.todo())
    public data object TsMultiRangeArray : DataType<Unit>(6152, Codec.todo())

    public data object TstzMultiRange : DataType<Unit>(4534, Codec.todo())
    public data object TstzMultiRangeArray : DataType<Unit>(6153, Codec.todo())

    public data object DateMultiRange : DataType<Unit>(4535, Codec.todo())
    public data object DateMultiRangeArray : DataType<Unit>(6155, Codec.todo())

    public data object Int8MultiRange : DataType<Unit>(4536, Codec.todo())
    public data object Int8MultiRangeArray : DataType<Unit>(6157, Codec.todo())

    public companion object {
        public val entries: Set<DataType<*>> = setOf(
            //
            Bytea,
            ByteaArray,

            // numbers
            Int2,
            Int2.Array,

            Int4,
            Int4.Array,

            Int8,
            Int8.Array,

            Float4,
            Float4.Array,

            Float8,
            Float8.Array,

            //
            Bool,
            Bool.Array,

            // characters
            Text,
            Text.Array,

            Name,
            Name.Array,

            QChar,
            QChar.Array,

            BPChar,
            BPChar.Array,

            Varchar,
            Varchar.Array,

            //
            JSON,
            JSON.Array,

            JSONB,
            JSONB.Array,

            // ids
            OID,
            OID.Array,

            TID,
            TID.Array,

            XID,
            XID.Array,

            CID,
            CID.Array,

            // geometric
            Point,
            Point.Array,

            LineSegment,
            LineSegment.Array,

            Path,
            Path.Array,

            Box,
            Box.Array,

            Polygon,
            Polygon.Array,

            Circle,
            Circle.Array,

            //
            Line,
            Line.Array,

            //
            CIDR,
            CIDR.Array,

            Inet,
            Inet.Array,

            MacAddress,
            MacAddress.Array,

            //
            ACLItem,
            ACLItemArray,

            //
            Bit,
            Bit.Array,

            VarBit,
            VarBit.Array,

            Record,
            RecordArray,

            UUID,
            UUIDArray,

            Daterange,
            DaterangeArray,

            Int4range,
            Int4rangeArray,

            Numrange,
            NumrangeArray,

            JSONPath,
            JSONPathArray,
        )

        public class External(id: Int) : DataType<Unit>(id, Codec.todo()) {
            override fun toString(): String = "External($id)"
        }

        public fun fromCode(id: Int): DataType<*> =
            entries.find { it.id == id } ?: External(id)
    }
}
