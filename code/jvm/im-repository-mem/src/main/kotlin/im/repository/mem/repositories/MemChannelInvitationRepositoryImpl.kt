package im.repository.mem.repositories

import im.domain.channel.Channel
import im.domain.invitations.ChannelInvitation
import im.domain.invitations.ChannelInvitationStatus
import im.domain.user.User
import im.domain.wrappers.Identifier
import im.repository.mem.model.invitation.ChannelInvitationDTO
import im.repository.pagination.Pagination
import im.repository.pagination.PaginationRequest
import im.repository.pagination.SortRequest
import im.repository.repositories.invitations.ChannelInvitationRepository
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap

class MemChannelInvitationRepositoryImpl(
    private val utils: MemRepoUtils,
) : ChannelInvitationRepository {
    private val invitations = ConcurrentHashMap<Identifier, ChannelInvitationDTO>()

    private var id = 999L // Start from 1000 to avoid conflicts with invitations created in tests

    override fun save(entity: ChannelInvitation): ChannelInvitation {
        val conflict = invitations.values.find { it.id == entity.id.value }
        if (conflict != null) {
            invitations[entity.id] = ChannelInvitationDTO.fromDomain(entity)
            return entity
        } else {
            val newId = Identifier(++id)
            val newInvitation = entity.copy(id = newId)
            invitations[newId] = ChannelInvitationDTO.fromDomain(newInvitation)
            return newInvitation
        }
    }

    override fun findByChannel(
        channel: Channel,
        status: ChannelInvitationStatus,
        sortRequest: SortRequest,
        paginationRequest: PaginationRequest,
    ): Pagination<ChannelInvitation> =
        utils.paginate(
            invitations.values.filter { it.channel.id == channel.id.value && it.status == status }.map { it.toDomain() },
            paginationRequest,
            sortRequest,
        )

    override fun findByInvitee(
        user: User,
        status: ChannelInvitationStatus,
        sortRequest: SortRequest,
        paginationRequest: PaginationRequest,
    ): Pagination<ChannelInvitation> =
        utils.paginate(
            invitations.values.filter { it.invitee.id == user.id.value && it.status == status }.map { it.toDomain() },
            paginationRequest,
            sortRequest,
        )

    override fun findByInviteeAndChannel(
        user: User,
        channel: Channel,
    ): ChannelInvitation? =
        invitations.values
            .find { it.invitee.id == user.id.value && it.channel.id == channel.id.value }
            ?.toDomain()

    override fun deleteExpired() {
        invitations.values.filter { it.expiresAt.isBefore(LocalDateTime.now()) }.forEach { delete(it.toDomain()) }
    }

    override fun saveAll(entities: Iterable<ChannelInvitation>): List<ChannelInvitation> {
        val newEntities = entities.map { save(it) }
        return newEntities.toList()
    }

    override fun findById(id: Identifier): ChannelInvitation? = invitations[id]?.toDomain()

    override fun findAll(): List<ChannelInvitation> = invitations.values.map { it.toDomain() }

    override fun find(
        pagination: PaginationRequest,
        sortRequest: SortRequest,
    ): Pagination<ChannelInvitation> {
        val page = utils.paginate(invitations.values.toList(), pagination, sortRequest)
        return Pagination(page.items.map { it.toDomain() }, page.info)
    }

    override fun findAllById(ids: Iterable<Identifier>): List<ChannelInvitation> {
        val idList = ids.map { it.value }
        return invitations.values.filter { it.id in idList }.map { it.toDomain() }
    }

    override fun deleteById(id: Identifier) {
        if (invitations.containsKey(id)) {
            delete(invitations[id]!!.toDomain())
        }
    }

    override fun existsById(id: Identifier): Boolean = invitations.containsKey(id)

    override fun count(): Long = invitations.size.toLong()

    override fun deleteAll() {
        invitations.forEach { delete(it.value.toDomain()) }
    }

    override fun deleteAll(entities: Iterable<ChannelInvitation>) {
        entities.forEach { delete(it) }
    }

    override fun delete(entity: ChannelInvitation) {
        invitations.remove(entity.id)
    }

    override fun deleteAllById(ids: Iterable<Identifier>) {
        ids.forEach { deleteById(it) }
    }

    fun deleteAllByChannel(channel: Channel) {
        invitations.values.filter { it.channel.id == channel.id.value }.forEach { delete(it.toDomain()) }
    }

    fun deleteAllByInvitee(user: User) {
        invitations.values.filter { it.invitee.id == user.id.value }.forEach { delete(it.toDomain()) }
    }

    fun deleteAllByInviter(user: User) {
        invitations.values.filter { it.inviter.id == user.id.value }.forEach { delete(it.toDomain()) }
    }

    override fun flush() {
        // No-op
    }
}
