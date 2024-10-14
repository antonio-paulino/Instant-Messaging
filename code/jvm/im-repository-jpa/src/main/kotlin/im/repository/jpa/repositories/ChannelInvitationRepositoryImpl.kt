package im.repository.jpa.repositories

import im.domain.channel.Channel
import im.domain.invitations.ChannelInvitation
import im.domain.invitations.ChannelInvitationStatus
import im.domain.user.User
import im.domain.wrappers.Identifier
import im.repository.jpa.model.invitation.ChannelInvitationDTO
import im.repository.jpa.model.invitation.ChannelInvitationStatusDTO
import im.repository.jpa.repositories.jpa.ChannelInvitationRepositoryJpa
import im.repository.pagination.Pagination
import im.repository.pagination.PaginationRequest
import im.repository.pagination.SortRequest
import im.repository.repositories.invitations.ChannelInvitationRepository
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component

@Component
@Primary
class ChannelInvitationRepositoryImpl(
    private val channelInvitationRepositoryJpa: ChannelInvitationRepositoryJpa,
    private val utils: JpaRepositoryUtils,
) : ChannelInvitationRepository {
    override fun findByChannel(
        channel: Channel,
        status: ChannelInvitationStatus,
        sortRequest: SortRequest,
    ): List<ChannelInvitation> =
        channelInvitationRepositoryJpa
            .findByChannelIdAndStatus(
                channel.id.value,
                ChannelInvitationStatusDTO.valueOf(status.name),
                utils.toSort(sortRequest),
            ).map { it.toDomain() }

    override fun findByInvitee(
        user: User,
        sortRequest: SortRequest,
    ): List<ChannelInvitation> =
        channelInvitationRepositoryJpa
            .findByInviteeId(user.id.value, utils.toSort(sortRequest))
            .map { it.toDomain() }

    override fun findByInviteeAndChannel(
        user: User,
        channel: Channel,
    ): ChannelInvitation? = channelInvitationRepositoryJpa.findByInviteeIdAndChannelId(user.id.value, channel.id.value)?.toDomain()

    override fun deleteExpired() {
        channelInvitationRepositoryJpa.deleteAllByExpiresAtIsBeforeOrStatusIn()
    }

    override fun save(entity: ChannelInvitation): ChannelInvitation =
        channelInvitationRepositoryJpa.save(ChannelInvitationDTO.fromDomain(entity)).toDomain()

    override fun saveAll(entities: Iterable<ChannelInvitation>): List<ChannelInvitation> =
        channelInvitationRepositoryJpa
            .saveAll(entities.map { ChannelInvitationDTO.fromDomain(it) })
            .map { it.toDomain() }

    override fun findById(id: Identifier): ChannelInvitation? =
        channelInvitationRepositoryJpa
            .findById(id.value)
            .map {
                it.toDomain()
            }.orElse(null)

    override fun findAll(): List<ChannelInvitation> = channelInvitationRepositoryJpa.findAll().map { it.toDomain() }

    override fun find(
        pagination: PaginationRequest,
        sortRequest: SortRequest,
    ): Pagination<ChannelInvitation> {
        val pageable = utils.toPageRequest(pagination, sortRequest)
        val res =
            if (pagination.getCount) {
                channelInvitationRepositoryJpa.findAll(pageable)
            } else {
                channelInvitationRepositoryJpa.findBy(pageable)
            }
        return Pagination(res.content.map { it.toDomain() }, utils.getPaginationInfo(res))
    }

    override fun deleteById(id: Identifier) {
        channelInvitationRepositoryJpa.deleteById(id.value)
    }

    override fun findAllById(ids: Iterable<Identifier>): List<ChannelInvitation> =
        channelInvitationRepositoryJpa
            .findAllById(
                ids.map {
                    it.value
                },
            ).map { it.toDomain() }

    override fun existsById(id: Identifier): Boolean = channelInvitationRepositoryJpa.existsById(id.value)

    override fun count(): Long = channelInvitationRepositoryJpa.count()

    override fun deleteAll() {
        channelInvitationRepositoryJpa.deleteAll()
    }

    override fun deleteAll(entities: Iterable<ChannelInvitation>) {
        channelInvitationRepositoryJpa.deleteAll(entities.map { ChannelInvitationDTO.fromDomain(it) })
    }

    override fun delete(entity: ChannelInvitation) {
        channelInvitationRepositoryJpa.delete(ChannelInvitationDTO.fromDomain(entity))
    }

    override fun deleteAllById(ids: Iterable<Identifier>) {
        channelInvitationRepositoryJpa.deleteAllById(ids.map { it.value })
    }

    override fun flush() {
        channelInvitationRepositoryJpa.flush()
    }
}
