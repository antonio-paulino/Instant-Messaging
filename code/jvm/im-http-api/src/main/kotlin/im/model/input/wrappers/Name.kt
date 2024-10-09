package im.model.input.wrappers

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@JsonDeserialize(using = NameDeserializer::class)
data class Name(
    @field:Size(min = 3, max = 30, message = "Name must be between 3 and 50 characters")
    @field:NotBlank(message = "Name must not be blank")
    val value: String
) {
    fun toDomain() = im.wrappers.Name(value)
}

class NameDeserializer : JsonDeserializer<Name>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Name {
        val value = p.readValueAs(String::class.java)
        return Name(value)
    }
}