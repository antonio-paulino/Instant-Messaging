package im.api.model.input.wrappers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import im.api.model.input.validators.IsNumber
import jakarta.validation.constraints.NotNull

@JsonDeserialize(using = IdentifierDeserializer::class)
@JsonSerialize(using = IdentifierSerializer::class) // For testing purposes
data class Identifier(
    @field:IsNumber(message = "Identifier must be a number")
    @field:NotNull(message = "Identifier is required")
    val value: String,
) {
    fun toDomain() = im.domain.wrappers.Identifier(value.toLong())

    override fun toString() = value
}

class IdentifierDeserializer : JsonDeserializer<Identifier>() {
    override fun deserialize(
        p: JsonParser,
        ctxt: DeserializationContext,
    ): Identifier {
        return Identifier(p.text)
    }
}

class IdentifierSerializer : JsonSerializer<Identifier>() {
    override fun serialize(
        value: Identifier,
        gen: JsonGenerator,
        serializers: SerializerProvider,
    ) {
        gen.writeString(value.value)
    }
}
