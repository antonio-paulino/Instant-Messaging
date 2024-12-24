package im.api.model.input.wrappers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import im.api.model.input.validators.domain.EmailValid
import jakarta.validation.constraints.Email

/**
 * Wrapper for email.
 *
 * @property value The email.
 */
@JsonDeserialize(using = EmailDeserializer::class)
@JsonSerialize(using = EmailSerializer::class) // For testing purposes
data class Email(
    @field:EmailValid
    val value: String,
) {
    fun toDomain() =
        im.domain.wrappers.email
            .Email(value)

    override fun toString() = value
}

/**
 * Deserializer for [Email].
 *
 * Converts a key-value pair directly to an [Email] object, without having to define a
 * JSON object with a `value` field.
 */
class EmailDeserializer : JsonDeserializer<im.api.model.input.wrappers.Email>() {
    override fun deserialize(
        p: JsonParser,
        ctxt: DeserializationContext,
    ): im.api.model.input.wrappers.Email {
        val value = p.readValueAs(String::class.java)
        return im.api.model.input.wrappers
            .Email(value)
    }
}

/**
 * Serializer for [Email].
 *
 * Converts an [Email] object directly to a key-value pair, without having to define a
 * JSON object with a `value` field.
 */
class EmailSerializer : JsonSerializer<im.api.model.input.wrappers.Email>() {
    override fun serialize(
        value: im.api.model.input.wrappers.Email,
        gen: JsonGenerator,
        serializers: SerializerProvider,
    ) {
        gen.writeString(value.value)
    }
}
