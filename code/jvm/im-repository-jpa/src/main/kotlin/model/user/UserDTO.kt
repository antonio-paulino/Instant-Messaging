package model.user


import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToMany
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.persistence.*
import model.channel.ChannelDTO
import model.invitation.ChannelInvitationDTO
import model.session.SessionDTO
import user.User

@Entity
@Table(name = "\"user\"")
data class UserDTO(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(unique = true, nullable = false, length = 30)
    val name: String = "",

    @Column(nullable = false, length = 100)
    val password: String = "",

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val sessions: List<SessionDTO> = emptyList(),

    @OneToMany(mappedBy = "owner", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val ownedChannels: List<ChannelDTO> = emptyList(),

    @ManyToMany(mappedBy = "members", fetch = FetchType.LAZY)
    val joinedChannels: List<ChannelDTO> = emptyList(),

    @OneToMany(mappedBy = "inviter", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val sentInvitations: List<ChannelInvitationDTO> = emptyList(),

    @OneToMany(mappedBy = "invitee", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val receivedInvitations: List<ChannelInvitationDTO> = emptyList()
) {
    companion object {
        fun fromDomain(user: User): UserDTO = UserDTO(
            id = user.id,
            name = user.name,
            password = user.password,
            sessions = user.sessions.map { SessionDTO.fromDomain(it) },
            ownedChannels = user.ownedChannels.map { ChannelDTO.fromDomain(it) },
            joinedChannels = user.joinedChannels.map { ChannelDTO.fromDomain(it) },
            sentInvitations = user.sentInvitations.map { ChannelInvitationDTO.fromDomain(it) },
            receivedInvitations = user.receivedInvitations.map { ChannelInvitationDTO.fromDomain(it) }
        )
    }

    fun toDomain(): User = User(
        id = id,
        name = name,
        password = password,
        sessions = sessions.map { it.toDomain() },
        ownedChannels = ownedChannels.map { it.toDomain() },
        joinedChannels = joinedChannels.map { it.toDomain() },
        sentInvitations = sentInvitations.map { it.toDomain() },
        receivedInvitations = receivedInvitations.map { it.toDomain() }
    )
}
