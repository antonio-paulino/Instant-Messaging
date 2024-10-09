package im.repositories.transactions

import im.repositories.channel.ChannelRepository
import im.repositories.invitations.ChannelInvitationRepository
import im.repositories.invitations.ImInvitationRepository
import im.repositories.messages.MessageRepository
import im.repositories.sessions.SessionRepository
import im.repositories.tokens.AccessTokenRepository
import im.repositories.tokens.RefreshTokenRepository
import im.repositories.user.UserRepository

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