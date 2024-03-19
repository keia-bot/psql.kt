package one.keia.oss.psql.protocol

import io.ktor.utils.io.*
import naibu.ext.into
import kotlin.reflect.KClass

public data class MessageRegistry<M : Message>(val type: MessageDeclaration.Type) {
    private val members = mutableSetOf<Member>()

    public fun findById(id: Char): MessageDeclaration<out M>? =
        members.find { it.declaration.identifier == id }?.declaration

    public fun findByValue(value: M): MessageDeclaration<in M>? =
        members.find { it.kClass.isInstance(value) }?.declaration?.into()

    internal inline fun <reified T : M> register(declaration: MessageDeclaration<T>) {
        register(T::class, declaration)
    }

    internal fun register(kClass: KClass<out M>, declaration: MessageDeclaration<out M>) {
        members.add(Member(kClass, declaration))
    }

    internal inner class Member(val kClass: KClass<out M>, val declaration: MessageDeclaration<out M>)

    internal companion object {
        operator fun invoke(
            type: MessageDeclaration.Type,
            block: MessageRegistry<Message>.() -> Unit,
        ): MessageRegistry<Message> {
            val registry = MessageRegistry<Message>(type)
            registry.block()
            return registry
        }
    }
}

public val FRONTEND_MESSAGES: MessageRegistry<Message> = MessageRegistry(MessageDeclaration.Type.Frontend) {
    register(Message.Frontend.Close)
    register(Message.Frontend.CopyFail)
    register(Message.Frontend.Describe)
    register(Message.Frontend.Flush)
    register(Message.Frontend.FunctionCall)
    register(Message.Frontend.Parse)
    register(Message.Frontend.PasswordMessage)
    register(Message.Frontend.Query)
    register(Message.Frontend.SASLInitialResponse)
    register(Message.Frontend.SASLResponse)
    register(Message.Frontend.StartupMessage)
    register(Message.Frontend.Sync)
    register(Message.Frontend.Terminate)
    register(Message.CopyDone)
    register(Message.CopyData)
}

private val BACKEND_MESSAGES = MessageRegistry(MessageDeclaration.Type.Backend) {
    register(Message.Backend.Authentication)
    register(Message.Backend.BackendKeyData)
    register(Message.Backend.Bind)
    register(Message.Backend.BindComplete)
    register(Message.Backend.CloseComplete)
    register(Message.Backend.CommandComplete)
    register(Message.Backend.CopyInResponse)
    register(Message.Backend.CopyOutResponse)
    register(Message.Backend.CopyBothResponse)
    register(Message.Backend.DataRow)
    register(Message.Backend.EmptyQueryResponse)
    register(Message.Backend.ErrorResponse)
    register(Message.Backend.FunctionCallResponse)
    register(Message.Backend.NoData)
    register(Message.Backend.NoticeResponse)
    register(Message.Backend.NotificationResponse)
    register(Message.Backend.ParameterStatus)
    register(Message.Backend.ParseComplete)
    register(Message.Backend.PortalSuspended)
    register(Message.Backend.ReadyForQuery)
    register(Message.Backend.RowDescription)
    register(Message.CopyDone)
    register(Message.CopyData)
}

public suspend inline fun <reified T : Message.Frontend> ByteWriteChannel.writePostgresMessage(
    value: T,
    writeIdentifier: Boolean = true,
) {
    FRONTEND_MESSAGES.findByValue(value)?.write(this, value, writeIdentifier)
}

public suspend fun ByteReadChannel.readPostgresMessage(): Message.Backend? {
    val id = readByte()
        .toInt()
        .toChar()

    return BACKEND_MESSAGES.findById(id)
        ?.read(this)
        ?.into()
}
