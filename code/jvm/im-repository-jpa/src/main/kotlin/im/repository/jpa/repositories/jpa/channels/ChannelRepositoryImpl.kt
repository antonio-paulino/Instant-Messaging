package im.repository.jpa.repositories.jpa.channels

import im.domain.channel.Channel
import im.domain.channel.ChannelRole
import im.domain.user.User
import im.domain.wrappers.identifier.Identifier
import im.domain.wrappers.name.Name
import im.repository.jpa.model.channel.ChannelDTO
import im.repository.jpa.model.channel.ChannelMemberDTO
import im.repository.jpa.model.channel.ChannelMemberId
import im.repository.jpa.model.channel.ChannelRoleDTO
import im.repository.jpa.repositories.jpa.JpaRepositoryUtils
import im.repository.pagination.Pagination
import im.repository.pagination.PaginationRequest
import im.repository.pagination.SortRequest
import im.repository.repositories.channel.ChannelRepository
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component

@Component
@Primary
class ChannelRepositoryImpl(
    private val channelRepositoryJpa: ChannelRepositoryJpa,
    private val channelMemberRepositoryJpa: ChannelMemberRepositoryJpa,
    private val utils: JpaRepositoryUtils,
) : ChannelRepository {
    override fun findByName(
        name: Name,
        filterPublic: Boolean,
    ): Channel? = channelRepositoryJpa.findByName(name.value, filterPublic)?.toDomain()

    override fun findByPartialName(
        name: String,
        filterPublic: Boolean,
        pagination: PaginationRequest,
        sortRequest: SortRequest,
    ): Pagination<Channel> {
        val pageable = utils.toPageRequest(pagination, sortRequest)
        val res =
            if (pagination.getCount) {
                channelRepositoryJpa.findByPartialName(name, filterPublic, pageable)
            } else {
                channelRepositoryJpa.findByPartialNameSliced(name, filterPublic, pageable)
            }
        return Pagination(res.content.map { it.toDomain() }, utils.getPaginationInfo(res))
    }

    override fun find(
        pagination: PaginationRequest,
        filterPublic: Boolean,
        sortRequest: SortRequest,
        after: Identifier,
    ): Pagination<Channel> {
        val res =
            if (pagination.getCount) {
                channelRepositoryJpa.findAll(utils.toPageRequest(pagination, sortRequest), filterPublic, after.value)
            } else {
                channelRepositoryJpa.findBy(utils.toPageRequest(pagination, sortRequest), filterPublic, after.value)
            }
        return Pagination(res.content.map { it.toDomain() }, utils.getPaginationInfo(res))
    }

    override fun getMember(
        channel: Channel,
        user: User,
    ): Pair<User, ChannelRole>? =
        channelMemberRepositoryJpa.findByChannelIdAndUserId(channel.id.value, user.id.value)?.let {
            it.user.toDomain() to it.role.toDomain()
        }

    override fun findByOwner(
        user: User,
        pagination: PaginationRequest,
        sortRequest: SortRequest,
        after: Identifier,
    ): Pagination<Channel> {
        val pageable = utils.toPageRequest(pagination, sortRequest)
        val res =
            if (pagination.getCount) {
                channelRepositoryJpa.findByOwnerId(user.id.value, pageable, after.value)
            } else {
                channelRepositoryJpa.findByOwnerIdSliced(user.id.value, pageable, after.value)
            }
        return Pagination(res.content.map { it.toDomain() }, utils.getPaginationInfo(res))
    }

    override fun findByMember(
        user: User,
        pagination: PaginationRequest,
        sortRequest: SortRequest,
        after: Identifier,
    ): Pagination<Channel> {
        val pageable = utils.toPageRequest(pagination, sortRequest)
        val res =
            if (pagination.getCount) {
                channelMemberRepositoryJpa.findByUserId(user.id.value, pageable, after.value)
            } else {
                channelMemberRepositoryJpa.findByUserIdSliced(user.id.value, pageable, after.value)
            }
        return Pagination(res.content.map { it.toDomain() }, utils.getPaginationInfo(res))
    }

    override fun addMember(
        channel: Channel,
        user: User,
        role: ChannelRole,
    ) {
        channelMemberRepositoryJpa.save(ChannelMemberDTO.fromDomain(channel, user, role))
    }

    override fun removeMember(
        channel: Channel,
        user: User,
    ) {
        channelMemberRepositoryJpa.deleteById(ChannelMemberId(channel.id.value, user.id.value))
    }

    override fun updateMemberRole(
        channel: Channel,
        user: User,
        role: ChannelRole,
    ) {
        channelMemberRepositoryJpa.updateById(ChannelMemberId(channel.id.value, user.id.value), ChannelRoleDTO.fromDomain(role))
    }

    override fun save(entity: Channel): Channel = channelRepositoryJpa.save(ChannelDTO.fromDomain(entity)).toDomain()

    override fun saveAll(entities: Iterable<Channel>): List<Channel> =
        channelRepositoryJpa
            .saveAll(
                entities.map {
                    ChannelDTO.fromDomain(it)
                },
            ).map { it.toDomain() }

    override fun findById(id: Identifier): Channel? =
        channelRepositoryJpa
            .findById(id.value)
            .map {
                it.toDomain()
            }.orElse(null)

    override fun findAll(): List<Channel> = channelRepositoryJpa.findAll().map { it.toDomain() }

    override fun find(
        pagination: PaginationRequest,
        sortRequest: SortRequest,
    ): Pagination<Channel> {
        val pageable = utils.toPageRequest(pagination, sortRequest)
        if (pagination.getCount) {
            val res = channelRepositoryJpa.findAll(pageable)
            return Pagination(res.content.map { it.toDomain() }, utils.getPaginationInfo(res))
        } else {
            val res = channelRepositoryJpa.findBy(pageable, false, 0)
            return Pagination(res.content.map { it.toDomain() }, utils.getPaginationInfo(res))
        }
    }

    override fun findAllById(ids: Iterable<Identifier>): List<Channel> =
        channelRepositoryJpa
            .findAllById(
                ids.map {
                    it.value
                },
            ).map { it.toDomain() }

    override fun deleteById(id: Identifier) {
        channelRepositoryJpa.deleteById(id.value)
    }

    override fun existsById(id: Identifier): Boolean = channelRepositoryJpa.existsById(id.value)

    override fun count(): Long = channelRepositoryJpa.count()

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
        channelRepositoryJpa.flush()
        channelMemberRepositoryJpa.flush()
    }
}
