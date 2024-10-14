package im.api.model.input.wrappers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@JsonDeserialize(using = NameDeserializer::class)
@JsonSerialize(using = NameSerializer::class) // For testing purposes
data class Name(
    @field:Size(min = 3, max = 30, message = "Name must be between 3 and 50 characters")
    @field:NotBlank(message = "Name must not be blank")
    val value: String,
) {
    fun toDomain() = im.domain.wrappers.Name(value)

    override fun toString() = value
}

class NameDeserializer : JsonDeserializer<Name>() {
    override fun deserialize(
        p: JsonParser,
        ctxt: DeserializationContext,
    ): Name {
        return Name(p.text)
    }
}

class NameSerializer : JsonSerializer<Name>() {
    override fun serialize(
        value: Name,
        gen: JsonGenerator,
        serializers: SerializerProvider,
    ) {
        gen.writeString(value.value)
    }
}
