package im.proxy

import im.api.controllers.RepositoryHTTPEvent
import im.repository.jpa.repositories.jpa.RepositoryEventJpa
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import kotlin.jvm.java

/**
 * This class listens for Repository JPA events and publishes them as HTTP events.
 *
 * The JPA implementation has its own event class that implements
 * the application event interface. Since we shouldn't expose the JPA
 * implementation directly to the HTTP layer, we use this
 * class as a proxy to convert the JPA events to HTTP events,
 * which are listened to by the HTTP layer.
 *
 * It is used to bridge the gap between the JPA and HTTP layers.
 *
 * @param applicationEventPublisher The application event publisher.
 */
@Component
class JpaToHttpEventProxy(
    private val applicationEventPublisher: ApplicationEventPublisher,
) : ApplicationListener<RepositoryEventJpa<*>> {
    override fun onApplicationEvent(event: RepositoryEventJpa<*>) {
        logger.info("Received JPA event, propagating to HTTP layer")
        applicationEventPublisher.publishEvent(RepositoryHTTPEvent(event.entity))
    }

    companion object {
        private val logger = LoggerFactory.getLogger(JpaToHttpEventProxy::class.java)
    }
}
