package im.api.controllers

import im.api.middlewares.authentication.Authenticated
import im.api.model.output.IdentifierOutputModel
import im.api.model.output.channel.ChannelOutputModel
import im.api.model.output.invitations.ChannelInvitationOutputModel
import im.api.model.output.messages.MessageOutputModel
import im.domain.Success
import im.domain.channel.Channel
import im.domain.channel.ChannelMember
import im.domain.invitations.ChannelInvitation
import im.domain.messages.Message
import im.domain.user.AuthenticatedUser
import im.domain.wrappers.identifier.Identifier
import im.repository.repositories.RepositoryEvent
import im.services.channels.ChannelService
import jakarta.annotation.PreDestroy
import org.springframework.context.ApplicationEvent
import org.springframework.context.ApplicationListener
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter.SseEventBuilder
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

@RestController
@Authenticated
class SSEController(
    private val channelService: ChannelService,
) : ApplicationListener<RepositoryHTTPEvent<*>> {
    private val listeners = ConcurrentHashMap<Identifier, CopyOnWriteArrayList<Listener>>() // Identified by user id
    private val eventStore = mutableMapOf<String, UserEvents>() // Identified by event id
    private val lock = ReentrantLock()

    private var eventId = 0L

    private val eventExecutor =
        Executors
            .newScheduledThreadPool(1)
            .also {
                it.scheduleAtFixedRate({
                    keepAlive()
                }, 2, 2, TimeUnit.SECONDS)
            }

    private fun keepAlive() {
        sendEventToAll("keep-alive", null)
    }

    private fun sendEventToAll(
        eventName: String,
        eventData: Any?,
        identifiers: List<Identifier>? = null,
    ) {
        try {
            val listeners = identifiers?.mapNotNull { listeners[it] } ?: listeners.values
            if (!listeners.isEmpty()) {
                val eventId = lock.withLock { eventId++.toString() }
                listeners.forEach { it.forEach { listener -> listener(eventName, eventData, eventId) } }
            }
            if (eventName != "keep-alive") {
                storeEvent(eventId.toString(), eventName, eventData, identifiers)
            }
        } catch (_: Exception) {
            // no-op
        }
    }

    private fun removeListener(
        listeners: CopyOnWriteArrayList<Listener>,
        listener: Listener,
    ) {
        listeners.remove(listener)
    }

    private fun addListener(
        listeners: CopyOnWriteArrayList<Listener>,
        listener: Listener,
    ) {
        listeners.add(listener)
    }

    @GetMapping("/api/sse/listen")
    fun subscribeMessages(
        @RequestHeader("Last-Event-ID") lastEventId: String?,
        user: AuthenticatedUser,
    ): ResponseEntity<SseEmitter> {
        val listeners = listeners.computeIfAbsent(user.user.id) { CopyOnWriteArrayList() }
        val emitter = createEmitter(listeners)

        lastEventId?.let {
            resendMissedEvents(it, emitter, user.user.id)
        }

        return ResponseEntity
            .ok()
            .header("Content-Type", "text/event-stream")
            .header("Cache-Control", "no-cache")
            .header("Connection", "keep-alive")
            .header("Content-Encoding", "none")
            .header("Access-Control-Allow-Origin", "*")
            .header("X-Accel-Buffering", "no") // For Nginx
            .body(emitter)
    }

    private fun createEmitter(listeners: CopyOnWriteArrayList<Listener>): SseEmitter {
        val emitter = SseEmitter(TimeUnit.HOURS.toMillis(1))
        val listener: Listener = makeListener(emitter).also { addListener(listeners, it) }
        emitter.onCompletion { removeListener(listeners, listener) }
        emitter.onTimeout { removeListener(listeners, listener) }
        emitter.onError { removeListener(listeners, listener) }
        return emitter
    }

    private fun makeListener(emitter: SseEmitter): Listener {
        return { eventName, eventData, eventId ->
            synchronized(emitter) {
                try {
                    emitter.send(
                        SseEmitter
                            .event()
                            .name(eventName)
                            .id(eventId)
                            .also { if (eventData != null) it.data(eventData) },
                    )
                } catch (ex: Exception) {
                    emitter.completeWithError(ex)
                }
            }
        }
    }

    private fun resendMissedEvents(
        lastEventId: String,
        emitter: SseEmitter,
        userId: Identifier,
    ) {
        val missedEvents = getMissedEventsSince(lastEventId, userId)
        missedEvents.forEach { event ->
            synchronized(emitter) {
                try {
                    emitter.send(event.first)
                } catch (ex: Exception) {
                    emitter.completeWithError(ex)
                }
            }
        }
    }

    // In memory storage of events
    // Meant for short term connection loss recovery
    // For example, when the emitter times out and the client reconnects
    private fun storeEvent(
        eventId: String,
        eventName: String,
        eventData: Any?,
        userIds: List<Identifier>?,
    ) {
        lock.withLock {
            val newEvent =
                SseEmitter
                    .event()
                    .name(eventName)
                    .id(eventId)
                    .also { if (eventData != null) it.data(eventData) }
            eventStore[eventId] = newEvent to userIds.orEmpty()
            if (eventStore.size > MAX_EVENTS_STORED) {
                eventStore.keys.sorted().take(EVENTS_CLEANUP_SIZE).forEach { eventStore.remove(it) }
            }
        }
    }

    private fun getMissedEventsSince(
        lastEventId: String,
        userId: Identifier,
    ): List<UserEvents> {
        lock.withLock {
            return eventStore.mapNotNull {
                if (it.key.toLong() > lastEventId.toLong() && userId in it.value.second) {
                    it.value
                } else {
                    null
                }
            }
        }
    }

    private fun memberIds(channelId: Identifier): List<Identifier> {
        val members = channelService.getChannelMembers(channelId) as Success? ?: return emptyList()
        return members.value.map { it.key.id }
    }

    @PreDestroy
    fun destroy() {
        eventExecutor.awaitTermination(AWAIT_TERMINATION_TIMEOUT, TimeUnit.SECONDS)
        eventExecutor.shutdown()
    }

    override fun onApplicationEvent(event: RepositoryHTTPEvent<*>) {
        when (event.repositoryEvent) {
            is RepositoryEvent.EntityPersisted -> handleEntityPersisted(event.repositoryEvent.entity)
            is RepositoryEvent.EntityUpdated -> handleEntityUpdated(event.repositoryEvent.entity)
            is RepositoryEvent.EntityRemoved -> handleEntityRemoved(event.repositoryEvent.entity)
        }
    }

    private fun handleEntityPersisted(entity: Any?) {
        when (entity) {
            is ChannelInvitation ->
                sendEventToAll(
                    INVITATION_CREATED_EVENT_NAME,
                    ChannelInvitationOutputModel.fromDomain(entity),
                    listOf(entity.invitee.id),
                )
            is Message ->
                sendEventToAll(
                    MESSAGE_CREATED_EVENT_NAME,
                    MessageOutputModel.fromDomain(entity),
                    memberIds(entity.channelId),
                )
            is ChannelMember -> {
                sendEventToAll(
                    CHANNEL_UPDATED_EVENT_NAME,
                    ChannelOutputModel.fromDomain(entity.channel.addMember(entity.user, entity.role)),
                    if (entity.channel.isPublic) null else entity.channel.members.map { it.key.id },
                )
            }
            is Channel ->
                sendEventToAll(
                    CHANNEL_CREATED_EVENT_NAME,
                    ChannelOutputModel.fromDomain(entity),
                    if (entity.isPublic) null else entity.members.map { it.key.id },
                )
        }
    }

    private fun handleEntityUpdated(entity: Any?) {
        when (entity) {
            is Channel ->
                sendEventToAll(
                    CHANNEL_UPDATED_EVENT_NAME,
                    ChannelOutputModel.fromDomain(entity),
                    if (entity.isPublic) null else entity.members.map { it.key.id },
                )
            is ChannelInvitation ->
                sendEventToAll(
                    INVITATION_UPDATED_EVENT_NAME,
                    ChannelInvitationOutputModel.fromDomain(entity),
                    listOf(entity.invitee.id, entity.inviter.id),
                )
            is Message ->
                sendEventToAll(
                    MESSAGE_UPDATED_EVENT_NAME,
                    MessageOutputModel.fromDomain(entity),
                    memberIds(entity.channelId),
                )
            is ChannelMember -> {
                sendEventToAll(
                    CHANNEL_UPDATED_EVENT_NAME,
                    ChannelOutputModel.fromDomain(entity.channel.addMember(entity.user, entity.role)),
                    if (entity.channel.isPublic) null else entity.channel.members.map { it.key.id },
                )
            }
        }
    }

    private fun handleEntityRemoved(entity: Any?) {
        when (entity) {
            is Channel -> {
                sendEventToAll(
                    CHANNEL_DELETED_EVENT_NAME,
                    IdentifierOutputModel.fromDomain(entity.id),
                    if (entity.isPublic) null else entity.members.map { it.key.id },
                )
            }
            is ChannelInvitation ->
                sendEventToAll(
                    INVITATION_DELETED_EVENT_NAME,
                    IdentifierOutputModel.fromDomain(entity.id),
                    listOf(entity.invitee.id, entity.inviter.id),
                )
            is Message ->
                sendEventToAll(
                    MESSAGE_DELETED_EVENT_NAME,
                    IdentifierOutputModel.fromDomain(entity.id),
                    memberIds(entity.channelId),
                )
            is ChannelMember -> {
                sendEventToAll(
                    CHANNEL_UPDATED_EVENT_NAME,
                    ChannelOutputModel.fromDomain(entity.channel.removeMember(entity.user)),
                    if (entity.channel.isPublic) null else entity.channel.members.map { it.key.id },
                )
            }
        }
    }

    private companion object {
        private const val MESSAGE_CREATED_EVENT_NAME = "message-created"
        private const val MESSAGE_UPDATED_EVENT_NAME = "message-updated"
        private const val MESSAGE_DELETED_EVENT_NAME = "message-deleted"
        private const val INVITATION_CREATED_EVENT_NAME = "invitation-created"
        private const val INVITATION_UPDATED_EVENT_NAME = "invitation-updated"
        private const val INVITATION_DELETED_EVENT_NAME = "invitation-deleted"
        private const val CHANNEL_CREATED_EVENT_NAME = "channel-created"
        private const val CHANNEL_DELETED_EVENT_NAME = "channel-deleted"
        private const val CHANNEL_UPDATED_EVENT_NAME = "channel-updated"
        private const val MAX_EVENTS_STORED = 1000000
        private const val EVENTS_CLEANUP_SIZE = 100000
        private const val AWAIT_TERMINATION_TIMEOUT = 5L
    }
}

typealias EventType = String
typealias Payload = Any?
typealias EventId = String

typealias Listener = (EventType, Payload, EventId) -> Unit

typealias UserEvents = Pair<SseEventBuilder, List<Identifier>>

/**
 * Event to be received by the SSE controller, wrapping a repository event.
 */
class RepositoryHTTPEvent<T>(
    val repositoryEvent: RepositoryEvent<T>,
) : ApplicationEvent(repositoryEvent)
