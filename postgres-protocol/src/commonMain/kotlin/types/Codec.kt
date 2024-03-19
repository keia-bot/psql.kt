package one.keia.oss.psql.protocol.types

import io.ktor.utils.io.core.*
import one.keia.oss.psql.protocol.FormatCode
import one.keia.oss.psql.protocol.readChar

public fun interface Codec<T> {
    /**
     *
     */
    public fun decode(format: FormatCode, value: ByteReadPacket): T

    @OptIn(ExperimentalUnsignedTypes::class)
    public companion object {
        public val Text: Codec<String> = Codec { _, value -> value.readText() }

        public val Char: Codec<Char> = Codec { format, value ->
            when (format) {
                FormatCode.Text   -> value.readText()[0]
                FormatCode.Binary -> value.readChar()
            }
        }

        public val Int16: Codec<Short> = Codec { format, value ->
            when (format) {
                FormatCode.Text   -> value.readText().toShort()
                FormatCode.Binary -> value.readShort()
            }
        }

        public val Int32: Codec<Int> = Codec { format, value ->
            when (format) {
                FormatCode.Text   -> value.readText().toInt()
                FormatCode.Binary -> value.readInt()
            }
        }

        public val Int64: Codec<Long> = Codec { format, value ->
            when (format) {
                FormatCode.Text   -> value.readText().toLong()
                FormatCode.Binary -> value.readLong()
            }
        }

        public val UInt32: Codec<UInt> = Codec { format, value ->
            when (format) {
                FormatCode.Text   -> value.readText().toUInt()
                FormatCode.Binary -> value.readUInt()
            }
        }

        public fun <T> None(): Codec<T> = Codec { _, _ -> TODO() }
    }
}
