package im.repository.jpa.repositories.jpa

import im.repository.repositories.RepositoryEvent
import org.springframework.context.ApplicationEvent

/**
 * JPA implementation of [RepositoryEvent] that
 * can be used to publish events to the application context.
 */
class RepositoryEventJpa<T>(
    val entity: RepositoryEvent<T>,
) : ApplicationEvent(entity)
