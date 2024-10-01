package model.invitation

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

enum class ImInvitationStatus {
    PENDING,
    USED,
}

@Converter(autoApply = true)
class ImInvitationStatusConverter : AttributeConverter<ImInvitationStatus, String> {
    override fun convertToDatabaseColumn(attribute: ImInvitationStatus?): String {
        return attribute?.name ?: ""
    }

    override fun convertToEntityAttribute(dbData: String?): ImInvitationStatus {
        return dbData?.let { ImInvitationStatus.valueOf(it) } ?: ImInvitationStatus.PENDING
    }
}