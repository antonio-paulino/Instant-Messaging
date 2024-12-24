package im.api.model.input.wrappers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import im.api.model.input.validators.domain.NameValid

/**
 * Wrapper for name.
 *
 * @property value The name.
 */
@JsonDeserialize(using = NameDeserializer::class)
@JsonSerialize(using = NameSerializer::class) // For testing purposes
data class Name(
    @field:NameValid
    val value: String,
) {
    fun toDomain() =
        im.domain.wrappers.name
            .Name(value)

    override fun toString() = value
}

/**
 * Deserializer for [Name].
 *
 * Converts a key-value pair directly to a [Name] object, without having to define a
 * JSON object with a `value` field.
 */
class NameDeserializer : JsonDeserializer<Name>() {
    override fun deserialize(
        p: JsonParser,
        ctxt: DeserializationContext,
    ): Name = Name(p.text)
}

/**
 * Serializer for [Name].
 *
 * Converts a [Name] object directly to a key-value pair, without having to define a
 * JSON object with a `value` field.
 */
class NameSerializer : JsonSerializer<Name>() {
    override fun serialize(
        value: Name,
        gen: JsonGenerator,
        serializers: SerializerProvider,
    ) {
        gen.writeString(value.value)
    }
}
