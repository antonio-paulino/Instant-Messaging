package im.repository.jpa.repositories

import im.domain.invitations.ImInvitation
import im.repository.jpa.model.invitation.ImInvitationDTO
import im.repository.jpa.repositories.jpa.ImInvitationRepositoryJpa
import im.repository.pagination.Pagination
import im.repository.pagination.PaginationRequest
import im.repository.pagination.SortRequest
import im.repository.repositories.invitations.ImInvitationRepository
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
import java.util.UUID

@Component
@Primary
class ImInvitationRepositoryImpl(
    private val imInvitationRepositoryJpa: ImInvitationRepositoryJpa,
    private val utils: JpaRepositoryUtils,
) : ImInvitationRepository {
    override fun deleteExpired() {
        imInvitationRepositoryJpa.deleteAllByExpiresAtIsBeforeOrStatusIs()
    }

    override fun save(entity: ImInvitation): ImInvitation = imInvitationRepositoryJpa.save(ImInvitationDTO.fromDomain(entity)).toDomain()

    override fun saveAll(entities: Iterable<ImInvitation>): List<ImInvitation> =
        imInvitationRepositoryJpa
            .saveAll(
                entities.map {
                    ImInvitationDTO.fromDomain(it)
                },
            ).map { it.toDomain() }

    override fun findById(id: UUID): ImInvitation? = imInvitationRepositoryJpa.findById(id).map { it.toDomain() }.orElse(null)

    override fun findAll(): List<ImInvitation> = imInvitationRepositoryJpa.findAll().map { it.toDomain() }

    override fun find(
        pagination: PaginationRequest,
        sortRequest: SortRequest,
    ): Pagination<ImInvitation> {
        val pageable = utils.toPageRequest(pagination, sortRequest)
        val res =
            if (pagination.getCount) {
                imInvitationRepositoryJpa.findAll(pageable)
            } else {
                imInvitationRepositoryJpa.findBy(pageable)
            }
        return Pagination(res.content.map { it.toDomain() }, utils.getPaginationInfo(res))
    }

    override fun findAllById(ids: Iterable<UUID>): List<ImInvitation> = imInvitationRepositoryJpa.findAllById(ids).map { it.toDomain() }

    override fun deleteById(id: UUID) {
        imInvitationRepositoryJpa.deleteById(id)
    }

    override fun existsById(id: UUID): Boolean = imInvitationRepositoryJpa.existsById(id)

    override fun count(): Long = imInvitationRepositoryJpa.count()

    override fun deleteAll() {
        imInvitationRepositoryJpa.deleteAll()
    }

    override fun deleteAll(entities: Iterable<ImInvitation>) {
        imInvitationRepositoryJpa.deleteAll(entities.map { ImInvitationDTO.fromDomain(it) })
    }

    override fun delete(entity: ImInvitation) {
        imInvitationRepositoryJpa.delete(ImInvitationDTO.fromDomain(entity))
    }

    override fun deleteAllById(ids: Iterable<UUID>) {
        imInvitationRepositoryJpa.deleteAllById(ids)
    }

    override fun flush() {
        imInvitationRepositoryJpa.flush()
    }
}
