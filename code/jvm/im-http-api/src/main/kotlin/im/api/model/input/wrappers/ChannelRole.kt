package im.api.model.input.wrappers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import im.api.model.input.validators.domain.Role

/**
 * Wrapper for channel role.
 *
 * @property value The channel role.
 */
@JsonDeserialize(using = ChannelRoleDeserializer::class)
@JsonSerialize(using = ChannelRoleSerializer::class) // For testing purposes
data class ChannelRole(
    @field:Role
    val value: String,
) {
    fun toDomain() = im.domain.channel.ChannelRole.valueOf(value)
}

/**
 * Deserializer for [ChannelRole].
 *
 * Converts a key-value pair directly to a [ChannelRole] object, without having to define a
 * JSON object with a `value` field.
 */
class ChannelRoleDeserializer : JsonDeserializer<ChannelRole>() {
    override fun deserialize(
        p: JsonParser,
        ctxt: DeserializationContext,
    ): ChannelRole = ChannelRole(p.text)
}

/**
 * Serializer for [ChannelRole].
 *
 * Converts a [ChannelRole] object directly to a key-value pair, without having to define a
 * JSON object with a `value` field.
 */
class ChannelRoleSerializer : JsonSerializer<ChannelRole>() {
    override fun serialize(
        value: ChannelRole,
        gen: JsonGenerator,
        serializers: SerializerProvider,
    ) {
        gen.writeString(value.value)
    }
}
