package im.repository.mem.transactions

import im.repository.repositories.channel.ChannelRepository
import im.repository.repositories.invitations.ChannelInvitationRepository
import im.repository.repositories.invitations.ImInvitationRepository
import im.repository.repositories.messages.MessageRepository
import im.repository.repositories.sessions.SessionRepository
import im.repository.repositories.tokens.AccessTokenRepository
import im.repository.repositories.tokens.RefreshTokenRepository
import im.repository.repositories.transactions.Transaction
import im.repository.repositories.user.UserRepository

class MemTransaction(
    override val channelRepository: ChannelRepository,
    override val userRepository: UserRepository,
    override val sessionRepository: SessionRepository,
    override val messageRepository: MessageRepository,
    override val accessTokenRepository: AccessTokenRepository,
    override val refreshTokenRepository: RefreshTokenRepository,
    override val imInvitationRepository: ImInvitationRepository,
    override val channelInvitationRepository: ChannelInvitationRepository
) : Transaction {


    override fun rollback() {
        // no-op
    }
}
