package one.keia.oss.psql.protocol

import io.ktor.utils.io.*
import io.ktor.utils.io.core.*

public abstract class MessageDeclaration<T : Message>(public val type: Type, public val identifier: Char) {

    /**
     * Reads a message from the [channel].
     * The [channel] must be positioned at the beginning of the length field.
     */
    public suspend fun read(channel: ByteReadChannel): T {
        val length = channel.readInt() - 4
        val packet = channel.readPacket(length)
        return read0(packet)
    }

    /**
     * Writes a message to the [channel], including the identifier and length fields.
     *
     * @param channel the channel to write to
     * @param value   the value to write
     */
    public suspend fun write(channel: ByteWriteChannel, value: T) {
        channel.writeByte(identifier.code.toByte())

        val builder = BytePacketBuilder()
        write0(builder, value)

        // length includes itself... lol
        channel.writeInt(builder.size + 4)
        channel.writePacket(builder.build())
    }

    protected abstract fun read0(packet: ByteReadPacket): T

    protected abstract fun write0(builder: BytePacketBuilder, value: T)

    public enum class Type {
        Frontend,
        Backend,
        Both
    }
}

