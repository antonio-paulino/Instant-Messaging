package im.api.model.output.users

import im.api.model.output.PaginationOutputModel
import im.domain.user.User
import im.repository.pagination.Pagination

data class UsersPaginatedOutputModel(
    val users: List<UserOutputModel>,
    val pagination: PaginationOutputModel?,
) {
    companion object {
        fun fromDomain(users: Pagination<User>): UsersPaginatedOutputModel =
            UsersPaginatedOutputModel(
                users = users.items.map { UserOutputModel.fromDomain(it) },
                pagination = PaginationOutputModel.fromPagination(users.info),
            )
    }
}
