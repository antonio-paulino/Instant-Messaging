package im.repositories

import im.pagination.Pagination
import im.channel.Channel
import im.channel.ChannelRole
import im.invitations.ChannelInvitation
import im.model.user.UserDTO
import im.pagination.PaginationRequest
import im.repositories.jpa.UserRepositoryJpa
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Component
import im.sessions.Session
import im.user.User
import im.repositories.user.UserRepository
import im.wrappers.Email
import im.wrappers.Identifier
import im.wrappers.Name
import im.wrappers.Password

@Component
class UserRepositoryImpl(
    private val userRepositoryJpa: UserRepositoryJpa,
    private val entityManager: EntityManager,
    private val utils: JpaRepositoryUtils
) : UserRepository {

    override fun findByName(name: Name): User? {
        return userRepositoryJpa.findByName(name.value).firstOrNull()?.toDomain()
    }

    override fun findByEmail(email: Email): User? {
        return userRepositoryJpa.findByEmail(email.value).firstOrNull()?.toDomain()
    }

    override fun findByPartialName(name: String, pagination: PaginationRequest): Pagination<User> {
        val res = userRepositoryJpa.findByPartialName(name, utils.toPageRequest(pagination, "id"))
        return Pagination(res.content.map { it.toDomain() }, utils.getPaginationInfo(res))
    }

    override fun findByNameAndPassword(name: Name, password: Password): User? {
        return userRepositoryJpa.findByNameAndPassword(name.value, password.value).firstOrNull()?.toDomain()
    }

    override fun findByEmailAndPassword(email: Email, password: Password): User? {
        return userRepositoryJpa.findByEmailAndPassword(email.value, password.value).firstOrNull()?.toDomain()
    }

    override fun getOwnedChannels(user: User): List<Channel> {
        return userRepositoryJpa.getOwnedChannels(user.id.value).map { it.toDomain() }
    }

    override fun getJoinedChannels(user: User): Map<Channel, ChannelRole> {
        return userRepositoryJpa.getJoinedChannels(user.id.value)
            .associate { it.channel.toDomain() to it.role.toDomain() }
    }

    override fun getInvitations(user: User): List<ChannelInvitation> {
        return userRepositoryJpa.getInvitations(user.id.value).map { it.toDomain() }
    }

    override fun getSessions(user: User): List<Session> {
        return userRepositoryJpa.getSessions(user.id.value).map { it.toDomain() }
    }

    override fun save(entity: User): User {
        return userRepositoryJpa.save(UserDTO.fromDomain(entity)).toDomain()
    }

    override fun saveAll(entities: Iterable<User>): List<User> {
        return userRepositoryJpa.saveAll(entities.map { UserDTO.fromDomain(it) }).map { it.toDomain() }
    }

    override fun findById(id: Identifier): User? {
        return userRepositoryJpa.findById(id.value).map { it.toDomain() }.orElse(null)
    }

    override fun findAll(): List<User> {
        return userRepositoryJpa.findAll().map { it.toDomain() }
    }

    override fun find(pagination: PaginationRequest): Pagination<User> {
        val res = userRepositoryJpa.findAll(utils.toPageRequest(pagination, "id"))
        return Pagination(res.content.map { it.toDomain() }, utils.getPaginationInfo(res))
    }

    override fun findAllById(ids: Iterable<Identifier>): List<User> {
        return userRepositoryJpa.findAllById(ids.map { it.value }).map { it.toDomain() }
    }

    override fun deleteById(id: Identifier) {
        userRepositoryJpa.deleteById(id.value)
    }

    override fun existsById(id: Identifier): Boolean {
        return userRepositoryJpa.existsById(id.value)
    }

    override fun count(): Long {
        return userRepositoryJpa.count()
    }

    override fun deleteAll() {
        userRepositoryJpa.deleteAll()
    }

    override fun deleteAll(entities: Iterable<User>) {
        userRepositoryJpa.deleteAll(entities.map { UserDTO.fromDomain(it) })
    }

    override fun delete(entity: User) {
        userRepositoryJpa.delete(UserDTO.fromDomain(entity))
    }

    override fun deleteAllById(ids: Iterable<Identifier>) {
        userRepositoryJpa.deleteAllById(ids.map { it.value })
    }

    override fun flush() {
        entityManager.flush()
        userRepositoryJpa.flush()
    }

}