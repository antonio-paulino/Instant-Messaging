package repositories

import invitations.ChannelInvitation
import invitations.ChannelInvitationRepository
import jakarta.persistence.EntityManager
import model.invitation.ChannelInvitationDTO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface ChannelInvitationRepositoryJpa : JpaRepository<ChannelInvitationDTO, Long>

@Component
class ChannelInvitationRepositoryImpl : ChannelInvitationRepository {

    @Autowired
    private lateinit var channelInvitationRepositoryJpa: ChannelInvitationRepositoryJpa

    @Autowired
    private lateinit var entityManager: EntityManager

    override fun save(entity: ChannelInvitation): ChannelInvitation {
        return channelInvitationRepositoryJpa.save(ChannelInvitationDTO.fromDomain(entity)).toDomain()
    }

    override fun saveAll(entities: Iterable<ChannelInvitation>): List<ChannelInvitation> {
        return channelInvitationRepositoryJpa.saveAll(entities.map { ChannelInvitationDTO.fromDomain(it) })
            .map { it.toDomain() }
    }

    override fun findById(id: Long): Optional<ChannelInvitation> {
        return channelInvitationRepositoryJpa.findById(id).map { it.toDomain() }
    }

    override fun findAll(): Iterable<ChannelInvitation> {
        return channelInvitationRepositoryJpa.findAll().map { it.toDomain() }
    }

    override fun findFirst(page: Int, pageSize: Int): List<ChannelInvitation> {
        val res = channelInvitationRepositoryJpa.findAll(org.springframework.data.domain.PageRequest.of(page, pageSize))
        return res.content.map { it.toDomain() }
    }

    override fun findLast(page: Int, pageSize: Int): List<ChannelInvitation> {
        val query = entityManager.createQuery(
            "SELECT i FROM ChannelInvitationDTO i ORDER BY i.id DESC",
            ChannelInvitationDTO::class.java
        )
        query.firstResult = page * pageSize
        query.maxResults = pageSize
        return query.resultList.map { it.toDomain() }
    }

    override fun deleteById(id: Long) {
        channelInvitationRepositoryJpa.deleteById(id)
    }

    override fun findAllById(ids: Iterable<Long>): Iterable<ChannelInvitation> {
        return channelInvitationRepositoryJpa.findAllById(ids).map { it.toDomain() }
    }

    override fun existsById(id: Long): Boolean {
        return channelInvitationRepositoryJpa.existsById(id)
    }

    override fun count(): Long {
        return channelInvitationRepositoryJpa.count()
    }

    override fun deleteAll() {
        channelInvitationRepositoryJpa.deleteAll()
    }

    override fun deleteAll(entities: Iterable<ChannelInvitation>) {
        channelInvitationRepositoryJpa.deleteAll(entities.map { ChannelInvitationDTO.fromDomain(it) })
    }

    override fun delete(entity: ChannelInvitation) {
        channelInvitationRepositoryJpa.delete(ChannelInvitationDTO.fromDomain(entity))
    }

    override fun deleteAllById(ids: Iterable<Long>) {
        channelInvitationRepositoryJpa.deleteAllById(ids)
    }
}