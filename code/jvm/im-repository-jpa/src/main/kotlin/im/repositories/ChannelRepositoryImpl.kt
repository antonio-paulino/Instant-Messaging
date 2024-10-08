package im.repositories

import im.pagination.Pagination
import im.channel.Channel
import im.repositories.channel.ChannelRepository
import im.channel.ChannelRole
import im.invitations.ChannelInvitation
import im.invitations.ChannelInvitationStatus
import jakarta.persistence.EntityManager
import im.messages.Message
import im.model.channel.ChannelDTO
import im.model.channel.ChannelMemberDTO
import im.model.channel.ChannelMemberId
import im.model.invitation.ChannelInvitationDTO
import im.model.message.MessageDTO
import im.pagination.PaginationRequest
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import im.user.User

@Repository
interface ChannelRepositoryJpa : JpaRepository<ChannelDTO, Long>

@Repository
interface ChannelMemberRepositoryJpa : JpaRepository<ChannelMemberDTO, ChannelMemberId>

@Component
class ChannelRepositoryImpl(
    private val channelRepositoryJpa: ChannelRepositoryJpa,
    private val channelMemberRepositoryJpa: ChannelMemberRepositoryJpa,
    private val entityManager: EntityManager,
    private val utils: JpaRepositoryUtils
) : ChannelRepository {

    override fun findByName(name: String, filterPublic: Boolean): List<Channel> {
        val baseQuery = "SELECT c FROM ChannelDTO c WHERE c.name = :name"
        val publicFilter = if (filterPublic) " AND c.isPublic = true" else ""
        val query = entityManager.createQuery(
            "$baseQuery$publicFilter", ChannelDTO::class.java
        )
        query.setParameter("name", name)
        return query.resultList.map { it.toDomain() }
    }

    override fun findByPartialName(name: String, filterPublic: Boolean, pagination: PaginationRequest): Pagination<Channel> {
        val baseQuery = "FROM ChannelDTO c WHERE c.name LIKE :name"
        val publicFilter = if (filterPublic) " AND c.isPublic = true" else ""

        val countQuery = entityManager.createQuery(
            "SELECT COUNT(c) $baseQuery$publicFilter", Long::class.java
        )

        countQuery.setParameter("name", "%$name%")

        val count = countQuery.singleResult

        val query = entityManager.createQuery(
            "SELECT c $baseQuery$publicFilter", ChannelDTO::class.java
        )

        query.setParameter("name", "%$name%")
        query.firstResult = (pagination.page - 1) * pagination.size
        query.maxResults = pagination.size

        return utils.calculatePagination(query.resultList.map { it.toDomain() }, count, pagination)
    }

    override fun find(pagination: PaginationRequest, filterPublic: Boolean): Pagination<Channel> {
        val baseCountQuery = "SELECT COUNT(c) FROM ChannelDTO c"
        val baseSelectQuery = "SELECT c FROM ChannelDTO c"
        val publicFilter = if (filterPublic) " WHERE c.isPublic = true" else ""

        val countQuery = entityManager.createQuery(
            "$baseCountQuery$publicFilter", Long::class.java
        )
        val count = countQuery.getSingleResult()

        val query = entityManager.createQuery(
            "$baseSelectQuery$publicFilter", ChannelDTO::class.java
        )
        query.firstResult = (pagination.page - 1) * pagination.size
        query.maxResults = pagination.size

        return utils.calculatePagination(query.resultList.map { it.toDomain() }, count, pagination)
    }


    override fun getInvitations(channel: Channel, status: ChannelInvitationStatus): List<ChannelInvitation> {
        val query = entityManager.createQuery(
            "SELECT i FROM ChannelInvitationDTO i WHERE i.channel.id = :channelId AND i.status = :status",
            ChannelInvitationDTO::class.java
        )
        query.setParameter("channelId", channel.id)
        query.setParameter("status", status)
        return query.resultList.map { it.toDomain() }
    }

    override fun getMessages(channel: Channel): List<Message> {
        val query = entityManager.createQuery(
            "SELECT m FROM MessageDTO m WHERE m.channel.id = :channelId",
            MessageDTO::class.java
        )
        query.setParameter("channelId", channel.id)
        return query.resultList.map { it.toDomain() }
    }

    override fun getMember(channel: Channel, user: User): Pair<User, ChannelRole>? {
        val query = entityManager.createQuery(
            "SELECT m FROM ChannelMemberDTO m WHERE m.id.channelID = :channelId AND m.id.userID = :userId",
            ChannelMemberDTO::class.java
        )
        query.setParameter("channelId", channel.id)
        query.setParameter("userId", user.id)
        val result = query.resultList.firstOrNull()
        return if (result != null) {
            Pair(result.user.toDomain(), result.role.toDomain())
        } else null
    }

    override fun save(entity: Channel): Channel {
        return channelRepositoryJpa.save(ChannelDTO.fromDomain(entity)).toDomain()
    }

    override fun saveAll(entities: Iterable<Channel>): List<Channel> {
        return channelRepositoryJpa.saveAll(entities.map { ChannelDTO.fromDomain(it) }).map { it.toDomain() }
    }

    override fun findById(id: Long): Channel? {
        return channelRepositoryJpa.findById(id).map { it.toDomain() }.orElse(null)
    }

    override fun findAll(): List<Channel> {
        return channelRepositoryJpa.findAll().map { it.toDomain() }
    }

    override fun find(pagination: PaginationRequest): Pagination<Channel> {
        val res = channelRepositoryJpa.findAll(utils.toPageRequest(pagination, "id"))
        return Pagination(res.content.map { it.toDomain() }, utils.getPaginationInfo(res))
    }

    override fun findAllById(ids: Iterable<Long>): List<Channel> {
        return channelRepositoryJpa.findAllById(ids).map { it.toDomain() }
    }

    override fun deleteById(id: Long) {
        channelRepositoryJpa.deleteById(id)
    }

    override fun existsById(id: Long): Boolean {
        return channelRepositoryJpa.existsById(id)
    }

    override fun count(): Long {
        return channelRepositoryJpa.count()
    }

    override fun deleteAll() {
        channelRepositoryJpa.deleteAll()
    }

    override fun deleteAll(entities: Iterable<Channel>) {
        channelRepositoryJpa.deleteAll(entities.map { ChannelDTO.fromDomain(it) })
    }

    override fun delete(entity: Channel) {
        channelRepositoryJpa.delete(ChannelDTO.fromDomain(entity))
    }

    override fun deleteAllById(ids: Iterable<Long>) {
        channelRepositoryJpa.deleteAllById(ids)
    }

    override fun flush() {
        entityManager.flush()
        channelRepositoryJpa.flush()
        channelMemberRepositoryJpa.flush()
    }
}