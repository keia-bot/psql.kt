package one.keia.oss.psql.protocol.types

import io.ktor.utils.io.core.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import naibu.ext.into
import one.keia.oss.psql.protocol.FormatCode
import one.keia.oss.psql.protocol.readChar
import one.keia.oss.psql.protocol.writeChar

public interface Codec<T> {
    public fun decode(type: DataType<*>, format: FormatCode, value: ByteReadPacket): T

    public fun encode(type: DataType<*>, format: FormatCode, value: T): ByteReadPacket = buildPacket {
        encode(type, format, value)
    }

    public fun BytePacketBuilder.encode(type: DataType<*>, format: FormatCode, value: T)

    public class Builder<T> {
        public lateinit var _decode: (DataType<*>, FormatCode, ByteReadPacket) -> T

        public lateinit var _encode: BytePacketBuilder.(DataType<*>, FormatCode, T) -> Unit

       public fun decode(block: (DataType<*>, FormatCode, ByteReadPacket) -> T) {
            _decode = block
        }

        public fun encode(block: BytePacketBuilder.(DataType<*>, FormatCode, T) -> Unit) {
            _encode = block
        }

        public fun build(): Codec<T> = object : Codec<T> {
            override fun decode(type: DataType<*>, format: FormatCode, value: ByteReadPacket): T = _decode(type, format, value)

            override fun BytePacketBuilder.encode(type: DataType<*>, format: FormatCode, value: T): Unit = _encode(type, format, value)
        }
    }

    public data object QChar : Codec<Char> {
        override fun decode(type: DataType<*>, format: FormatCode, value: ByteReadPacket): Char = value.readChar()
        override fun BytePacketBuilder.encode(type: DataType<*>, format: FormatCode, value: Char): Unit = writeChar(value)
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    public companion object {
        public operator fun <T> invoke(block: Builder<T>.() -> Unit): Codec<T> = Builder<T>().apply(block).build()

        public val Text: Codec<String> = Codec {
            decode { _, _, v -> v.readText() }
            encode { _, _, v -> writeText(v) }
        }

        public val Char: Codec<Char> = Codec {
            decode { _, _, v -> v.readChar() }
            encode { _, _, v -> writeChar(v) }
        }

        public val Int16: Codec<Short> = Codec {
            decode { _, f, v -> if (f == FormatCode.Binary) v.readShort() else v.readText().toShort() }
            encode { _, f, v -> if (f == FormatCode.Text) writeText(v.toString()) else writeShort(v) }
        }

        public val Int32: Codec<Int> = Codec {
            decode { _, f, v -> if (f == FormatCode.Binary) v.readInt() else v.readText().toInt() }
            encode { _, f, v -> if (f == FormatCode.Text) writeText(v.toString()) else writeInt(v) }
        }

        public val Int64: Codec<Long> = Codec {
            decode { _, f, v -> if (f == FormatCode.Binary) v.readLong() else v.readText().toLong() }
            encode { _, f, v -> if (f == FormatCode.Text) writeText(v.toString()) else writeLong(v) }
        }

        public val Float32: Codec<Float> = Codec {
            decode { _, f, v -> if (f == FormatCode.Binary) v.readFloat() else v.readText().toFloat() }
            encode { _, f, v -> if (f == FormatCode.Text) writeText(v.toString()) else writeFloat(v) }
        }

        public val Float64: Codec<Double> = Codec {
            decode { _, f, v -> if (f == FormatCode.Binary) v.readDouble() else v.readText().toDouble() }
            encode { _, f, v -> if (f == FormatCode.Text) writeText(v.toString()) else writeDouble(v) }
        }

        public val UInt32: Codec<UInt> = Codec {
            decode { _, f, v -> if (f == FormatCode.Binary) v.readUInt() else v.readText().toUInt() }
            encode { _, f, v -> if (f == FormatCode.Text) writeText(v.toString()) else writeUInt(v) }
        }

        public val UInt64: Codec<ULong> = Codec {
            decode { _, f, v -> if (f == FormatCode.Binary) v.readULong() else v.readText().toULong() }
            encode { _, f, v -> if (f == FormatCode.Text) writeText(v.toString()) else writeULong(v) }
        }

        public val Bool: Codec<Boolean> = Codec {
            decode { _, _, v -> v.readByte() != 0.toByte() }
            encode { _, _, v -> writeByte(if (v) 1 else 0) }
        }

        public val JSON: Codec<JsonElement> = Codec {
            decode { _, _, v -> Json.decodeFromString(v.readText()) }
            encode { _, _, v -> writeText(Json.encodeToString(v)) }
        }

       public fun <T> Array(codec: Codec<T>): Codec<List<T?>> = Codec {
           encode { type, format, value ->

           }

           decode { type, format, value ->
               when (format) {
                   FormatCode.Binary -> TODO()

                   FormatCode.Text   -> {
                       val text = value
                           .readText()
                           .let { it.substring(1..<it.lastIndex) }

                       if (text.isEmpty()) {
                           return@decode emptyList()
                       }

                       val items = mutableListOf<T?>()
                       val chars = text.toCharArray().iterator()
                       var inQuotes = false
                       var inEscape = false
                       var done = false
                       val element = StringBuilder()
                       while (!done) {
                           element.clear()
                           el@while (chars.hasNext()) {
                               val char = chars.next()
                               if (inEscape) {
                                   element.append(char)
                                   inEscape = false
                                   continue@el
                               }

                               when {
                                   inEscape -> {
                                       element.append(char)
                                       inEscape = false
                                   }

                                   char == '\\' -> inEscape = true

                                   char == '"' -> inQuotes = !inQuotes

                                   char == ',' && !inQuotes -> break@el

                                   else -> element.append(char)
                               }
                           }

                           items += (if (element.toString() == "NULL") null else codec.decode(
                               type.into<DataType.Array<T>>().element,
                               format,
                               ByteReadPacket(element.toString().encodeToByteArray()))
                                    )

                           done = !chars.hasNext()
                       }

                       items
                   }
               }
           }
       }

        public val Bytea: Codec<ByteArray> = Codec {
            decode { _, _, v -> v.readBytes() }
            encode { _, _, v -> writeFully(v) }
        }

        public fun <I, A>transform(codec: Codec<A>, decode: (A) -> I, encode: (I) -> A): Codec<I> = Codec<I> {
            decode { type, format, value ->
                decode(codec.decode(type, format, value))
            }

            encode { t, f, v ->
                with (codec) { encode(t, f, encode(v)) }
            }
        }

        public fun <T> todo(): Codec<T> = Codec {
            decode { _, _, _ -> TODO() }
            encode { _, _, _ -> TODO() }
        }
    }
}