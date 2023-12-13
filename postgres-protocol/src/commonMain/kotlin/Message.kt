package one.keia.oss.psql.protocol

import io.ktor.utils.io.core.*
import kotlin.jvm.JvmInline

public sealed interface Message {
    public sealed interface Frontend : Message {
        public data class Close(val type: StatementType, val name: String) : Frontend {

            public companion object : MessageDeclaration<Close>(Type.Frontend, 'C') {
                override fun read0(packet: ByteReadPacket): Close {
                    val type = StatementType.fromCode(packet.readChar())
                    val name = packet.readCString()
                    return Close(type, name)
                }

                override fun write0(builder: BytePacketBuilder, value: Close) {
                    builder.writeChar(value.type.code)
                    builder.writeCString(value.name)
                }
            }
        }

        @JvmInline
        public value class CopyFail(public val message: String) : Frontend {
            public companion object : MessageDeclaration<CopyFail>(Type.Frontend, 'f') {
                override fun read0(packet: ByteReadPacket): CopyFail = CopyFail(packet.readCString())

                override fun write0(builder: BytePacketBuilder, value: CopyFail) {
                    builder.writeCString(value.message)
                }
            }
        }

        public data class Describe(val type: StatementType, val name: String) : Frontend {
            public companion object : MessageDeclaration<Describe>(Type.Frontend, 'D') {
                override fun read0(packet: ByteReadPacket): Describe {
                    val type = StatementType.fromCode(packet.readChar())
                    val name = packet.readCString()
                    return Describe(type, name)
                }

                override fun write0(builder: BytePacketBuilder, value: Describe) {
                    builder.writeChar(value.type.code)
                    builder.writeCString(value.name)
                }
            }
        }

        public data object Flush : Frontend, MessageDeclaration<Flush>(Type.Frontend, 'H') {
            override fun read0(packet: ByteReadPacket): Flush = Flush

            override fun write0(builder: BytePacketBuilder, value: Flush) {
                // no-op
            }
        }

        public data class FunctionCall(
            val objectId: Int,
            val argumentFormatCodes: List<FormatCode>,
            val arguments: List<ByteReadPacket?>,
            val resultFormatCode: FormatCode,
        ) : Frontend {
            public companion object : MessageDeclaration<FunctionCall>(Type.Frontend, 'F') {
                override fun read0(packet: ByteReadPacket): FunctionCall {
                    val objectId = packet.readInt()

                    //
                    val argumentFormatCodes = List(packet.readShort().toInt()) {
                        FormatCode.fromCode(packet.readShort())
                    }

                    //
                    val arguments = List(packet.readShort().toInt()) {
                        when (val length = packet.readInt()) {
                            -1   -> null
                            0    -> ByteReadPacket.Empty
                            else -> ByteReadPacket(packet.readBytes(length))
                        }
                    }

                    //
                    return FunctionCall(
                        objectId,
                        argumentFormatCodes,
                        arguments,
                        FormatCode.fromCode(packet.readShort())
                    )
                }

                override fun write0(builder: BytePacketBuilder, value: FunctionCall) {
                    builder.writeInt(value.objectId)

                    //
                    builder.writeShort(value.argumentFormatCodes.size.toShort())
                    for (it in value.argumentFormatCodes) {
                        builder.writeShort(it.code)
                    }

                    //
                    builder.writeShort(value.arguments.size.toShort())
                    for (it in value.arguments) {
                        if (it == null) {
                            builder.writeInt(-1)
                        } else {
                            builder.writeInt(it.remaining.toInt())
                            builder.writePacket(it)
                        }
                    }

                    //
                    builder.writeShort(value.resultFormatCode.code)
                }
            }
        }

        public data class Parse(val name: String, val query: String, val parameterTypes: List<Int>) : Frontend {
            public companion object : MessageDeclaration<Parse>(Type.Frontend, 'P') {
                override fun read0(packet: ByteReadPacket): Parse {
                    val name = packet.readCString()
                    val query = packet.readCString()
                    val parameterTypes = List(packet.readShort().toInt()) { packet.readInt() }
                    return Parse(name, query, parameterTypes)
                }

                override fun write0(builder: BytePacketBuilder, value: Parse) {
                    builder.writeCString(value.name)
                    builder.writeCString(value.query)
                    builder.writeShort(value.parameterTypes.size.toShort())
                    for (it in value.parameterTypes) builder.writeInt(it)
                }
            }
        }

        @JvmInline
        public value class PasswordMessage(public val password: String) : Frontend {
            public companion object : MessageDeclaration<PasswordMessage>(Type.Frontend, 'p') {
                override fun read0(packet: ByteReadPacket): PasswordMessage = PasswordMessage(packet.readCString())

                override fun write0(builder: BytePacketBuilder, value: PasswordMessage) {
                    builder.writeCString(value.password)
                }
            }
        }

        @JvmInline
        public value class Query(public val query: String) : Frontend {
            public companion object : MessageDeclaration<Query>(Type.Frontend, 'Q') {
                override fun read0(packet: ByteReadPacket): Query = Query(packet.readText())

                override fun write0(builder: BytePacketBuilder, value: Query) {
                    builder.writeText(value.query)
                }
            }
        }

        public data class SASLInitialResponse(
            val mechanismName: String,
            val initialResponse: ByteReadPacket?,
        ) : Frontend {
            public companion object : MessageDeclaration<SASLInitialResponse>(Type.Frontend, 'p') {
                override fun read0(packet: ByteReadPacket): SASLInitialResponse {
                    throw UnsupportedOperationException()
                }

                override fun write0(builder: BytePacketBuilder, value: SASLInitialResponse) {
                    builder.writeCString(value.mechanismName)
                    if (value.initialResponse != null) {
                        builder.writeInt(value.initialResponse.remaining.toInt())
                        builder.writePacket(value.initialResponse)
                    } else {
                        builder.writeInt(-1)
                    }
                }
            }
        }

        public data class SASLResponse(val response: ByteReadPacket?) : Frontend {
            public companion object : MessageDeclaration<SASLResponse>(Type.Frontend, 'p') {
                override fun read0(packet: ByteReadPacket): SASLResponse {
                    throw UnsupportedOperationException()
                }

                override fun write0(builder: BytePacketBuilder, value: SASLResponse) {
                    if (value.response != null) {
                        builder.writeInt(value.response.remaining.toInt())
                        builder.writePacket(value.response)
                    } else {
                        builder.writeInt(-1)
                    }
                }
            }
        }
    }

    public sealed interface Backend : Message {
        public data class BackendKeyData(val processId: Int, val secretKey: Int) : Backend {
            public companion object : MessageDeclaration<BackendKeyData>(Type.Backend, 'K') {
                override fun read0(packet: ByteReadPacket): BackendKeyData {
                    val processId = packet.readInt()
                    val secretKey = packet.readInt()
                    return BackendKeyData(processId, secretKey)
                }

                override fun write0(builder: BytePacketBuilder, value: BackendKeyData) {
                    builder.writeInt(value.processId)
                    builder.writeInt(value.secretKey)
                }
            }
        }

        public data class Bind(
            val destinationPortal: String,
            val preparedStatement: String,
            val parameterFormatCodes: List<FormatCode>,
            val parameters: List<ByteArray>,
            val resultFormatCodes: List<Int>,
        ) : Backend {
            public companion object : MessageDeclaration<Bind>(Type.Backend, 'B') {
                override fun read0(packet: ByteReadPacket): Bind {
                    val dpn = packet.readCString()
                    val ssn = packet.readCString()

                    //
                    val parameterFormatCodes = List(packet.readShort().toInt()) {
                        FormatCode.fromCode(packet.readShort())
                    }

                    //
                    val parameters = List(packet.readShort().toInt()) {
                        packet.readBytes(packet.readInt())
                    }

                    //
                    val resultFormatCodes = List(packet.readShort().toInt()) {
                        packet.readInt()
                    }

                    return Bind(dpn, ssn, parameterFormatCodes, parameters, resultFormatCodes)
                }

                override fun write0(builder: BytePacketBuilder, value: Bind) {
                    builder.writeCString(value.destinationPortal)
                    builder.writeCString(value.preparedStatement)

                    //
                    builder.writeShort(value.parameterFormatCodes.size.toShort())
                    for (it in value.parameterFormatCodes) {
                        builder.writeShort(it.code)
                    }

                    //
                    builder.writeShort(value.parameters.size.toShort())
                    for (it in value.parameters) {
                        builder.writeInt(it.size)
                        builder.writeFully(it)
                    }

                    //
                    builder.writeShort(value.resultFormatCodes.size.toShort())
                    for (it in value.resultFormatCodes) {
                        builder.writeInt(it)
                    }
                }
            }
        }

        public data object BindComplete : Backend, MessageDeclaration<BindComplete>(Type.Backend, '2') {
            override fun read0(packet: ByteReadPacket): BindComplete = BindComplete

            override fun write0(builder: BytePacketBuilder, value: BindComplete) {
                // no-op
            }
        }

        public data object CloseComplete : Backend, MessageDeclaration<CloseComplete>(Type.Backend, '3') {
            override fun read0(packet: ByteReadPacket): CloseComplete = CloseComplete

            override fun write0(builder: BytePacketBuilder, value: CloseComplete) {
                // no-op
            }
        }

        @JvmInline
        public value class CommandComplete(public val tag: String) : Backend {
            public companion object : MessageDeclaration<CommandComplete>(Type.Backend, 'C') {
                override fun read0(packet: ByteReadPacket): CommandComplete =
                    CommandComplete(packet.readCString())

                override fun write0(builder: BytePacketBuilder, value: CommandComplete) {
                    builder.writeCString(value.tag)
                }
            }
        }

        public data class CopyInResponse(val isTextual: Boolean, val columns: List<Short>) : Backend {
            public companion object : MessageDeclaration<CopyInResponse>(Type.Backend, 'G') {
                override fun read0(packet: ByteReadPacket): CopyInResponse {
                    val isTextual = packet.readByte() == 0.toByte()
                    val columns = List(packet.readShort().toInt()) {
                        packet.readShort()
                    }
                    return CopyInResponse(isTextual, columns)
                }

                override fun write0(builder: BytePacketBuilder, value: CopyInResponse) {
                    builder.writeByte(if (value.isTextual) 0 else 1)
                    builder.writeShort(value.columns.size.toShort())
                    for (it in value.columns) {
                        builder.writeShort(it)
                    }
                }
            }
        }

        public data class CopyOutResponse(val isTextual: Boolean, val columns: List<Short>) : Backend {
            public companion object : MessageDeclaration<CopyOutResponse>(Type.Backend, 'H') {
                override fun read0(packet: ByteReadPacket): CopyOutResponse {
                    val isTextual = packet.readByte() == 0.toByte()
                    val columns = List(packet.readShort().toInt()) { packet.readShort() }
                    return CopyOutResponse(isTextual, columns)
                }

                override fun write0(builder: BytePacketBuilder, value: CopyOutResponse) {
                    builder.writeByte(if (value.isTextual) 0 else 1)
                    builder.writeShort(value.columns.size.toShort())
                    for (it in value.columns) builder.writeShort(it)
                }
            }
        }

        public data class CopyBothResponse(val isTextual: Boolean, val columns: List<Short>) : Backend {
            public companion object : MessageDeclaration<CopyBothResponse>(Type.Backend, 'W') {
                override fun read0(packet: ByteReadPacket): CopyBothResponse {
                    val isTextual = packet.readByte() == 0.toByte()
                    val columns = List(packet.readShort().toInt()) { packet.readShort() }
                    return CopyBothResponse(isTextual, columns)
                }

                override fun write0(builder: BytePacketBuilder, value: CopyBothResponse) {
                    builder.writeByte(if (value.isTextual) 0 else 1)
                    builder.writeShort(value.columns.size.toShort())
                    for (it in value.columns) builder.writeShort(it)
                }
            }
        }

        @JvmInline
        public value class DataRow(public val columns: List<ByteArray>) : Backend {
            public companion object : MessageDeclaration<DataRow>(Type.Backend, 'D') {
                override fun read0(packet: ByteReadPacket): DataRow {
                    val columns = List(packet.readShort().toInt()) { packet.readBytes(packet.readInt()) }
                    return DataRow(columns)
                }

                override fun write0(builder: BytePacketBuilder, value: DataRow) {
                    builder.writeShort(value.columns.size.toShort())
                    for (it in value.columns) {
                        builder.writeInt(it.size)
                        builder.writeFully(it)
                    }
                }
            }
        }

        public data object EmptyQueryResponse : Backend, MessageDeclaration<EmptyQueryResponse>(Type.Backend, 'I') {
            override fun read0(packet: ByteReadPacket): EmptyQueryResponse = EmptyQueryResponse

            override fun write0(builder: BytePacketBuilder, value: EmptyQueryResponse) {
                // no-op
            }
        }

        @JvmInline
        public value class ErrorResponse(public val errors: List<Field>) : Backend {
            public data class Field(val type: MessageFieldType, val value: String)

            public companion object : MessageDeclaration<ErrorResponse>(Type.Backend, 'E') {
                override fun read0(packet: ByteReadPacket): ErrorResponse {
                    val fields = mutableListOf<Field>()
                    while (packet.isNotEmpty) {
                        val code = packet.readByte()
                        if (code == 0.toByte()) break

                        val type = MessageFieldType.fromCode(code.toInt().toChar())
                                   // 55.7. Since more field types might be added in the future, frontends should silently
                                   // ignore fields of unrecognized type.
                                   ?: continue

                        val value = packet.readCString()
                        fields += Field(type, value)
                    }

                    return ErrorResponse(fields)
                }

                override fun write0(builder: BytePacketBuilder, value: ErrorResponse) {
                    for (field in value.errors) {
                        builder.writeByte(field.type.code.code.toByte())
                        builder.writeCString(field.value)
                    }

                    builder.writeByte(0)
                }
            }
        }

        @JvmInline
        public value class FunctionCallResponse(public val result: ByteArray?) : Backend {
            public companion object : MessageDeclaration<FunctionCallResponse>(Type.Backend, 'V') {
                override fun read0(packet: ByteReadPacket): FunctionCallResponse {
                    val length = packet.readInt()
                    val result = if (length == -1) null else packet.readBytes(length)
                    return FunctionCallResponse(result)
                }

                override fun write0(builder: BytePacketBuilder, value: FunctionCallResponse) {
                    builder.writeInt(if (value.result == null) -1 else value.result.size)
                    if (value.result != null) builder.writeFully(value.result)
                }
            }
        }

        public data object NoData : Backend, MessageDeclaration<NoData>(Type.Backend, 'n') {
            override fun read0(packet: ByteReadPacket): NoData = NoData

            override fun write0(builder: BytePacketBuilder, value: NoData) {
                // no-op
            }
        }

        public data class NoticeResponse(val fieldType: MessageFieldType, val value: String) : Backend {
            public companion object : MessageDeclaration<NoticeResponse>(Type.Backend, 'N') {
                override fun read0(packet: ByteReadPacket): NoticeResponse {
                    val fieldType = MessageFieldType.fromCode(packet.readByte().toInt().toChar())
                                    // 55.7. Since more field types might be added in the future, frontends should silently
                                    // ignore fields of unrecognized type.
                                    ?: return NoticeResponse(MessageFieldType.Unknown, packet.readCString())

                    val value = packet.readCString()
                    return NoticeResponse(fieldType, value)
                }

                override fun write0(builder: BytePacketBuilder, value: NoticeResponse) {
                    builder.writeByte(value.fieldType.code.code.toByte())
                    builder.writeCString(value.value)
                }
            }
        }

        public data class NotificationResponse(val processId: Int, val channel: String, val payload: String) : Backend {
            public companion object : MessageDeclaration<NotificationResponse>(Type.Backend, 'A') {
                override fun read0(packet: ByteReadPacket): NotificationResponse {
                    val processId = packet.readInt()
                    val channel = packet.readCString()
                    val payload = packet.readCString()
                    return NotificationResponse(processId, channel, payload)
                }

                override fun write0(builder: BytePacketBuilder, value: NotificationResponse) {
                    builder.writeInt(value.processId)
                    builder.writeCString(value.channel)
                    builder.writeCString(value.payload)
                }
            }
        }

        public data class ParameterDescription(val objectIDs: List<Int>) : Backend {
            public companion object : MessageDeclaration<ParameterDescription>(Type.Backend, 't') {
                override fun read0(packet: ByteReadPacket): ParameterDescription {
                    val objectIDs = List(packet.readShort().toInt()) { packet.readInt() }
                    return ParameterDescription(objectIDs)
                }

                override fun write0(builder: BytePacketBuilder, value: ParameterDescription) {
                    builder.writeShort(value.objectIDs.size.toShort())
                    for (it in value.objectIDs) builder.writeInt(it)
                }
            }
        }

        public data class ParameterStatus(val name: String, val value: String) : Backend {
            public companion object : MessageDeclaration<ParameterStatus>(Type.Backend, 'S') {
                override fun read0(packet: ByteReadPacket): ParameterStatus {
                    val name = packet.readCString()
                    val value = packet.readCString()
                    return ParameterStatus(name, value)
                }

                override fun write0(builder: BytePacketBuilder, value: ParameterStatus) {
                    builder.writeCString(value.name)
                    builder.writeCString(value.value)
                }
            }
        }

        public data object ParseComplete : Backend, MessageDeclaration<ParseComplete>(Type.Backend, '1') {
            override fun read0(packet: ByteReadPacket): ParseComplete = ParseComplete

            override fun write0(builder: BytePacketBuilder, value: ParseComplete) {
                // no-op
            }
        }

        public data object PortalSuspended : Backend, MessageDeclaration<PortalSuspended>(Type.Backend, 's') {
            override fun read0(packet: ByteReadPacket): PortalSuspended = PortalSuspended

            override fun write0(builder: BytePacketBuilder, value: PortalSuspended) {
                // no-op
            }
        }

        @JvmInline
        public value class RowDescription(public val fields: List<Field>) : Backend {
            public data class Field(
                val name: String,
                val tableObjectId: Int?,
                val tableAttributeNumber: Short?,
                val dataTypeObjectId: Int,
                val dataTypeSize: Short,
                val typeModifier: Int,
                val formatCode: FormatCode,
            )

            public companion object : MessageDeclaration<RowDescription>(Type.Backend, 'T') {
                override fun read0(packet: ByteReadPacket): RowDescription {
                    val fields = List(packet.readShort().toInt()) {
                        val name = packet.readCString()
                        val tableObjectId = packet.readInt().takeIf { it != 0 }
                        val tableAttributeNumber = packet.readShort().takeIf { it != 0.toShort() }
                        val dataTypeObjectId = packet.readInt()
                        val dataTypeSize = packet.readShort()
                        val typeModifier = packet.readInt()
                        val formatCode = FormatCode.fromCode(packet.readShort())
                        Field(
                            name,
                            tableObjectId,
                            tableAttributeNumber,
                            dataTypeObjectId,
                            dataTypeSize,
                            typeModifier,
                            formatCode
                        )
                    }

                    return RowDescription(fields)
                }

                override fun write0(builder: BytePacketBuilder, value: RowDescription) {
                    throw UnsupportedOperationException()
                }
            }
        }
    }

    @JvmInline
    public value class CopyData(public val data: ByteReadPacket) : Backend, Frontend {
        public companion object : MessageDeclaration<CopyData>(Type.Both, 'd') {
            override fun read0(packet: ByteReadPacket): CopyData =
                CopyData(packet)

            override fun write0(builder: BytePacketBuilder, value: CopyData) {
                builder.writePacket(value.data)
            }
        }
    }

    public data object CopyDone : Backend, Frontend, MessageDeclaration<CopyDone>(Type.Both, 'c') {
        override fun read0(packet: ByteReadPacket): CopyDone = CopyDone

        override fun write0(builder: BytePacketBuilder, value: CopyDone) {
            // no-op
        }
    }
}