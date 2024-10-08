package im.model.channel

import im.channel.Channel
import im.model.user.UserDTO
import jakarta.persistence.Entity
import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.time.LocalDateTime

@Entity
@Table(name = "channel")
open class ChannelDTO(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open val id: Long = 0,

    @Column(nullable = false, length = 30)
    open val name: String = "",

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
        fun fromDomain(channel: Channel): ChannelDTO = ChannelDTO(
            id = channel.id,
            name = channel.name,
            owner = UserDTO.fromDomain(channel.owner),
            isPublic = channel.isPublic,
            createdAt = channel.createdAt,
            members = channel.members
                .mapKeys { UserDTO.fromDomain(it.key) }
                .mapValues { ChannelRoleDTO.valueOf(it.value.name) },
        )
    }

    fun toDomain(): Channel = Channel(
        id = id,
        name = name,
        owner = owner!!.toDomain(),
        isPublic = isPublic,
        createdAt = createdAt,
        membersLazy = lazy {
            members
                .mapKeys { it.key.toDomain() }
                .mapValues { it.value.toDomain() }
        }
    )
}
