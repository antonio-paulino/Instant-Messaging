package im.api.model.output.channel

import im.domain.channel.Channel

data class ChannelsOutputModel(
    val channels: List<ChannelOutputModel>,
) {
    companion object {
        fun fromDomain(channels: List<Channel>) =
            ChannelsOutputModel(
                channels = channels.map { ChannelOutputModel.fromDomain(it) },
            )
    }
}
