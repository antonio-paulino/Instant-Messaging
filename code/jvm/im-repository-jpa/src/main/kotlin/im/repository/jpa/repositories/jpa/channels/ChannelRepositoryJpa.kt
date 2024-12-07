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
                "(c.isPublic or NOT :filterPublic = true) AND c.id > :after",
    )
    fun findByPartialName(
        name: String,
        filterPublic: Boolean,
        page: Pageable,
        after: Long,
    ): Page<ChannelDTO>

    @Query(
        value =
            "SELECT c FROM ChannelDTO c WHERE lower(c.name) LIKE CONCAT(lower(:name), '%') AND " +
                "(c.isPublic or NOT :filterPublic = true) AND c.id > :after",
    )
    fun findByPartialNameSliced(
        name: String,
        filterPublic: Boolean,
        page: Pageable,
        after: Long,
    ): Slice<ChannelDTO>

    @Query(
        value = "SELECT c FROM ChannelDTO c WHERE (c.isPublic or NOT :filterPublic = true) AND c.id > :after",
    )
    fun findAll(
        page: Pageable,
        filterPublic: Boolean,
        after: Long,
    ): Page<ChannelDTO>

    @Query(
        value = "SELECT c FROM ChannelDTO c WHERE  (c.isPublic or NOT :filterPublic = true) AND c.id > :after",
    )
    fun findBy(
        page: Pageable,
        filterPublic: Boolean,
        after: Long,
    ): Slice<ChannelDTO>

    @Query(
        value = "SELECT c FROM ChannelDTO c WHERE c.owner.id = :ownerId AND c.id > :after",
    )
    fun findByOwnerId(
        ownerId: Long,
        page: Pageable,
        after: Long,
    ): Page<ChannelDTO>

    @Query(
        value = "SELECT c FROM ChannelDTO c WHERE c.owner.id = :ownerId AND c.id > :after",
    )
    fun findByOwnerIdSliced(
        ownerId: Long,
        page: Pageable,
        after: Long,
    ): Slice<ChannelDTO>
}
