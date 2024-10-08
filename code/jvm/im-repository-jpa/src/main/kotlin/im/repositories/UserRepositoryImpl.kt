package im.repositories

import im.pagination.Pagination
import im.channel.Channel
import im.channel.ChannelRole
import im.invitations.ChannelInvitation
import im.model.channel.ChannelDTO
import im.model.channel.ChannelRoleDTO
import im.model.invitation.ChannelInvitationDTO
import im.model.session.SessionDTO
import im.model.user.UserDTO
import im.pagination.PaginationRequest
import jakarta.persistence.EntityManager
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import im.sessions.Session
import im.user.User
import im.repositories.user.UserRepository
import im.wrappers.Email
import im.wrappers.Name
import im.wrappers.Password
import java.util.*

@Repository
interface UserRepositoryJpa : JpaRepository<UserDTO, Long>

@Component
class UserRepositoryImpl(
    private val userRepositoryJpa: UserRepositoryJpa,
    private val entityManager: EntityManager,
    private val utils: JpaRepositoryUtils
) : UserRepository {

    override fun findByName(name: Name): User? {
        val query = entityManager.createQuery("SELECT u FROM UserDTO u WHERE u.name = :name", UserDTO::class.java)
        query.setParameter("name", name.value)
        return query.resultList.firstOrNull()?.toDomain()
    }

    override fun findByEmail(email: Email): User? {
        val query = entityManager.createQuery("SELECT u FROM UserDTO u WHERE u.email = :email", UserDTO::class.java)
        query.setParameter("email", email.value)
        return query.resultList.firstOrNull()?.toDomain()
    }

    override fun findByPartialName(name: String): List<User> {
        val query = entityManager.createQuery("SELECT u FROM UserDTO u WHERE u.name LIKE :name", UserDTO::class.java)
        query.setParameter("name", "${name}%")
        return query.resultList.map { it.toDomain() }
    }

    override fun findByNameAndPassword(name: Name, password: Password): User? {
        val query = entityManager.createQuery(
            "SELECT u FROM UserDTO u WHERE lower(u.name) = lower(:name) AND u.password = :password",
            UserDTO::class.java
        )
        query.setParameter("name", name.value)
        query.setParameter("password", password.value)
        return query.resultList.firstOrNull()?.toDomain()
    }

    override fun findByEmailAndPassword(email: Email, password: Password): User? {
        val query = entityManager.createQuery(
            "SELECT u FROM UserDTO u WHERE lower(u.email) = lower(:email) AND u.password = :password",
            UserDTO::class.java
        )
        query.setParameter("email", email.value)
        query.setParameter("password", password.value)
        return query.resultList.firstOrNull()?.toDomain()
    }

    override fun getOwnedChannels(user: User): List<Channel> {
        val query = entityManager.createQuery(
            "SELECT c FROM ChannelDTO c WHERE c.owner.id = :userId",
            ChannelDTO::class.java
        )
        query.setParameter("userId", user.id.value)
        return query.resultList.map { it.toDomain() }
    }

    override fun getJoinedChannels(user: User): Map<Channel, ChannelRole> {
        val query = entityManager.createQuery(
            "SELECT c, r.role FROM ChannelDTO c JOIN ChannelMemberDTO r ON c.id = r.id.channelID WHERE r.id.userID = :userId",
            Array<Any>::class.java,
        )
        query.setParameter("userId", user.id.value)
        val res = query.resultList.associate {
            (it[0] as ChannelDTO).toDomain() to (it[1] as ChannelRoleDTO).toDomain()
        }
        return res
    }

    override fun getInvitations(user: User): List<ChannelInvitation> {
        val query = entityManager.createQuery(
            "SELECT i FROM ChannelInvitationDTO i WHERE i.invitee.id = :userId",
            ChannelInvitationDTO::class.java
        )
        query.setParameter("userId", user.id.value)
        return query.resultList.map { it.toDomain() }
    }

    override fun getSessions(user: User): List<Session> {
        val query = entityManager.createQuery(
            "SELECT s FROM SessionDTO s WHERE s.user.id = :userId",
            SessionDTO::class.java
        )
        query.setParameter("userId", user.id.value)
        return query.resultList.map { it.toDomain() }
    }

    override fun save(entity: User): User {
        return userRepositoryJpa.save(UserDTO.fromDomain(entity)).toDomain()
    }

    override fun saveAll(entities: Iterable<User>): List<User> {
        return userRepositoryJpa.saveAll(entities.map { UserDTO.fromDomain(it) }).map { it.toDomain() }
    }

    override fun findById(id: Long): User? {
        return userRepositoryJpa.findById(id).map { it.toDomain() }.orElse(null)
    }

    override fun findAll(): List<User> {
        return userRepositoryJpa.findAll().map { it.toDomain() }
    }

    override fun find(pagination: PaginationRequest): Pagination<User> {
        val res = userRepositoryJpa.findAll(utils.toPageRequest(pagination, "id"))
        return Pagination(res.content.map { it.toDomain() }, utils.getPaginationInfo(res))
    }

    override fun findAllById(ids: Iterable<Long>): List<User> {
        return userRepositoryJpa.findAllById(ids).map { it.toDomain() }
    }

    override fun deleteById(id: Long) {
        userRepositoryJpa.deleteById(id)
    }

    override fun existsById(id: Long): Boolean {
        return userRepositoryJpa.existsById(id)
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

    override fun deleteAllById(ids: Iterable<Long>) {
        userRepositoryJpa.deleteAllById(ids)
    }

    override fun flush() {
        entityManager.flush()
        userRepositoryJpa.flush()
    }

}