package im.api.model.output.messages

import im.api.model.output.PaginationOutputModel
import im.domain.messages.Message
import im.repository.pagination.PaginationInfo

data class MessagesPaginatedOutputModel(
    val messages: List<MessageOutputModel>,
    val pagination: PaginationOutputModel,
) {
    companion object {
        fun fromMessages(
            messages: List<Message>,
            pagination: PaginationInfo,
        ): MessagesPaginatedOutputModel =
            MessagesPaginatedOutputModel(
                messages.map { MessageOutputModel.fromDomain(it) },
                PaginationOutputModel.fromPagination(pagination),
            )
    }
}
