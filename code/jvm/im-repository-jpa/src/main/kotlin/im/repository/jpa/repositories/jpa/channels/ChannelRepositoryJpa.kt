package im.repository.jpa.repositories.jpa.channels

import im.repository.jpa.model.channel.ChannelDTO
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ChannelRepositoryJpa : JpaRepository<ChannelDTO, Long> {
    @Query(
        "SELECT c FROM ChannelDTO c WHERE c.name = :name AND " +
            "(c.isPublic or NOT :filterPublic = true)",
    )
    fun findByName(
        name: String,
        filterPublic: Boolean,
    ): ChannelDTO?

    @Query(
        value =
            "SELECT c FROM ChannelDTO c WHERE lower(c.name) LIKE CONCAT(lower(:name), '%') AND " +
                "(c.isPublic or NOT :filterPublic = true)",
    )
    fun findByPartialName(
        name: String,
        filterPublic: Boolean,
        page: Pageable,
    ): Page<ChannelDTO>

    @Query(
        value =
            "SELECT c FROM ChannelDTO c WHERE lower(c.name) LIKE CONCAT(lower(:name), '%') AND " +
                "(c.isPublic or NOT :filterPublic = true)",
    )
    fun findByPartialNameSliced(
        name: String,
        filterPublic: Boolean,
        page: Pageable,
    ): Slice<ChannelDTO>

    @Query(
        value = "SELECT c FROM ChannelDTO c WHERE c.isPublic or NOT :filterPublic = true",
    )
    fun findAll(
        page: Pageable,
        filterPublic: Boolean,
    ): Page<ChannelDTO>

    @Query(
        value = "SELECT c FROM ChannelDTO c WHERE c.isPublic or NOT :filterPublic = true",
    )
    fun findBy(
        page: Pageable,
        filterPublic: Boolean,
    ): Slice<ChannelDTO>

    fun findByOwnerId(
        ownerId: Long,
        page: Pageable,
    ): Page<ChannelDTO>

    @Query(
        value = "SELECT c FROM ChannelDTO c WHERE c.owner.id = :ownerId",
    )
    fun findByOwnerIdSliced(
        ownerId: Long,
        page: Pageable,
    ): Slice<ChannelDTO>
}
