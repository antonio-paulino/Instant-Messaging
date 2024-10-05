package transactions

import channel.ChannelRepository
import invitations.ChannelInvitationRepository
import invitations.ImInvitationRepository
import sessions.SessionRepository
import tokens.AccessTokenRepository
import tokens.RefreshTokenRepository
import user.UserRepository

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
    val accessTokenRepository: AccessTokenRepository
    val refreshTokenRepository: RefreshTokenRepository
    val imInvitationRepository: ImInvitationRepository
    val channelInvitationRepository: ChannelInvitationRepository
}