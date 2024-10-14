package im.api.model.input.wrappers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import im.api.model.input.validators.Role

@JsonDeserialize(using = ChannelRoleDeserializer::class)
@JsonSerialize(using = ChannelRoleSerializer::class) // For testing purposes
data class ChannelRole(
    @field:Role
    val value: String,
) {
    fun toDomain() = im.domain.channel.ChannelRole.valueOf(value)
}

class ChannelRoleDeserializer : JsonDeserializer<ChannelRole>() {
    override fun deserialize(
        p: JsonParser,
        ctxt: DeserializationContext,
    ): ChannelRole {
        return ChannelRole(p.text)
    }
}

class ChannelRoleSerializer : JsonSerializer<ChannelRole>() {
    override fun serialize(
        value: ChannelRole,
        gen: JsonGenerator,
        serializers: SerializerProvider,
    ) {
        gen.writeString(value.value)
    }
}
