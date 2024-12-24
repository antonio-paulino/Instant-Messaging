package im.repository.jpa.model.channel

import im.domain.channel.Channel
import im.domain.wrappers.identifier.toIdentifier
import im.domain.wrappers.name.toName
import im.repository.jpa.model.user.UserDTO
import im.repository.jpa.repositories.jpa.channels.ChannelRepositoryListenerJpa
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.MapKeyJoinColumn
import jakarta.persistence.Table
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.time.LocalDateTime

/**
 * Represents a channel in the database.
 *
 * - A channel has one owner, and an owner can have own many channels (many-to-one relationship).
 * - A channel can have many messages, and each message is associated to a single channel (one-to-many relationship).
 * - A channel can have many invitations, and each invitation is associated to a single channel (one-to-many relationship).
 * - A channel can have many members, and each member is associated to a single channel (many-to-many relationship).
 *
 * @property id The unique identifier of the channel.
 * @property name The name of the channel.
 * @property owner The user that created the channel.
 * @property isPublic Indicates if the channel is public.
 * @property createdAt The date and time when the channel was created.
 * @property members The members of the channel and their roles.
 */
@Entity
@EntityListeners(ChannelRepositoryListenerJpa::class)
@Table(name = "channel")
open class ChannelDTO(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open val id: Long = 0,
    @Column(nullable = false, length = 30)
    open val name: String = "",
    @Column(nullable = false)
    open val defaultRole: ChannelRoleDTO = ChannelRoleDTO.MEMBER,
    @ManyToOne
    @JoinColumn(name = "owner", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    open val owner: UserDTO? = null,
    @Column(name = "is_public", nullable = false)
    open val isPublic: Boolean = true,
    @Column(name = "created_at", nullable = false)
    open val createdAt: LocalDateTime = LocalDateTime.now(),
    @ElementCollection
    @CollectionTable(
        name = "channel_member",
        joinColumns = [JoinColumn(name = "channel_id")],
    )
    @MapKeyJoinColumn(name = "user_id")
    @Column(name = "role")
    open val members: Map<UserDTO, ChannelRoleDTO> = hashMapOf(),
) {
    companion object {
        fun fromDomain(channel: Channel): ChannelDTO =
            ChannelDTO(
                id = channel.id.value,
                name = channel.name.value,
                defaultRole = ChannelRoleDTO.fromDomain(channel.defaultRole),
                owner = UserDTO.fromDomain(channel.owner),
                isPublic = channel.isPublic,
                createdAt = channel.createdAt,
                members =
                    channel.members
                        .mapKeys { UserDTO.fromDomain(it.key) }
                        .mapValues { ChannelRoleDTO.valueOf(it.value.name) },
            )
    }

    fun toDomain(): Channel =
        Channel(
            id = id.toIdentifier(),
            name = name.toName(),
            defaultRole = defaultRole.toDomain(),
            owner = owner!!.toDomain(),
            isPublic = isPublic,
            createdAt = createdAt,
            membersLazy =
                lazy {
                    members
                        .mapKeys { it.key.toDomain() }
                        .mapValues { it.value.toDomain() }
                },
        )
}
