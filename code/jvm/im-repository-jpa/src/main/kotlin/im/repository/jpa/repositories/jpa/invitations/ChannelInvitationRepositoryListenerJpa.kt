package im.repository.jpa.repositories.jpa.invitations

import im.repository.jpa.model.invitation.ChannelInvitationDTO
import im.repository.jpa.repositories.jpa.RepositoryEventJpa
import im.repository.repositories.RepositoryEvent
import jakarta.persistence.PostPersist
import jakarta.persistence.PostRemove
import jakarta.persistence.PostUpdate
import org.springframework.context.ApplicationEventPublisher

class ChannelInvitationRepositoryListenerJpa(
    private val applicationEventPublisher: ApplicationEventPublisher,
) {
    @PostPersist
    fun onChannelInvitationPersist(channelInvitation: ChannelInvitationDTO) {
        applicationEventPublisher.publishEvent(RepositoryEventJpa(RepositoryEvent.EntityPersisted(channelInvitation.toDomain())))
    }

    @PostUpdate
    fun onChannelInvitationUpdate(channelInvitation: ChannelInvitationDTO) {
        applicationEventPublisher.publishEvent(RepositoryEventJpa(RepositoryEvent.EntityUpdated(channelInvitation.toDomain())))
    }

    @PostRemove
    fun onChannelInvitationRemove(channelInvitation: ChannelInvitationDTO) {
        applicationEventPublisher.publishEvent(RepositoryEventJpa(RepositoryEvent.EntityRemoved(channelInvitation.toDomain())))
    }
}
