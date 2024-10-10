package im.model.input.wrappers

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

@JsonDeserialize(using = EmailDeserializer::class)
data class Email(
    @field:Email(message = "Email must be a valid email address")
    @field:NotNull(message = "Email is required")
    @field:Size(min = 8, max = 50, message = "Email must be between 5 and 50 characters")
    val value: String
) {
    fun toDomain() = im.wrappers.Email(value)
}

class EmailDeserializer : JsonDeserializer<im.model.input.wrappers.Email>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): im.model.input.wrappers.Email {
        val value = p.readValueAs(String::class.java)
        return im.model.input.wrappers.Email(value)
    }
}
