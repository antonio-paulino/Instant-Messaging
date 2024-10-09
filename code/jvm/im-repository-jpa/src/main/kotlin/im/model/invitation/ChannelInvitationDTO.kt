package im.model.invitation

import im.invitations.ChannelInvitation
import im.invitations.ChannelInvitationStatus
import im.channel.ChannelRole
import im.model.channel.ChannelDTO
import im.model.user.UserDTO
import im.wrappers.toIdentifier
import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.time.LocalDateTime

@Entity
@Table(name = "channel_invitation")
@NamedQuery(
    name = "ChannelInvitationDTO.findByChannel",
    query = "SELECT i FROM ChannelInvitationDTO i WHERE i.channel.id = :channelId and i.status = :status"
)
open class ChannelInvitationDTO(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open val id: Long = 0,

    @ManyToOne
    @JoinColumn(name = "channel_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    open val channel: ChannelDTO? = null,

    @ManyToOne
    @JoinColumn(name = "inviter", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    open val inviter: UserDTO? = null,

    @ManyToOne
    @JoinColumn(name = "invitee", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    open val invitee: UserDTO? = null,

    @Column(nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    open val status: ChannelInvitationStatus = ChannelInvitationStatus.PENDING,

    @Column(nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    open val role: ChannelRole = ChannelRole.MEMBER,

    @Column(name = "expires_at", nullable = false)
    open val expiresAt: LocalDateTime = LocalDateTime.now().plusDays(7),
) {
    companion object {
        fun fromDomain(invitation: ChannelInvitation): ChannelInvitationDTO =
            ChannelInvitationDTO(
                id = invitation.id.value,
                channel = ChannelDTO.fromDomain(invitation.channel),
                inviter = UserDTO.fromDomain(invitation.inviter),
                invitee = UserDTO.fromDomain(invitation.invitee),
                status = invitation.status,
                role = invitation.role,
                expiresAt = invitation.expiresAt
            )
    }

    fun toDomain(): ChannelInvitation = ChannelInvitation(
        id = id.toIdentifier(),
        channel = channel!!.toDomain(),
        inviter = inviter!!.toDomain(),
        invitee = invitee!!.toDomain(),
        status = status,
        role = role,
        expiresAt = expiresAt
    )
}