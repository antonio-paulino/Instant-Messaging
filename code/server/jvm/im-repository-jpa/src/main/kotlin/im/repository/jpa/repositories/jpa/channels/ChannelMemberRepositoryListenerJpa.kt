package im.repository.jpa.repositories.jpa.channels

import im.repository.jpa.model.channel.ChannelMemberDTO
import im.repository.jpa.repositories.jpa.RepositoryEventJpa
import im.repository.repositories.RepositoryEvent
import jakarta.persistence.PostPersist
import jakarta.persistence.PostRemove
import jakarta.persistence.PostUpdate
import org.springframework.context.ApplicationEventPublisher

class ChannelMemberRepositoryListenerJpa(
    private val applicationEventPublisher: ApplicationEventPublisher,
) {
    @PostPersist
    fun onMemberAdd(member: ChannelMemberDTO) {
        val channelMember = member.toDomain()
        applicationEventPublisher.publishEvent(RepositoryEventJpa(RepositoryEvent.EntityPersisted(channelMember)))
    }

    @PostRemove
    fun onMemberRemove(member: ChannelMemberDTO) {
        val channelMember = member.toDomain()
        applicationEventPublisher.publishEvent(RepositoryEventJpa(RepositoryEvent.EntityRemoved(channelMember)))
    }

    @PostUpdate
    fun onMemberUpdate(member: ChannelMemberDTO) {
        val channelMember = member.toDomain()
        applicationEventPublisher.publishEvent(RepositoryEventJpa(RepositoryEvent.EntityUpdated(channelMember)))
    }
}
