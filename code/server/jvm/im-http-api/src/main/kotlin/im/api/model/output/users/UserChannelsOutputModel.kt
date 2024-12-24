package im.api.model.output.users

import im.api.model.output.channel.ChannelOutputModel
import im.domain.channel.Channel
import im.domain.channel.ChannelRole

data class UserChannelsOutputModel(
    val ownedChannels: List<ChannelOutputModel>,
    val memberChannels: List<ChannelOutputModel>,
) {
    companion object {
        fun fromUserChannels(channels: Map<Channel, ChannelRole>) =
            UserChannelsOutputModel(
                ownedChannels =
                    channels
                        .filter { it.value == ChannelRole.OWNER }
                        .map { ChannelOutputModel.fromDomain(it.key) },
                memberChannels =
                    channels
                        .filter { it.value == ChannelRole.MEMBER || it.value == ChannelRole.GUEST }
                        .map { ChannelOutputModel.fromDomain(it.key) },
            )
    }
}
