package im.repository.jpa.transactions

import im.repository.repositories.channel.ChannelRepository
import im.repository.repositories.invitations.ChannelInvitationRepository
import im.repository.repositories.invitations.ImInvitationRepository
import im.repository.repositories.messages.MessageRepository
import im.repository.repositories.sessions.SessionRepository
import im.repository.repositories.tokens.AccessTokenRepository
import im.repository.repositories.tokens.RefreshTokenRepository
import im.repository.repositories.transactions.Transaction
import im.repository.repositories.user.UserRepository
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionStatus

class TransactionJpa(
    private val manager: PlatformTransactionManager,
    private val transaction: TransactionStatus,
    override val channelRepository: ChannelRepository,
    override val userRepository: UserRepository,
    override val sessionRepository: SessionRepository,
    override val accessTokenRepository: AccessTokenRepository,
    override val refreshTokenRepository: RefreshTokenRepository,
    override val imInvitationRepository: ImInvitationRepository,
    override val channelInvitationRepository: ChannelInvitationRepository,
    override val messageRepository: MessageRepository,
) : Transaction {
    override fun rollback() {
        manager.rollback(transaction)
    }
}
