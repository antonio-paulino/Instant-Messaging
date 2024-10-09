package im.model.input.wrappers

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import im.model.input.validators.Password
import jakarta.validation.constraints.NotNull

@JsonDeserialize(using = PasswordDeserializer::class)
data class Password(
    @field:Password(message = "Password must be valid")
    @field:NotNull(message = "Password is required")
    val value: String
) {
    fun toDomain() = im.wrappers.Password(value)
}

class PasswordDeserializer : JsonDeserializer<im.model.input.wrappers.Password>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): im.model.input.wrappers.Password {
        val value = p.readValueAs(String::class.java)
        return im.model.input.wrappers.Password(value)
    }
}
