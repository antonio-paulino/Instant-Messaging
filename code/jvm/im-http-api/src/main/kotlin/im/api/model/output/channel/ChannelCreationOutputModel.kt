package im.api.model.output.channel

import im.domain.channel.Channel

data class ChannelCreationOutputModel(
    val id: Long,
    val createdAt: String,
) {
    companion object {
        fun fromDomain(channel: Channel): ChannelCreationOutputModel =
            ChannelCreationOutputModel(
                id = channel.id.value,
                createdAt = channel.createdAt.toString(),
            )
    }
}
