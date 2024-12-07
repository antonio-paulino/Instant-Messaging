package im.repository.jpa.repositories.jpa.channels

import im.repository.jpa.model.channel.ChannelDTO
import im.repository.jpa.repositories.jpa.RepositoryEventJpa
import im.repository.repositories.RepositoryEvent
import jakarta.persistence.PostPersist
import jakarta.persistence.PostUpdate
import jakarta.persistence.PreRemove
import org.springframework.context.ApplicationEventPublisher

class ChannelRepositoryListenerJpa(
    private val applicationEventPublisher: ApplicationEventPublisher,
) {
    @PostPersist
    fun onChannelPersist(channel: ChannelDTO) {
        applicationEventPublisher.publishEvent(RepositoryEventJpa(RepositoryEvent.EntityPersisted(channel.toDomain())))
    }

    @PostUpdate
    fun onChannelUpdate(channel: ChannelDTO) {
        applicationEventPublisher.publishEvent(RepositoryEventJpa(RepositoryEvent.EntityUpdated(channel.toDomain())))
    }

    @PreRemove
    fun onChannelDelete(channel: ChannelDTO) {
        applicationEventPublisher.publishEvent(RepositoryEventJpa(RepositoryEvent.EntityRemoved(channel.toDomain())))
    }
}
