package im.repository.mem.model.channel

import im.domain.channel.Channel
import im.domain.wrappers.identifier.toIdentifier
import im.domain.wrappers.name.toName
import im.repository.mem.model.user.UserDTO
import java.time.LocalDateTime

data class ChannelDTO(
    val id: Long = 0,
    val name: String,
    val owner: UserDTO,
    val defaultRole: ChannelRoleDTO = ChannelRoleDTO.MEMBER,
    val isPublic: Boolean,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val members: Map<UserDTO, ChannelRoleDTO> = hashMapOf(),
) {
    companion object {
        fun fromDomain(channel: Channel): ChannelDTO =
            ChannelDTO(
                id = channel.id.value,
                name = channel.name.value,
                owner = UserDTO.fromDomain(channel.owner),
                defaultRole = ChannelRoleDTO.valueOf(channel.defaultRole.name),
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
            owner = owner.toDomain(),
            defaultRole = ChannelRoleDTO.valueOf(defaultRole.name).toDomain(),
            isPublic = isPublic,
            createdAt = createdAt,
            membersLazy =
                lazy {
                    members
                        .mapKeys { it.key.toDomain() }
                        .mapValues { ChannelRoleDTO.valueOf(it.value.name).toDomain() }
                },
        )
}
