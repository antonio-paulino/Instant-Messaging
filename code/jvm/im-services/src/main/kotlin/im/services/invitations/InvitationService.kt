package im.services.invitations

import jakarta.inject.Named

@Named
class InvitationService {

    fun createInvitation(toLong: Long, userId: Long, expirationDate: String, role: String) {
        TODO()
    }

    fun getChannelInvitations(toLong: Long) {
        TODO()
    }

    fun updateInvitation(toLong: Long, toLong1: Long, userId: Long, expirationDate: String, role: String) {
        TODO()
    }

    fun deleteInvitation(toLong: Long, toLong1: Long) {
        TODO()
    }

    fun getUserInvitations(toLong: Long) {
        TODO()
    }

    fun acceptInvitation(toLong: Long, toLong1: Long) {
        TODO()
    }
}