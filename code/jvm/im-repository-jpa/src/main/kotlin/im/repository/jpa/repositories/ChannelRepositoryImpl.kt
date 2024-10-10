package im.repository.jpa.repositories

import im.channel.Channel
import im.channel.ChannelRole
import im.repository.jpa.model.channel.ChannelDTO
import im.repository.jpa.repositories.jpa.ChannelMemberRepositoryJpa
import im.repository.jpa.repositories.jpa.ChannelRepositoryJpa
import im.repository.pagination.Pagination
import im.repository.pagination.PaginationRequest
import im.repository.repositories.channel.ChannelRepository
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Component
import im.user.User
import im.wrappers.Identifier
import im.wrappers.Name
import org.springframework.context.annotation.Primary

@Component
@Primary
class ChannelRepositoryImpl(
    private val channelRepositoryJpa: ChannelRepositoryJpa,
    private val channelMemberRepositoryJpa: ChannelMemberRepositoryJpa,
    private val entityManager: EntityManager,
    private val utils: JpaRepositoryUtils
) : ChannelRepository {

    override fun findByName(name: Name, filterPublic: Boolean): Channel? {
        return channelRepositoryJpa.findByName(name.value, filterPublic)?.toDomain()
    }

    override fun findByPartialName(
        name: String,
        filterPublic: Boolean,
        pagination: PaginationRequest
    ): Pagination<Channel> {
        val res = channelRepositoryJpa.findByPartialName(
            name,
            filterPublic,
            utils.toPageRequest(pagination, "id")
        )
        return Pagination(res.content.map { it.toDomain() }, utils.getPaginationInfo(res))
    }

    override fun find(pagination: PaginationRequest, filterPublic: Boolean): Pagination<Channel> {
        val res = channelRepositoryJpa.find(filterPublic, utils.toPageRequest(pagination, "id"))
        return Pagination(res.content.map { it.toDomain() }, utils.getPaginationInfo(res))
    }

    override fun getMember(channel: Channel, user: User): Pair<User, ChannelRole>? {
        return channelMemberRepositoryJpa.findMemberByChannel(channel.id.value, user.id.value)?.let {
            it.user.toDomain() to it.role.toDomain()
        }
    }

    override fun findByOwner(user: User): List<Channel> {
        return channelRepositoryJpa.findByOwner(user.id.value).map { it.toDomain() }
    }

    override fun findByMember(user: User): Map<Channel, ChannelRole> {
        return channelRepositoryJpa.findByMember(user.id.value)
            .associate { it.channel.toDomain() to it.role.toDomain() }
    }

    override fun save(entity: Channel): Channel {
        return channelRepositoryJpa.save(ChannelDTO.fromDomain(entity)).toDomain()
    }

    override fun saveAll(entities: Iterable<Channel>): List<Channel> {
        return channelRepositoryJpa.saveAll(entities.map { ChannelDTO.fromDomain(it) }).map { it.toDomain() }
    }

    override fun findById(id: Identifier): Channel? {
        return channelRepositoryJpa.findById(id.value).map { it.toDomain() }.orElse(null)
    }

    override fun findAll(): List<Channel> {
        return channelRepositoryJpa.findAll().map { it.toDomain() }
    }

    override fun find(pagination: PaginationRequest): Pagination<Channel> {
        val res = channelRepositoryJpa.findAll(utils.toPageRequest(pagination, "id"))
        return Pagination(res.content.map { it.toDomain() }, utils.getPaginationInfo(res))
    }

    override fun findAllById(ids: Iterable<Identifier>): List<Channel> {
        return channelRepositoryJpa.findAllById(ids.map { it.value }).map { it.toDomain() }
    }

    override fun deleteById(id: Identifier) {
        channelRepositoryJpa.deleteById(id.value)
    }

    override fun existsById(id: Identifier): Boolean {
        return channelRepositoryJpa.existsById(id.value)
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

    override fun deleteAllById(ids: Iterable<Identifier>) {
        channelRepositoryJpa.deleteAllById(ids.map { it.value })
    }

    override fun flush() {
        entityManager.flush()
        channelRepositoryJpa.flush()
    }
}