package im.repository.jpa.repositories.jpa

import im.repository.jpa.model.invitation.ChannelInvitationDTO
import im.repository.jpa.model.invitation.ChannelInvitationStatusDTO
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
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
        pagination: Pageable,
    ): Page<ChannelInvitationDTO>

    @Query(
        """
        SELECT ci
        FROM ChannelInvitationDTO ci
        WHERE ci.channel.id = :channelId
        AND ci.status = :status
        AND ci.expiresAt > CURRENT_TIMESTAMP
        """,
    )
    fun findByChannelIdAndStatusSliced(
        channelId: Long,
        status: ChannelInvitationStatusDTO,
        pagination: Pageable,
    ): Slice<ChannelInvitationDTO>

    @Query(
        """
        SELECT ci
        FROM ChannelInvitationDTO ci
        WHERE ci.invitee.id = :userId
        AND ci.status = :status
        AND ci.expiresAt > CURRENT_TIMESTAMP
        """,
    )
    fun findByInviteeIdSliced(
        userId: Long,
        status: ChannelInvitationStatusDTO,
        pagination: Pageable,
    ): Slice<ChannelInvitationDTO>

    @Query(
        """
        SELECT ci
        FROM ChannelInvitationDTO ci
        WHERE ci.invitee.id = :userId
        AND ci.status = :status
        AND ci.expiresAt > CURRENT_TIMESTAMP
        """,
    )
    fun findByInviteeId(
        userId: Long,
        status: ChannelInvitationStatusDTO,
        pagination: Pageable,
    ): Page<ChannelInvitationDTO>

    fun findBy(pageable: PageRequest): Slice<ChannelInvitationDTO>
}
