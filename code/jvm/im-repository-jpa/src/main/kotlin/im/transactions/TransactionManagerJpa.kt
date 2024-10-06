package im.transactions

import im.channel.ChannelRepository
import im.invitations.ChannelInvitationRepository
import im.invitations.ImInvitationRepository
import org.hibernate.type.SerializationException
import org.springframework.stereotype.Component
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.DefaultTransactionDefinition
import im.sessions.SessionRepository
import im.tokens.AccessTokenRepository
import im.tokens.RefreshTokenRepository
import im.user.UserRepository

private const val MAX_SERIALIZABLE_RETRIES = 3

@Component
class TransactionManagerJpa(
    private val channelRepository: ChannelRepository,
    private val userRepository: UserRepository,
    private val sessionRepository: SessionRepository,
    private val accessTokenRepository: AccessTokenRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val imInvitationRepository: ImInvitationRepository,
    private val channelInvitationRepository: ChannelInvitationRepository,
    private val manager: PlatformTransactionManager
) : TransactionManager {

    override fun <T> run(block: Transaction.() -> T, isolation: TransactionIsolation): T {
        val definition = DefaultTransactionDefinition()
        definition.isolationLevel = convertIsolation(isolation)
        var tries = 1
        while (true) {
            val transaction = manager.getTransaction(definition)
            try {
                val result = newTransaction(transaction).block()
                manager.commit(transaction)
                return result
            } catch (e: Exception) {
                manager.rollback(transaction)
                if (isolation == TransactionIsolation.SERIALIZABLE &&
                    tries < MAX_SERIALIZABLE_RETRIES &&
                    e is SerializationException
                ) {
                    tries++
                    continue
                }
                throw e
            }
        }
    }

    private fun convertIsolation(isolation: TransactionIsolation): Int {
        return when (isolation) {
            TransactionIsolation.DEFAULT -> TransactionDefinition.ISOLATION_DEFAULT
            TransactionIsolation.READ_UNCOMMITTED -> TransactionDefinition.ISOLATION_READ_UNCOMMITTED
            TransactionIsolation.READ_COMMITTED -> TransactionDefinition.ISOLATION_READ_COMMITTED
            TransactionIsolation.REPEATABLE_READ -> TransactionDefinition.ISOLATION_REPEATABLE_READ
            TransactionIsolation.SERIALIZABLE -> TransactionDefinition.ISOLATION_SERIALIZABLE
        }
    }

    private fun newTransaction(transaction: TransactionStatus): Transaction {
        return TransactionJpa(
            manager,
            transaction,
            channelRepository,
            userRepository,
            sessionRepository,
            accessTokenRepository,
            refreshTokenRepository,
            imInvitationRepository,
            channelInvitationRepository
        )
    }
}