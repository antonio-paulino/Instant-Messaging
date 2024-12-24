package im.repository.repositories.user

import im.domain.user.User
import im.domain.wrappers.email.Email
import im.domain.wrappers.identifier.Identifier
import im.domain.wrappers.name.Name
import im.domain.wrappers.password.Password
import im.repository.pagination.Pagination
import im.repository.pagination.PaginationRequest
import im.repository.pagination.SortRequest
import im.repository.repositories.Repository

/**
 * [Repository] for [User] entities.
 */
interface UserRepository : Repository<User, Identifier> {
    /**
     * Finds a user by their name.
     *
     * @param name the name of the user
     * @return the user with the given name, or `null` if no such user exists
     */
    fun findByName(name: Name): User?

    /**
     * Finds a user by their email.
     *
     * @param email the email of the user
     * @return the user with the given email, or `null` if no such user exists
     */
    fun findByEmail(email: Email): User?

    /**
     * Finds users whose name starts with the given string, case-insensitive.
     *
     * @param name the partial name of the users
     * @return the users whose name contains the given string
     */
    fun findByPartialName(
        name: String,
        pagination: PaginationRequest,
        sortRequest: SortRequest,
    ): Pagination<User>

    /**
     * Finds a user by their name and password.
     *
     * @param name the name of the user
     * @param password the password of the user
     * @return the user with the given name and password, or `null` if no such user exists
     */
    fun findByNameAndPassword(
        name: Name,
        password: Password,
    ): User?

    /**
     * Finds a user by their email and password.
     *
     * @param email the email of the user
     * @param password the password of the user
     * @return the user with the given email and password, or `null` if no such user exists
     */
    fun findByEmailAndPassword(
        email: Email,
        password: Password,
    ): User?
}
