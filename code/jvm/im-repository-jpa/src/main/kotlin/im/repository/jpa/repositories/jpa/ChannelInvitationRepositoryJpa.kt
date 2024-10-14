package im.repository.jpa.repositories.jpa

import im.repository.jpa.model.invitation.ChannelInvitationDTO
import im.repository.jpa.model.invitation.ChannelInvitationStatusDTO
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Slice
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface ChannelInvitationRepositoryJpa : JpaRepository<ChannelInvitationDTO, Long> {
    fun deleteAllByExpiresAtIsBeforeOrStatusIn(
        expiresAt: LocalDateTime = LocalDateTime.now(),
        status: List<ChannelInvitationStatusDTO> =
            listOf(
                ChannelInvitationStatusDTO.ACCEPTED,
                ChannelInvitationStatusDTO.REJECTED,
            ),
    )

    fun findByInviteeIdAndChannelId(
        userId: Long,
        channelId: Long,
    ): ChannelInvitationDTO?

    fun findByChannelIdAndStatus(
        channelId: Long,
        status: ChannelInvitationStatusDTO,
        sort: Sort,
    ): List<ChannelInvitationDTO>

    fun findByInviteeId(
        userId: Long,
        sort: Sort,
    ): List<ChannelInvitationDTO>

    fun findBy(pageable: PageRequest): Slice<ChannelInvitationDTO>
}
