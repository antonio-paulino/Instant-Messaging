package im.api.model.input.wrappers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import jakarta.validation.constraints.NotNull

@JsonDeserialize(using = PasswordDeserializer::class)
@JsonSerialize(using = PasswordSerializer::class) // For testing purposes
data class Password(
    @field:im.api.model.input.validators.Password(message = "Password must be valid")
    @field:NotNull(message = "Password is required")
    val value: String,
) {
    fun toDomain() = im.domain.wrappers.Password(value)

    override fun toString() = value
}

class PasswordDeserializer : JsonDeserializer<Password>() {
    override fun deserialize(
        p: JsonParser,
        ctxt: DeserializationContext,
    ): Password {
        return Password(p.text)
    }
}

class PasswordSerializer : JsonSerializer<Password>() {
    override fun serialize(
        value: Password,
        gen: JsonGenerator,
        serializers: SerializerProvider,
    ) {
        gen.writeString(value.value)
    }
}
