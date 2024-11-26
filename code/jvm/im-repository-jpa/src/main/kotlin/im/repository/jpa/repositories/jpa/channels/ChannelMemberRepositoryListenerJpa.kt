package im.repository.jpa.repositories.jpa.channels

import im.repository.jpa.model.channel.ChannelMemberDTO
import im.repository.jpa.repositories.jpa.RepositoryEventJpa
import im.repository.repositories.RepositoryEvent
import jakarta.persistence.PostPersist
import jakarta.persistence.PostRemove
import org.springframework.context.ApplicationEventPublisher

class ChannelMemberRepositoryListenerJpa(
    private val applicationEventPublisher: ApplicationEventPublisher,
) {
    @PostPersist
    fun onMemberAdd(member: ChannelMemberDTO) {
        val channel = member.channel.toDomain().addMember(member.user.toDomain(), member.role.toDomain())
        applicationEventPublisher.publishEvent(RepositoryEventJpa(RepositoryEvent.EntityUpdated(channel)))
    }

    @PostRemove
    fun onMemberRemove(member: ChannelMemberDTO) {
        val channel = member.channel.toDomain().removeMember(member.user.toDomain())
        applicationEventPublisher.publishEvent(RepositoryEventJpa(RepositoryEvent.EntityUpdated(channel)))
    }
}
