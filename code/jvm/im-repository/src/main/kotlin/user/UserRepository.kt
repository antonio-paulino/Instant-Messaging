package user

import Repository

interface UserRepository : Repository<User, Long> {
    fun findByName(name: String): User?
    fun findByPartialName(name: String): List<User>
    fun findByNameAndPassword(name: String, password: String): User?
}