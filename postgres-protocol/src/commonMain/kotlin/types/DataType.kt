package one.keia.oss.psql.protocol.types

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement

public sealed class DataType<T>(public val id: Int) : Codec<T> {
    public data object Bool : DataType<Boolean>(16), Codec<Boolean> by Codec.None() // boolean

    public data object Bytea : DataType<ByteArray>(17), Codec<ByteArray> by Codec.None() // byte array

    public data object QChar : DataType<Char>(18), Codec<Char> by Codec.Char

    public data object Name : DataType<String>(19), Codec<String> by Codec.Text

    public data object Int8 : DataType<Long>(20), Codec<Long> by Codec.Int64

    public data object Int2 : DataType<Short>(21), Codec<Short> by Codec.Int16

    public data object Int4 : DataType<Int>(23), Codec<Int> by Codec.Int32

    public data object Text : DataType<String>(25), Codec<String> by Codec.Text

    public data object OID : DataType<UInt>(26), Codec<UInt> by Codec.UInt32

    public data object XID : DataType<UInt>(28), Codec<UInt> by Codec.UInt32

    public data object CID : DataType<UInt>(29), Codec<UInt> by Codec.UInt32

    public data object JSON : DataType<JsonElement>(114), Codec<JsonElement> by Codec.None()

    public data object JSONArray : DataType<JsonArray>(199), Codec<JsonArray> by Codec.None()

    public data object Box : DataType<Unit>(603), Codec<Unit> by Codec.None()

    public data object Polygon : DataType<Unit>(604), Codec<Unit> by Codec.None()

    public data object Line : DataType<Unit>(628), Codec<Unit> by Codec.None()

    public data object LineArray : DataType<Unit>(629), Codec<Unit> by Codec.None()

    public data object CIDR : DataType<Unit>(650), Codec<Unit> by Codec.None()

    public data object CIDRArray : DataType<Unit>(651), Codec<Unit> by Codec.None()

    public data object Float4 : DataType<Unit>(700), Codec<Unit> by Codec.None()

    public data object Float8 : DataType<Unit>(701), Codec<Unit> by Codec.None()

    public data object Circle : DataType<Unit>(718), Codec<Unit> by Codec.None()

    public data object CircleArray : DataType<Unit>(719), Codec<Unit> by Codec.None()

    public data object Unknown : DataType<Unit>(705), Codec<Unit> by Codec.None()

    public data object Macaddr : DataType<Unit>(829), Codec<Unit> by Codec.None()

    public data object Inet : DataType<Unit>(869), Codec<Unit> by Codec.None()

    public data object BoolArray : DataType<Unit>(1000), Codec<Unit> by Codec.None()

    public data object QCharArray : DataType<Unit>(1002), Codec<Unit> by Codec.None()

    public data object NameArray : DataType<Unit>(1003), Codec<Unit> by Codec.None()

    public data object Int2Array : DataType<Unit>(1005), Codec<Unit> by Codec.None()

    public data object Int4Array : DataType<Unit>(1007), Codec<Unit> by Codec.None()

    public data object TextArray : DataType<Unit>(1009), Codec<Unit> by Codec.None()

    public data object TIDArray : DataType<Unit>(1010), Codec<Unit> by Codec.None()

    public data object ByteaArray : DataType<Unit>(1001), Codec<Unit> by Codec.None()

    public data object XIDArray : DataType<Unit>(1011), Codec<Unit> by Codec.None()

    public data object CIDArray : DataType<Unit>(1012), Codec<Unit> by Codec.None()

    public data object BPCharArray : DataType<Unit>(1014), Codec<Unit> by Codec.None()

    public data object VarcharArray : DataType<Unit>(1015), Codec<Unit> by Codec.None()

    public data object Int8Array : DataType<Unit>(1016), Codec<Unit> by Codec.None()

    public data object PointArray : DataType<Unit>(1017), Codec<Unit> by Codec.None()

    public data object LsegArray : DataType<Unit>(1018), Codec<Unit> by Codec.None()

    public data object PathArray : DataType<Unit>(1019), Codec<Unit> by Codec.None()

    public data object BoxArray : DataType<Unit>(1020), Codec<Unit> by Codec.None()

    public data object Float4Array : DataType<Unit>(1021), Codec<Unit> by Codec.None()

    public data object Float8Array : DataType<Unit>(1022), Codec<Unit> by Codec.None()

    public data object PolygonArray : DataType<Unit>(1027), Codec<Unit> by Codec.None()

    public data object OIDArray : DataType<Unit>(1028), Codec<Unit> by Codec.None()

    public data object ACLItem : DataType<Unit>(1033), Codec<Unit> by Codec.None()

    public data object ACLItemArray : DataType<Unit>(1034), Codec<Unit> by Codec.None()

    public data object MacaddrArray : DataType<Unit>(1040), Codec<Unit> by Codec.None()

    public data object InetArray : DataType<Unit>(1041), Codec<Unit> by Codec.None()

    public data object BPChar : DataType<Unit>(1042), Codec<Unit> by Codec.None()

    public data object Varchar : DataType<Unit>(1043), Codec<Unit> by Codec.None()

    public data object Date : DataType<Unit>(1082), Codec<Unit> by Codec.None()

    public data object Time : DataType<Unit>(1083), Codec<Unit> by Codec.None()

    public data object Timestamp : DataType<Unit>(1114), Codec<Unit> by Codec.None()

    public data object TimestampArray : DataType<Unit>(1115), Codec<Unit> by Codec.None()

    public data object DateArray : DataType<Unit>(1182), Codec<Unit> by Codec.None()

    public data object TimeArray : DataType<Unit>(1183), Codec<Unit> by Codec.None()

    public data object Timestamptz : DataType<Unit>(1184), Codec<Unit> by Codec.None()

    public data object TimestamptzArray : DataType<Unit>(1185), Codec<Unit> by Codec.None()

    public data object Interval : DataType<Unit>(1186), Codec<Unit> by Codec.None()

    public data object IntervalArray : DataType<Unit>(1187), Codec<Unit> by Codec.None()

    public data object NumericArray : DataType<Unit>(1231), Codec<Unit> by Codec.None()

    public data object Timetz : DataType<Unit>(1266), Codec<Unit> by Codec.None()

    public data object TimetzArray : DataType<Unit>(1270), Codec<Unit> by Codec.None()

    public data object Bit : DataType<Unit>(1560), Codec<Unit> by Codec.None()

    public data object BitArray : DataType<Unit>(1561), Codec<Unit> by Codec.None()

    public data object Varbit : DataType<Unit>(1562), Codec<Unit> by Codec.None()

    public data object VarbitArray : DataType<Unit>(1563), Codec<Unit> by Codec.None()

    public data object Numeric : DataType<Unit>(1700), Codec<Unit> by Codec.None()

    public data object Record : DataType<Unit>(2249), Codec<Unit> by Codec.None()

    public data object RecordArray : DataType<Unit>(2287), Codec<Unit> by Codec.None()

    public data object UUID : DataType<Unit>(2950), Codec<Unit> by Codec.None()

    public data object UUIDArray : DataType<Unit>(2951), Codec<Unit> by Codec.None()

    public data object JSONB : DataType<Unit>(3802), Codec<Unit> by Codec.None()

    public data object JSONBArray : DataType<Unit>(3807), Codec<Unit> by Codec.None()

    public data object Daterange : DataType<Unit>(3912), Codec<Unit> by Codec.None()

    public data object DaterangeArray : DataType<Unit>(3913), Codec<Unit> by Codec.None()

    public data object Int4range : DataType<Unit>(3904), Codec<Unit> by Codec.None()

    public data object Int4rangeArray : DataType<Unit>(3905), Codec<Unit> by Codec.None()

    public data object Numrange : DataType<Unit>(3906), Codec<Unit> by Codec.None()

    public data object NumrangeArray : DataType<Unit>(3907), Codec<Unit> by Codec.None()

    public data object Tsrange : DataType<Unit>(3908), Codec<Unit> by Codec.None()

    public data object TsrangeArray : DataType<Unit>(3909), Codec<Unit> by Codec.None()

    public data object Tstzrange : DataType<Unit>(3910), Codec<Unit> by Codec.None()

    public data object TstzrangeArray : DataType<Unit>(3911), Codec<Unit> by Codec.None()

    public data object Int8range : DataType<Unit>(3926), Codec<Unit> by Codec.None()

    public data object Int8rangeArray : DataType<Unit>(3927), Codec<Unit> by Codec.None()

    public data object JSONPath : DataType<Unit>(4072), Codec<Unit> by Codec.None()

    public data object JSONPathArray : DataType<Unit>(4073), Codec<Unit> by Codec.None()

    public data object Int4multirange : DataType<Unit>(4451), Codec<Unit> by Codec.None()

    public data object Nummultirange : DataType<Unit>(4532), Codec<Unit> by Codec.None()

    public data object Tsmultirange : DataType<Unit>(4533), Codec<Unit> by Codec.None()

    public data object Tstzmultirange : DataType<Unit>(4534), Codec<Unit> by Codec.None()

    public data object Datemultirange : DataType<Unit>(4535), Codec<Unit> by Codec.None()

    public data object Int8multirange : DataType<Unit>(4536), Codec<Unit> by Codec.None()

    public data object Int4multirangeArray : DataType<Unit>(6150), Codec<Unit> by Codec.None()

    public data object NummultirangeArray : DataType<Unit>(6151), Codec<Unit> by Codec.None()

    public data object TsmultirangeArray : DataType<Unit>(6152), Codec<Unit> by Codec.None()

    public data object TstzmultirangeArray : DataType<Unit>(6153), Codec<Unit> by Codec.None()

    public data object DatemultirangeArray : DataType<Unit>(6155), Codec<Unit> by Codec.None()

    public data object Int8multirangeArray : DataType<Unit>(6157), Codec<Unit> by Codec.None()

    public companion object {
        public val entries: Set<DataType<*>> = setOf(
            Bool,
            Bytea,
            QChar,
            Name,
            Int8,
            Int2,
            Int4,
            Text,
            OID,
            TID,
            XID,
            CID,
            JSON,
            JSONArray,
            Point,
            Lseg,
            Path,
            Box,
            Polygon,
            Line,
            LineArray,
            CIDR,
            CIDRArray,
            Float4,
            Float8,
            Circle,
            CircleArray,
            Unknown,
            Macaddr,
            Inet,
            BoolArray,
            QCharArray,
            Name,
            Int2Array,
            Int4Array,
            TextArray,
            TIDArray,
            ByteaArray,
            XIDArray,
            CIDArray,
            BPCharArray,
            VarcharArray,
            Int8Array,
            PointArray,
            LsegArray,
            PathArray,
            BoxArray,
            Float4Array,
            Float8Array,
            PolygonArray,
            OIDArray,
            ACLItem,
            ACLItemArray,
            MacaddrArray,
            InetArray,
            BPChar,
            Varchar,
            Date,
            Time,
            Timestamp,
            TimestampArray,
            DateArray,
            TimeArray,
            Timestamptz,
            TimestamptzArray,
            Interval,
            IntervalArray,
            NumericArray,
            Timetz,
            TimetzArray,
            Bit,
            BitArray,
            Varbit,
            VarbitArray,
            Numeric,
            Record,
            RecordArray,
            UUID,
            UUIDArray,
            JSONB,
            JSONBArray,
            Daterange,
            DaterangeArray,
            Int4range,
            Int4rangeArray,
            Numrange,
            NumrangeArray,
            JSONPath,
            JSONPathArray,
            Int4multirange,
            Nummultirange,
            Tsmultirange,
            Tstzmultirange,
            Datemultirange,
            Int8multirange,
            Int4multirangeArray,
            NummultirangeArray,
            TsmultirangeArray,
            TstzmultirangeArray,
            DatemultirangeArray,
            Int8multirangeArray,
        )

        public fun fromCode(id: Int): DataType<*> =
            entries.find { it.id == id } ?: error("Unknown data type object id: $id")
    }
}
