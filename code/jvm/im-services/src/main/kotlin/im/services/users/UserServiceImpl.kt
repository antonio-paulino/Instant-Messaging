package im.services.users

import im.domain.user.User
import im.domain.wrappers.Identifier
import im.repository.pagination.Pagination
import im.repository.pagination.PaginationRequest
import im.repository.pagination.SortRequest
import im.repository.repositories.transactions.TransactionManager
import im.services.Either
import im.services.failure
import im.services.success
import jakarta.inject.Named

@Named
class UserServiceImpl(
    private val transactionManager: TransactionManager,
) : UserService {
    companion object {
        private const val DEFAULT_SORT = "id"
        private val validSortFields = setOf("name", "email", "id")
    }

    override fun getUserById(id: Identifier): Either<UserError, User> =
        transactionManager.run {
            val user =
                userRepository.findById(id)
                    ?: return@run failure(UserError.UserNotFound)
            success(user)
        }

    override fun getUsers(
        name: String?,
        pagination: PaginationRequest,
        sortRequest: SortRequest,
    ): Either<UserError, Pagination<User>> =
        transactionManager.run {
            val sort = sortRequest.sortBy ?: DEFAULT_SORT

            if (sort !in validSortFields) {
                return@run failure(UserError.InvalidSortField(sort, validSortFields.toList()))
            }

            val users =
                if (name != null) {
                    userRepository.findByPartialName(name, pagination, sortRequest.copy(sortBy = sort))
                } else {
                    userRepository.find(pagination, sortRequest.copy(sortBy = sort))
                }
            success(users)
        }
}
