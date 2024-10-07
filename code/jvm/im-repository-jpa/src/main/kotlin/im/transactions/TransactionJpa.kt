package im.transactions

import im.repositories.channel.ChannelRepository
import im.repositories.invitations.ChannelInvitationRepository
import im.repositories.invitations.ImInvitationRepository
import im.repositories.transactions.Transaction
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionStatus
import im.repositories.sessions.SessionRepository
import im.repositories.tokens.AccessTokenRepository
import im.repositories.tokens.RefreshTokenRepository
import im.repositories.user.UserRepository

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
) : Transaction {

    override fun rollback() {
        manager.rollback(transaction)
    }
}