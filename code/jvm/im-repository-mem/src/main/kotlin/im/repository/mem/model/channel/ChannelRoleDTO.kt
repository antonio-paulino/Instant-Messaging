package im.repository.mem.model.channel

import im.channel.ChannelRole

enum class ChannelRoleDTO {
    OWNER,
    MEMBER,
    GUEST;

    companion object {
        fun fromDomain(role: ChannelRole): ChannelRoleDTO {
            return when (role) {
                ChannelRole.OWNER -> OWNER
                ChannelRole.MEMBER -> MEMBER
                ChannelRole.GUEST -> GUEST
            }
        }
    }

    fun toDomain(): ChannelRole {
        return when (this) {
            OWNER -> ChannelRole.OWNER
            MEMBER -> ChannelRole.MEMBER
            GUEST -> ChannelRole.GUEST
        }
    }
}