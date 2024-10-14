package im.api.model.output.channel

import im.api.model.output.PaginationOutputModel
import im.domain.channel.Channel
import im.repository.pagination.Pagination

data class ChannelsPaginatedOutputModel(
    val channels: List<ChannelOutputModel>,
    val pagination: PaginationOutputModel?,
) {
    companion object {
        fun fromDomain(channels: Pagination<Channel>): ChannelsPaginatedOutputModel =
            ChannelsPaginatedOutputModel(
                channels =
                    channels.items.map {
                        ChannelOutputModel
                            .fromDomain(it)
                    },
                pagination = PaginationOutputModel.fromPagination(channels.info),
            )
    }
}
