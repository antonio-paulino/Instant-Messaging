package model.invitation

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

enum class ChannelInvitationStatus {
    PENDING, ACCEPTED, REJECTED
}

@Converter(autoApply = true)
class ChannelInvitationStatusConverter : AttributeConverter<ChannelInvitationStatus, String> {
    override fun convertToDatabaseColumn(attribute: ChannelInvitationStatus?): String {
        return attribute?.name ?: ""
    }

    override fun convertToEntityAttribute(dbData: String?): ChannelInvitationStatus {
        return dbData?.let { ChannelInvitationStatus.valueOf(it) } ?: ChannelInvitationStatus.PENDING
    }
}