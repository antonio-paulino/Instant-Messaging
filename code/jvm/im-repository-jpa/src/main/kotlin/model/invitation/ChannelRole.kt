package model.invitation

import invitations.ChannelRole
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

enum class ChannelRole {
    OWNER,
    MEMBER, // read-write
    GUEST // read-only
}

@Converter(autoApply = true)
class ChannelRoleConverter : AttributeConverter<ChannelRole, String> {
    override fun convertToDatabaseColumn(attribute: ChannelRole?): String {
        return attribute?.name ?: ""
    }

    override fun convertToEntityAttribute(dbData: String?): ChannelRole {
        return dbData?.let { ChannelRole.valueOf(it) } ?: ChannelRole.MEMBER
    }
}