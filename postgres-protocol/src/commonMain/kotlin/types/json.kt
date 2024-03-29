package one.keia.oss.psql.protocol.types

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import naibu.common.identity

public data object JSON : DataType<JsonElement>(114, Codec.JSON) {
    public data object Array : DataType<JsonArray>(199, Codec.transform(Codec.JSON, { it.jsonArray }, ::identity))
}
public data object JSONB : DataType<JsonElement>(3802, Codec.JSON) {
    public data object Array : DataType<JsonArray>(3807, Codec.transform(Codec.JSON, { it.jsonArray }, ::identity))
}
