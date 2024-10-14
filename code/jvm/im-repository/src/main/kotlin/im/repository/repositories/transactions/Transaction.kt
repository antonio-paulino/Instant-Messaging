package im.repository.repositories.transactions

import im.repository.repositories.channel.ChannelRepository
import im.repository.repositories.invitations.ChannelInvitationRepository
import im.repository.repositories.invitations.ImInvitationRepository
import im.repository.repositories.messages.MessageRepository
import im.repository.repositories.sessions.SessionRepository
import im.repository.repositories.tokens.AccessTokenRepository
import im.repository.repositories.tokens.RefreshTokenRepository
import im.repository.repositories.user.UserRepository

/**
 * Represents a transaction in DBMS.
 *
 * [PostgresSQL Transactions](https://www.postgresql.org/docs/current/tutorial-transactions.html)
 *
 * [Oracle Transactions](https://docs.oracle.com/en/database/oracle/oracle-database/21/cncpt/transactions.html)
 */
interface Transaction {
    /**
     * Rolls back the transaction.
     */
    fun rollback()

    val channelRepository: ChannelRepository
    val userRepository: UserRepository
    val sessionRepository: SessionRepository
    val messageRepository: MessageRepository
    val accessTokenRepository: AccessTokenRepository
    val refreshTokenRepository: RefreshTokenRepository
    val imInvitationRepository: ImInvitationRepository
    val channelInvitationRepository: ChannelInvitationRepository
}
