package im.controllers


import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import im.model.input.body.InvitationInputModel
import im.services.invitations.InvitationService

@RestController
class InvitationsController(private val invitationService: InvitationService) {

    @PostMapping("/channels/{channelId}/invitations")
    fun createInvitation(
        @PathVariable channelId: String,
        @RequestBody invitationInput: InvitationInputModel
    ) {
        invitationService.createInvitation(
            channelId.toLong(),
            invitationInput.userId.toLong(),
            invitationInput.expiration,
            invitationInput.role
        )
    }

    @GetMapping("/channels/{channelId}/invitations")
    fun getChannelInvitations(
        @PathVariable channelId: String
    ) {
        invitationService.getChannelInvitations(channelId.toLong())
    }

    @PatchMapping("/channels/{channelId}/invitations/{invitationId}")
    fun updateInvitation(
        @PathVariable channelId: String,
        @PathVariable invitationId: String,
        @RequestBody invitationInput: InvitationInputModel
    ) {
        invitationService.updateInvitation(
            channelId.toLong(),
            invitationId.toLong(),
            invitationInput.userId.toLong(),
            invitationInput.expiration,
            invitationInput.role
        )
    }

    @DeleteMapping("/channels/{channelId}/invitations/{invitationId}")
    fun deleteInvitation(
        @PathVariable channelId: String,
        @PathVariable invitationId: String
    ) {
        invitationService.deleteInvitation(channelId.toLong(), invitationId.toLong())
    }

    @GetMapping("/users/{userId}/invitations")
    fun getUserInvitations(
        @PathVariable userId: String
    ) {
        invitationService.getUserInvitations(userId.toLong())
    }

    @PatchMapping("/users/{userId}/invitations/{invitationId}")
    fun acceptInvitation(
        @PathVariable userId: String,
        @PathVariable invitationId: String
    ) {
        invitationService.acceptInvitation(userId.toLong(), invitationId.toLong())
    }

}