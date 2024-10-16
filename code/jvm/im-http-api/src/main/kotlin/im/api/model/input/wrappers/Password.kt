package im.api.model.input.wrappers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import im.api.model.input.validators.domain.ValidPassword
import jakarta.validation.constraints.NotNull

/**
 * Wrapper for password.
 *
 * @property value The password.
 */
@JsonDeserialize(using = PasswordDeserializer::class)
@JsonSerialize(using = PasswordSerializer::class) // For testing purposes
data class Password(
    @field:ValidPassword(message = "Password must be valid")
    @field:NotNull(message = "Password is required")
    val value: String,
) {
    fun toDomain() =
        im.domain.wrappers.password
            .Password(value)

    override fun toString() = value
}

/**
 * Deserializer for [Password].
 *
 * Converts a key-value pair directly to a [Password] object, without having to define a
 * JSON object with a `value` field.
 */
class PasswordDeserializer : JsonDeserializer<Password>() {
    override fun deserialize(
        p: JsonParser,
        ctxt: DeserializationContext,
    ): Password = Password(p.text)
}

/**
 * Serializer for [Password].
 *
 * Converts a [Password] object directly to a key-value pair, without having to define a
 * JSON object with a `value` field.
 */
class PasswordSerializer : JsonSerializer<Password>() {
    override fun serialize(
        value: Password,
        gen: JsonGenerator,
        serializers: SerializerProvider,
    ) {
        gen.writeString(value.value)
    }
}
