package repositories

import jakarta.persistence.EntityManager
import model.user.UserDTO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import user.User
import user.UserRepository
import java.util.*

@Repository
interface UserRepositoryJpa : JpaRepository<UserDTO, Long>

@Component
class UserRepositoryImpl : UserRepository {

    @Autowired
    private lateinit var entityManager: EntityManager

    @Autowired
    private lateinit var userRepositoryJpa: UserRepositoryJpa

    override fun findByName(name: String): User? {
        val query = entityManager.createQuery("SELECT u FROM UserDTO u WHERE u.name = :name", UserDTO::class.java)
        query.setParameter("name", name)
        return query.resultList.firstOrNull()?.toDomain()
    }

    override fun findByPartialName(name: String): List<User> {
        val query = entityManager.createQuery("SELECT u FROM UserDTO u WHERE u.name LIKE :name", UserDTO::class.java)
        query.setParameter("name", "$name%")
        return query.resultList.map { it.toDomain() }
    }

    override fun findByNameAndPassword(name: String, password: String): User? {
        val query = entityManager.createQuery(
            "SELECT u FROM UserDTO u WHERE u.name = :name AND u.password = :password",
            UserDTO::class.java
        )
        query.setParameter("name", name)
        query.setParameter("password", password)
        return query.resultList.firstOrNull()?.toDomain()
    }

    override fun save(entity: User): User {
        return userRepositoryJpa.save(UserDTO.fromDomain(entity)).toDomain()
    }

    override fun saveAll(entities: Iterable<User>): List<User> {
        return userRepositoryJpa.saveAll(entities.map { UserDTO.fromDomain(it) }).map { it.toDomain() }
    }

    override fun findById(id: Long): Optional<User> {
        return userRepositoryJpa.findById(id).map { it.toDomain() }
    }

    override fun findAll(): Iterable<User> {
        return userRepositoryJpa.findAll().map { it.toDomain() }
    }

    override fun findFirst(page: Int, pageSize: Int): List<User> {
        val res = userRepositoryJpa.findAll(Pageable.ofSize(pageSize).withPage(page))
        return res.content.map { it.toDomain() }
    }

    override fun findLast(page: Int, pageSize: Int): List<User> {
        val query = entityManager.createQuery("SELECT u FROM UserDTO u ORDER BY u.id DESC", UserDTO::class.java)
        query.firstResult = page * pageSize
        query.maxResults = pageSize
        return query.resultList.map { it.toDomain() }
    }

    override fun findAllById(ids: Iterable<Long>): Iterable<User> {
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
}