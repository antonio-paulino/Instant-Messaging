package im.repository.jpa.repositories.jpa.invitations

import im.repository.jpa.model.invitation.ChannelInvitationDTO
import im.repository.jpa.model.invitation.ChannelInvitationStatusDTO
import org.springframework.data.domain.Page
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

    @Query(
        """
        SELECT ci
        FROM ChannelInvitationDTO ci
        WHERE ci.invitee.id = :userId
        AND ci.channel.id = :channelId
        AND ci.status = 'PENDING'
        AND ci.expiresAt > CURRENT_TIMESTAMP
        """,
    )
    fun findByInviteeIdAndChannelId(
        userId: Long,
        channelId: Long,
    ): ChannelInvitationDTO?

    @Query(
        """
        SELECT ci
        FROM ChannelInvitationDTO ci
        WHERE ci.channel.id = :channelId
        AND ci.status = :status
        AND ci.expiresAt > CURRENT_TIMESTAMP
        AND ci.id > :after
        """,
    )
    fun findByChannelIdAndStatus(
        channelId: Long,
        status: ChannelInvitationStatusDTO,
        pagination: Pageable,
        after: Long,
    ): Page<ChannelInvitationDTO>

    @Query(
        """
        SELECT ci
        FROM ChannelInvitationDTO ci
        WHERE ci.channel.id = :channelId
        AND ci.status = :status
        AND ci.expiresAt > CURRENT_TIMESTAMP
        AND ci.id > :after
        """,
    )
    fun findByChannelIdAndStatusSliced(
        channelId: Long,
        status: ChannelInvitationStatusDTO,
        pagination: Pageable,
        after: Long,
    ): Slice<ChannelInvitationDTO>

    @Query(
        """
        SELECT ci
        FROM ChannelInvitationDTO ci
        WHERE ci.invitee.id = :userId
        AND ci.status = :status
        AND ci.expiresAt > CURRENT_TIMESTAMP
        AND ci.id > :after
        """,
    )
    fun findByInviteeSliced(
        userId: Long,
        status: ChannelInvitationStatusDTO,
        pagination: Pageable,
        after: Long,
    ): Slice<ChannelInvitationDTO>

    @Query(
        """
        SELECT ci
        FROM ChannelInvitationDTO ci
        WHERE ci.invitee.id = :userId
        AND ci.status = :status
        AND ci.expiresAt > CURRENT_TIMESTAMP
        AND ci.id > :after
        """,
    )
    fun findByInvitee(
        userId: Long,
        status: ChannelInvitationStatusDTO,
        pagination: Pageable,
        after: Long,
    ): Page<ChannelInvitationDTO>

    fun findBy(pageable: Pageable): Slice<ChannelInvitationDTO>
}
