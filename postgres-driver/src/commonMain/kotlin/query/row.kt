package one.keia.oss.psql.driver.query

import io.ktor.utils.io.core.*
import naibu.collections.SuspendIterator
import naibu.ext.into
import one.keia.oss.psql.protocol.Message
import one.keia.oss.psql.protocol.types.DataType
import kotlin.jvm.JvmName

public class Row(
    private val inner: Message.Backend.DataRow,
    private val description: Message.Backend.RowDescription
) {
    public fun toMap(): Map<String, Any?> = description.fields.associate { it.name to get(it.name) }

    @JvmName("get0")
    public inline fun <reified T : Any> get(name: String): T? = get(name)?.into()

    public fun get(name: String): Any? = get(description.fields.indexOfFirst { it.name == name })

    public fun get(index: Int): Any? {
        val field = description.fields[index]
        val column = inner.columns[index]
        if (field.dataType is DataType.Companion.External) {
            return field.dataType to column
        }

        return column?.let { bytes -> field.dataType.codec.decode(field.dataType, field.formatCode, ByteReadPacket(bytes)) }
    }
}

public interface RowIterator : SuspendIterator<Row>

