package model.channel

import channel.ChannelRole
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

enum class ChannelRoleDTO {
    OWNER,
    MEMBER,
    GUEST;

    companion object {
        fun fromDomain(role: ChannelRole): ChannelRoleDTO {
            return when (role) {
                ChannelRole.OWNER -> OWNER
                ChannelRole.MEMBER -> MEMBER
                ChannelRole.GUEST -> GUEST
            }
        }
    }

    fun toDomain(): ChannelRole {
        return when (this) {
            OWNER -> ChannelRole.OWNER
            MEMBER -> ChannelRole.MEMBER
            GUEST -> ChannelRole.GUEST
        }
    }
}

@Converter(autoApply = true)
class ChannelRoleConverter : AttributeConverter<ChannelRoleDTO, String> {
    override fun convertToDatabaseColumn(attribute: ChannelRoleDTO?): String {
        return attribute?.name ?: ""
    }

    override fun convertToEntityAttribute(dbData: String?): ChannelRoleDTO {
        return dbData?.let { ChannelRoleDTO.valueOf(it) } ?: ChannelRoleDTO.MEMBER
    }
}