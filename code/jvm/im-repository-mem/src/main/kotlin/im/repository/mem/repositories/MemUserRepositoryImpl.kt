package im.repository.mem.repositories

import im.domain.user.User
import im.domain.wrappers.Email
import im.domain.wrappers.Name
import im.domain.wrappers.Password
import im.repository.mem.model.user.UserDTO
import im.repository.pagination.Pagination
import im.repository.pagination.PaginationRequest
import im.repository.pagination.SortRequest
import im.repository.repositories.user.UserRepository
import java.util.concurrent.ConcurrentHashMap

class MemUserRepositoryImpl(
    private val utils: MemRepoUtils,
    private val channelInvitationRepositoryImpl: MemChannelInvitationRepositoryImpl,
    private val channelRepositoryImpl: MemChannelRepositoryImpl,
    private val messageRepositoryImpl: MemMessageRepositoryImpl,
    private val sessionRepositoryImpl: MemSessionRepositoryImpl,
) : UserRepository {
    private val users = ConcurrentHashMap<Long, UserDTO>()
    private var id = 999L // Start from 1000 to avoid conflicts with users created in tests

    override fun findByName(name: Name): User? = users.values.firstOrNull { it.name == name.value }?.toDomain()

    override fun findByEmail(email: Email): User? = users.values.firstOrNull { it.email == email.value }?.toDomain()

    override fun findByPartialName(
        name: String,
        pagination: PaginationRequest,
        sortRequest: SortRequest,
    ): Pagination<User> {
        val page = utils.paginate(users.values.filter { it.name.startsWith(name) }, pagination, sortRequest)
        return Pagination(page.items.map { it.toDomain() }, page.info)
    }

    override fun findByNameAndPassword(
        name: Name,
        password: Password,
    ): User? = users.values.firstOrNull { it.name == name.value && it.password == password.value }?.toDomain()

    override fun findByEmailAndPassword(
        email: Email,
        password: Password,
    ): User? = users.values.firstOrNull { it.email == email.value && it.password == password.value }?.toDomain()

    override fun save(entity: User): User {
        val conflict = users.values.find { it.email == entity.email.value || it.name == entity.name.value }
        if (conflict != null && conflict.id != entity.id.value) {
            throw IllegalArgumentException("User with email ${entity.email} already exists")
        }
        if (conflict != null) {
            users[entity.id.value] = UserDTO.fromDomain(entity)
            return entity
        } else {
            val newId = im.domain.wrappers.Identifier(++id)
            val newUser = entity.copy(id = newId)
            users[newId.value] = UserDTO.fromDomain(newUser)
            return newUser
        }
    }

    override fun saveAll(entities: Iterable<User>): List<User> {
        entities.forEach { save(it) }
        return entities.toList()
    }

    override fun findById(id: im.domain.wrappers.Identifier): User? = users[id.value]?.toDomain()

    override fun findAll(): List<User> = users.values.map { it.toDomain() }.toList()

    override fun find(
        pagination: PaginationRequest,
        sortRequest: SortRequest,
    ): Pagination<User> {
        val page = utils.paginate(users.values.toList(), pagination, sortRequest)
        return Pagination(page.items.map { it.toDomain() }, page.info)
    }

    override fun findAllById(ids: Iterable<im.domain.wrappers.Identifier>): List<User> =
        users.values
            .filter { user ->
                user.id in
                    ids.map {
                        it.value
                    }
            }.map { it.toDomain() }
            .toList()

    override fun deleteById(id: im.domain.wrappers.Identifier) {
        if (users.containsKey(id.value)) {
            delete(users[id.value]!!.toDomain())
        }
    }

    override fun existsById(id: im.domain.wrappers.Identifier): Boolean = users.containsKey(id.value)

    override fun count(): Long = users.size.toLong()

    override fun deleteAll() {
        id = 999L
        users.values.forEach { delete(it.toDomain()) }
    }

    override fun deleteAll(entities: Iterable<User>) {
        entities.forEach { delete(it) }
    }

    override fun delete(entity: User) {
        channelInvitationRepositoryImpl.deleteAllByInviter(entity)
        channelInvitationRepositoryImpl.deleteAllByInvitee(entity)
        channelRepositoryImpl.deleteAllByOwner(entity)
        messageRepositoryImpl.deleteAllByAuthor(entity)
        sessionRepositoryImpl.deleteAllByUser(entity)
        users.remove(entity.id.value)
    }

    override fun deleteAllById(ids: Iterable<im.domain.wrappers.Identifier>) {
        ids.forEach { deleteById(it) }
    }

    override fun flush() {
        // No-op
    }
}
