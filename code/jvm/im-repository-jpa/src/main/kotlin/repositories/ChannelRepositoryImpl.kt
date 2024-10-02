package repositories

import channel.Channel
import channel.ChannelMember
import channel.ChannelRepository
import invitations.ChannelRole
import jakarta.persistence.EntityManager
import model.channel.ChannelDTO
import model.channel.ChannelMemberDTO
import model.channel.ChannelMemberID
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import user.User
import java.util.*

@Repository
interface ChannelRepositoryJpa : JpaRepository<ChannelDTO, Long>

@Repository
interface ChannelMemberRepositoryJpa : JpaRepository<ChannelMemberDTO, ChannelMemberID>

@Component
class ChannelRepositoryImpl : ChannelRepository {

    @Autowired
    private lateinit var channelRepositoryJpa: ChannelRepositoryJpa

    @Autowired
    private lateinit var channelMemberRepositoryJpa: ChannelMemberRepositoryJpa

    @Autowired
    private lateinit var entityManager: EntityManager

    override fun findByName(name: String): Channel? {
        val query = entityManager.createQuery(
            "SELECT c FROM ChannelDTO c WHERE c.name = :name",
            ChannelDTO::class.java
        )
        query.setParameter("name", name)
        return query.resultList.firstOrNull()?.toDomain()
    }

    override fun findByPartialName(name: String): Iterable<Channel> {
        val query = entityManager.createQuery(
            "SELECT c FROM ChannelDTO c WHERE c.name LIKE :name",
            ChannelDTO::class.java
        )
        query.setParameter("name", "%$name%")
        return query.resultList.map { it.toDomain() }
    }

    override fun getUserRoles(channel: Channel): Map<User, ChannelRole> {
        val query = entityManager.createQuery(
            "SELECT m FROM ChannelMemberDTO m WHERE m.channel = :channel",
            ChannelMemberDTO::class.java
        )
        query.setParameter("channel", ChannelDTO.fromDomain(channel))
        return query.resultList.associate { it.user!!.toDomain() to it.role!!.toDomain() }
    }

    override fun addMember(channel: Channel, user: User, role: ChannelRole): Channel {
        val newMember = ChannelMemberDTO.fromDomain(ChannelMember(channel, user, role))
        val newChannel = channel.copy(members = channel.members + user)
        channelMemberRepositoryJpa.save(newMember)
        return save(newChannel).also {
            channelRepositoryJpa.flush()
            channelMemberRepositoryJpa.flush()
        }
    }

    override fun removeMember(channel: Channel, user: User): Channel {
        val channelMember = channelMemberRepositoryJpa.findById(ChannelMemberID(channel.id, user.id)).get()
        val newChannel = channel.copy(members = channel.members - user)
        channelMemberRepositoryJpa.delete(channelMember)
        return save(newChannel)
    }

    override fun save(entity: Channel): Channel {
        return channelRepositoryJpa.save(ChannelDTO.fromDomain(entity)).toDomain()
    }

    override fun saveAll(entities: Iterable<Channel>): List<Channel> {
        return channelRepositoryJpa.saveAll(entities.map { ChannelDTO.fromDomain(it) }).map { it.toDomain() }
    }

    override fun findById(id: Long): Optional<Channel> {
        return channelRepositoryJpa.findById(id).map { it.toDomain() }
    }

    override fun findAll(): Iterable<Channel> {
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

    override fun findAllById(ids: Iterable<Long>): Iterable<Channel> {
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
}