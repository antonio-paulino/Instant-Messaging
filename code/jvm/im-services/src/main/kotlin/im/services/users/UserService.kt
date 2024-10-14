package im.services.users

import im.domain.user.User
import im.domain.wrappers.Identifier
import im.repository.pagination.Pagination
import im.repository.pagination.PaginationRequest
import im.repository.pagination.SortRequest
import im.services.Either

interface UserService {
    /**
     * Retrieves a user by their identifier.
     *
     * @param id the user identifier
     * @return the user if it exists, or an [UserError] otherwise
     */
    fun getUserById(id: Identifier): Either<UserError, User>

    /**
     * Retrieves a list of users.
     *
     * @param name the name to search for
     * @param pagination the pagination request
     * @param sortRequest the sort request
     * @return a [Pagination] of users if the search is successful, or an [UserError] otherwise
     */
    fun getUsers(
        name: String?,
        pagination: PaginationRequest,
        sortRequest: SortRequest,
    ): Either<UserError, Pagination<User>>
}
