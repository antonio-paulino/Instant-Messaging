package im.repository.jpa.repositories.jpa

import im.repository.jpa.model.user.UserDTO
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface UserRepositoryJpa : JpaRepository<UserDTO, Long> {
    fun findByName(name: String): List<UserDTO>

    fun findByEmail(email: String): List<UserDTO>

    @Query(
        countQuery = "SELECT COUNT(u) FROM UserDTO u WHERE lower(u.name) LIKE CONCAT(lower(:name), '%')",
        value = "SELECT u FROM UserDTO u WHERE lower(u.name) LIKE CONCAT(lower(:name), '%')",
    )
    fun findByPartialName(
        name: String,
        page: Pageable,
    ): Page<UserDTO>

    @Query(
        value = "SELECT u FROM UserDTO u WHERE lower(u.name) LIKE CONCAT(lower(:name), '%')",
    )
    fun findByPartialNameSliced(
        name: String,
        page: Pageable,
    ): Slice<UserDTO>

    fun findByNameAndPassword(
        name: String,
        password: String,
    ): List<UserDTO>

    fun findByEmailAndPassword(
        email: String,
        password: String,
    ): List<UserDTO>

    fun findBy(pageable: Pageable): Slice<UserDTO>
}
