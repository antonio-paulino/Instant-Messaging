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

    @OneToMany(mappedBy = "id", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val messages: List<MessageDTO> = mutableListOf(),

    @OneToMany(mappedBy = "id", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val members: List<UserDTO> = mutableListOf(),

    @OneToMany(mappedBy = "id", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val invitations: List<ChannelInvitationDTO> = mutableListOf()
) {
    companion object {
        fun fromDomain(channel: Channel): ChannelDTO = ChannelDTO(
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

    fun toDomain(): Channel = Channel(
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
