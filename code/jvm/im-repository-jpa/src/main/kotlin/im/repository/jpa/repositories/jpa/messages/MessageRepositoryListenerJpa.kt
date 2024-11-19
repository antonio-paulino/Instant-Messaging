package im.repository.jpa.repositories.jpa.messages

import im.repository.jpa.model.message.MessageDTO
import im.repository.jpa.repositories.jpa.RepositoryEventJpa
import im.repository.repositories.RepositoryEvent
import jakarta.persistence.PostPersist
import jakarta.persistence.PostRemove
import jakarta.persistence.PostUpdate
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class MessageRepositoryListenerJpa(
    private val applicationEventPublisher: ApplicationEventPublisher,
) {
    @PostPersist
    fun onMessagePersist(message: MessageDTO) {
        applicationEventPublisher.publishEvent(RepositoryEventJpa(RepositoryEvent.EntityPersisted(message.toDomain())))
    }

    @PostUpdate
    fun onMessageUpdate(message: MessageDTO) {
        applicationEventPublisher.publishEvent(RepositoryEventJpa(RepositoryEvent.EntityUpdated(message.toDomain())))
    }

    @PostRemove
    fun onMessageRemove(message: MessageDTO) {
        applicationEventPublisher.publishEvent(RepositoryEventJpa(RepositoryEvent.EntityRemoved(message.toDomain())))
    }
}
