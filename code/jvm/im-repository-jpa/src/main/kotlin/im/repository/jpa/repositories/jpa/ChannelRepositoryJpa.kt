package im.repository.jpa.repositories.jpa

import im.repository.jpa.model.channel.ChannelDTO
import im.repository.jpa.model.channel.ChannelMemberDTO
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ChannelRepositoryJpa : JpaRepository<ChannelDTO, Long> {
    @Query(
        "SELECT c FROM ChannelDTO c WHERE c.name = :name AND " +
                "(c.isPublic or NOT :filterPublic = true)"
    )
    fun findByName(name: String, filterPublic: Boolean): ChannelDTO?

    @Query(
        countQuery = "SELECT COUNT(c) FROM ChannelDTO c WHERE c.name LIKE CONCAT(:name, '%') AND " +
                "(c.isPublic or NOT :filterPublic = true)",
        value = "SELECT c FROM ChannelDTO c WHERE c.name LIKE CONCAT(:name, '%') AND " +
                "(c.isPublic or NOT :filterPublic = true)"
    )
    fun findByPartialName(name: String, filterPublic: Boolean, page: Pageable): Page<ChannelDTO>

    @Query(
        countQuery = "SELECT COUNT(c) FROM ChannelDTO c WHERE " +
                "(c.isPublic or NOT :filterPublic = true)",
        value = "SELECT c FROM ChannelDTO c WHERE " +
                "(c.isPublic or NOT :filterPublic = true)"
    )
    fun find(filterPublic: Boolean, page: Pageable): Page<ChannelDTO>

    @Query(
        "SELECT c FROM ChannelDTO as c where c.owner.id = :ownerId"
    )
    fun findByOwner(ownerId: Long): List<ChannelDTO>

    @Query(
        "SELECT m FROM ChannelMemberDTO as m where m.user.id = :userId"
    )
    fun findByMember(userId: Long): List<ChannelMemberDTO>
}