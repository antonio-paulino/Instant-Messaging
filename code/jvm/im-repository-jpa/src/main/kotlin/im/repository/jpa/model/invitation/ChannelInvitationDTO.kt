package im.repository.jpa.model.invitation

import im.domain.channel.ChannelRole
import im.domain.invitations.ChannelInvitation
import im.domain.wrappers.identifier.toIdentifier
import im.repository.jpa.model.channel.ChannelDTO
import im.repository.jpa.model.user.UserDTO
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.time.LocalDateTime

/**
 * Represents a channel invitation in the database.
 *
 * - An invitation is associated to a single channel (many-to-one relationship).
 * - An invitation is associated to an inviter (many-to-one relationship).
 * - An invitation is associated to an invitee (many-to-one relationship).
 *
 * @property id The unique identifier of the channel invitation.
 * @property channel The channel that the invitation is for.
 * @property inviter The user that sent the invitation.
 * @property invitee The user that received the invitation.
 * @property status The status of the invitation.
 * @property role The role that the invitee will have in the channel.
 * @property expiresAt The date and time when the invitation expires.
 */
@Entity
@Table(name = "channel_invitation")
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
    open val status: ChannelInvitationStatusDTO = ChannelInvitationStatusDTO.PENDING,
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
                status = ChannelInvitationStatusDTO.fromDomain(invitation.status),
                role = invitation.role,
                expiresAt = invitation.expiresAt,
            )
    }

    fun toDomain(): ChannelInvitation =
        ChannelInvitation(
            id = id.toIdentifier(),
            channel = channel!!.toDomain(),
            inviter = inviter!!.toDomain(),
            invitee = invitee!!.toDomain(),
            status = status.toDomain(),
            role = role,
            expiresAt = expiresAt,
        )
}
