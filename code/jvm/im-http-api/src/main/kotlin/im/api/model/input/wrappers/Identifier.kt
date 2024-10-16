package im.api.model.input.wrappers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import im.api.model.input.validators.domain.IdentifierValid

/**
 * Wrapper for identifier.
 *
 * @property value The identifier.
 */
@JsonDeserialize(using = IdentifierDeserializer::class)
@JsonSerialize(using = IdentifierSerializer::class) // For testing purposes
data class Identifier(
    @field:IdentifierValid(message = "Identifier must be a number")
    val value: String,
) {
    fun toDomain() =
        im.domain.wrappers.identifier
            .Identifier(value.toLong())

    override fun toString() = value
}

/**
 * Deserializer for [Identifier].
 *
 * Converts a key-value pair directly to an [Identifier] object, without having to define a
 * JSON object with a `value` field.
 */
class IdentifierDeserializer : JsonDeserializer<Identifier>() {
    override fun deserialize(
        p: JsonParser,
        ctxt: DeserializationContext,
    ): Identifier = Identifier(p.text)
}

/**
 * Serializer for [Identifier].
 *
 * Converts an [Identifier] object directly to a key-value pair, without having to define a
 * JSON object with a `value` field.
 */
class IdentifierSerializer : JsonSerializer<Identifier>() {
    override fun serialize(
        value: Identifier,
        gen: JsonGenerator,
        serializers: SerializerProvider,
    ) {
        gen.writeString(value.value)
    }
}
