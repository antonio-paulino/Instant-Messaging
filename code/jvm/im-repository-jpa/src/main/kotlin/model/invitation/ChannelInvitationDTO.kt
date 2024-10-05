package model.invitation

import invitations.ChannelInvitation
import invitations.ChannelInvitationStatus
import channel.ChannelRole
import jakarta.persistence.*
import model.channel.ChannelDTO
import model.user.UserDTO
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.time.LocalDateTime

@Entity
@Table(name = "channel_invitation")
data class ChannelInvitationDTO(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    val channel: ChannelDTO? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inviter", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    val inviter: UserDTO? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invitee", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    val invitee: UserDTO? = null,

    @Column(nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    val status: ChannelInvitationStatus = ChannelInvitationStatus.PENDING,

    @Column(nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    val role: ChannelRole = ChannelRole.MEMBER,

    @Column(name = "expires_at", nullable = false)
    val expiresAt: LocalDateTime = LocalDateTime.now().plusDays(7),
) {
    companion object {
        fun fromDomain(invitation: ChannelInvitation): ChannelInvitationDTO = ChannelInvitationDTO(
            id = invitation.id,
            channel = ChannelDTO.fromDomain(invitation.channel),
            inviter = UserDTO.fromDomain(invitation.inviter),
            invitee = UserDTO.fromDomain(invitation.invitee),
            status = invitation.status,
            role = invitation.role,
            expiresAt = invitation.expiresAt
        )
    }

    fun toDomain(): ChannelInvitation = ChannelInvitation(
        id = id,
        channel = channel!!.toDomain(),
        inviter = inviter!!.toDomain(),
        invitee = invitee!!.toDomain(),
        status = status,
        role = role,
        expiresAt = expiresAt
    )
}