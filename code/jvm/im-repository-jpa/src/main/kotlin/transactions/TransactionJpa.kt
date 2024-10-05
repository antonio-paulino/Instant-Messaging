package transactions

import channel.ChannelRepository
import invitations.ChannelInvitationRepository
import invitations.ImInvitationRepository
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionStatus
import sessions.SessionRepository
import tokens.AccessTokenRepository
import tokens.RefreshTokenRepository
import user.UserRepository

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