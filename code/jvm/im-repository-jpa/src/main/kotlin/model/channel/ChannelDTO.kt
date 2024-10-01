package model.channel

import channel.Channel
import jakarta.persistence.Entity

import jakarta.persistence.*
import model.invitation.ChannelInvitationDTO
import model.message.MessageDTO
import model.user.UserDTO
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.time.LocalDateTime

@Entity
@Table(name = "channel")
data class ChannelDTO(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, length = 30)
    val name: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    val owner: UserDTO? = null,

    @Column(name = "is_public", nullable = false)
    val isPublic: Boolean = true,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "ChannelMember",
        joinColumns = [JoinColumn(name = "channel_id")],
        inverseJoinColumns = [JoinColumn(name = "user_id")]
    )
    val members: List<UserDTO> = emptyList(),

    @OneToMany(mappedBy = "id", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val messages: List<MessageDTO> = emptyList(),

    @OneToMany(mappedBy = "id", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val invitations: List<ChannelInvitationDTO> = emptyList()
) {
    companion object {
        fun fromDomain(channel: Channel) = ChannelDTO(
            id = channel.id,
            name = channel.name,
            owner = UserDTO.fromDomain(channel.owner),
            isPublic = channel.isPublic,
            createdAt = channel.createdAt,
            members = channel.members.map { UserDTO.fromDomain(it) },
            messages = channel.messages.map { MessageDTO.fromDomain(it) },
            invitations = channel.invitations.map { ChannelInvitationDTO.fromDomain(it) }
        )
    }

    fun toDomain() = Channel(
        id = id,
        name = name,
        owner = owner!!.toDomain(),
        isPublic = isPublic,
        createdAt = createdAt,
        members = members.map { it.toDomain() },
        messages = messages.map { it.toDomain() },
        invitations = invitations.map { it.toDomain() }
    )
}
