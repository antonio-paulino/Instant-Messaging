package im.repositories

import im.channel.Channel
import im.channel.ChannelRepository
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
import org.springframework.data.domain.Pageable
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
    private val entityManager: EntityManager
) : ChannelRepository {

    override fun findByName(name: String): Channel? {
        val query = entityManager.createQuery(
            "SELECT c FROM ChannelDTO c WHERE c.name = :name",
            ChannelDTO::class.java
        )
        query.setParameter("name", name)
        return query.resultList.firstOrNull()?.toDomain()
    }

    override fun findByPartialName(name: String): List<Channel> {
        val query = entityManager.createQuery(
            "SELECT c FROM ChannelDTO c WHERE lower(c.name ) LIKE lower(:name)",
            ChannelDTO::class.java
        )
        query.setParameter("name", "%$name%")
        return query.resultList.map { it.toDomain() }
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

    override fun findFirst(page: Int, pageSize: Int): List<Channel> {
        val res = channelRepositoryJpa.findAll(Pageable.ofSize(pageSize).withPage(page))
        return res.content.map { it.toDomain() }
    }

    override fun findLast(page: Int, pageSize: Int): List<Channel> {
        val query = entityManager.createQuery(
            "SELECT c FROM ChannelDTO c ORDER BY c.id DESC",
            ChannelDTO::class.java
        )
        query.firstResult = page * pageSize
        query.maxResults = pageSize
        return query.resultList.map { it.toDomain() }
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