package im.transactions

import im.channel.ChannelRepository
import im.invitations.ChannelInvitationRepository
import im.invitations.ImInvitationRepository
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionStatus
import im.sessions.SessionRepository
import im.tokens.AccessTokenRepository
import im.tokens.RefreshTokenRepository
import im.user.UserRepository

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