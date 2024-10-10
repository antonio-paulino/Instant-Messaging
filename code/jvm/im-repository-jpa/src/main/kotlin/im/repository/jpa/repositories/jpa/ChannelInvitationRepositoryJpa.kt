package im.repository.jpa.repositories.jpa

import im.invitations.ChannelInvitationStatus
import im.repository.jpa.model.invitation.ChannelInvitationDTO
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ChannelInvitationRepositoryJpa : JpaRepository<ChannelInvitationDTO, Long> {
    @Query("SELECT i FROM ChannelInvitationDTO i WHERE i.channel.id = :channelId AND i.status = :status")
    fun findByChannel(channelId: Long, status: ChannelInvitationStatus): List<ChannelInvitationDTO>

    @Query("SELECT i FROM ChannelInvitationDTO i WHERE i.invitee.id = :userId")
    fun findByInvitee(userId: Long): List<ChannelInvitationDTO>
}