package im.model.input.wrappers

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import im.model.input.validators.IsNumber
import jakarta.validation.constraints.NotNull

@JsonDeserialize(using = IdentifierDeserializer::class)
data class Identifier(
    @field:IsNumber(message = "Identifier must be a number")
    @field:NotNull(message = "Identifier is required")
    val value: String
) {
    fun toDomain() = im.wrappers.Identifier(value.toLong())
}

class IdentifierDeserializer : JsonDeserializer<Identifier>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Identifier {
        val value = p.readValueAs(String::class.java)
        return Identifier(value)
    }
}
