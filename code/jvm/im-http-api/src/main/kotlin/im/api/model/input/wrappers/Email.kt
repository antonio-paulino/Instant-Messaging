package im.api.model.input.wrappers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

@JsonDeserialize(using = EmailDeserializer::class)
@JsonSerialize(using = EmailSerializer::class) // For testing purposes
data class Email(
    @field:Email(message = "Email must be a valid email address")
    @field:NotNull(message = "Email is required")
    @field:NotBlank(message = "Email must not be blank")
    @field:Size(min = 8, max = 50, message = "Email must be between 5 and 50 characters")
    val value: String,
) {
    fun toDomain() = im.domain.wrappers.Email(value)

    override fun toString() = value
}

class EmailDeserializer : JsonDeserializer<im.api.model.input.wrappers.Email>() {
    override fun deserialize(
        p: JsonParser,
        ctxt: DeserializationContext,
    ): im.api.model.input.wrappers.Email {
        val value = p.readValueAs(String::class.java)
        return im.api.model.input.wrappers.Email(value)
    }
}

class EmailSerializer : JsonSerializer<im.api.model.input.wrappers.Email>() {
    override fun serialize(
        value: im.api.model.input.wrappers.Email,
        gen: JsonGenerator,
        serializers: SerializerProvider,
    ) {
        gen.writeString(value.value)
    }
}
