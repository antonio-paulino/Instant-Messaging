package im.api.model.output.channel

import im.api.model.output.users.UserOutputModel
import im.domain.channel.Channel

data class ChannelOutputModel(
    val id: Long,
    val name: String,
    val owner: UserOutputModel,
    val defaultRole: String,
    val isPublic: Boolean,
    val members: List<ChannelMemberOutputModel>,
    val createdAt: String,
) {
    companion object {
        fun fromDomain(channel: Channel) =
            ChannelOutputModel(
                id = channel.id.value,
                name = channel.name.value,
                owner = UserOutputModel.fromDomain(channel.owner),
                defaultRole = channel.defaultRole.name,
                isPublic = channel.isPublic,
                members = channel.members.map { ChannelMemberOutputModel.fromDomain(it.key, it.value) },
                createdAt = channel.createdAt.toString(),
            )
    }
}
