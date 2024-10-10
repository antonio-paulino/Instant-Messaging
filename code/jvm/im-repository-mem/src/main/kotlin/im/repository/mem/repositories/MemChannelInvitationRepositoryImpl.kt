package im.repository.mem.repositories

import im.channel.Channel
import im.invitations.ChannelInvitation
import im.invitations.ChannelInvitationStatus
import im.repository.pagination.Pagination
import im.repository.pagination.PaginationRequest
import im.repository.repositories.invitations.ChannelInvitationRepository
import im.repository.mem.model.invitation.ChannelInvitationDTO
import im.user.User
import im.wrappers.Identifier
import java.util.concurrent.ConcurrentHashMap

class MemChannelInvitationRepositoryImpl(
    private val utils: MemRepoUtils
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

    override fun findByChannel(channel: Channel, status: ChannelInvitationStatus): List<ChannelInvitation> {
        return invitations.values.filter { it.channel.id == channel.id.value && it.status == status }
            .map { it.toDomain() }
    }

    override fun findByInvitee(user: User): List<ChannelInvitation> {
        return invitations.values.filter { it.invitee.id == user.id.value }.map { it.toDomain() }
    }

    override fun saveAll(entities: Iterable<ChannelInvitation>): List<ChannelInvitation> {
        val newEntities = entities.map { save(it) }
        return newEntities.toList()
    }

    override fun findById(id: Identifier): ChannelInvitation? {
        return invitations[id]?.toDomain()
    }

    override fun findAll(): List<ChannelInvitation> {
        return invitations.values.map { it.toDomain() }
    }

    override fun find(pagination: PaginationRequest): Pagination<ChannelInvitation> {
        val page = utils.paginate(invitations.values.toList(), pagination, "id")
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

    override fun existsById(id: Identifier): Boolean {
        return invitations.containsKey(id)
    }

    override fun count(): Long {
        return invitations.size.toLong()
    }

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