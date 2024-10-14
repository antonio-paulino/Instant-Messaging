package im.repository.jpa.model.invitation

import im.domain.invitations.ChannelInvitationStatus
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

enum class ChannelInvitationStatusDTO {
    PENDING,
    ACCEPTED,
    REJECTED,
    ;

    companion object {
        fun fromDomain(status: ChannelInvitationStatus): ChannelInvitationStatusDTO = valueOf(status.name)
    }

    fun toDomain(): ChannelInvitationStatus =
        ChannelInvitationStatus
            .valueOf(this.name)
}

@Converter(autoApply = true)
class ChannelInvitationStatusConverter : AttributeConverter<ChannelInvitationStatusDTO, String> {
    override fun convertToDatabaseColumn(attribute: ChannelInvitationStatusDTO?): String = attribute?.name ?: ""

    override fun convertToEntityAttribute(dbData: String?): ChannelInvitationStatusDTO =
        dbData?.let {
            ChannelInvitationStatusDTO.valueOf(it)
        } ?: ChannelInvitationStatusDTO.PENDING
}
