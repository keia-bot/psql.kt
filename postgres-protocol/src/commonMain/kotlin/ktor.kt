package one.keia.oss.psql.protocol

import io.ktor.utils.io.core.*

internal fun Output.writeChar(char: Char) = writeByte(char.code.toByte())

internal fun Input.readChar(): Char = readByte().toInt().toChar()

internal fun Input.readPacketUntilDelimiter(delimiter: Byte) = buildPacket {
    readUntilDelimiter(delimiter, this)
}

internal fun Input.readCString() =
    readPacketUntilDelimiter(0).also { discard(1) }.readText()

internal fun Output.writeCString(text: String) {
    writeText(text)
    writeByte(0)
}
