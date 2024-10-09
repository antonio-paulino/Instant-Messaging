package im.transactions

import im.repositories.channel.ChannelRepository
import im.repositories.invitations.ChannelInvitationRepository
import im.repositories.invitations.ImInvitationRepository
import im.repositories.messages.MessageRepository
import im.repositories.transactions.Transaction
import im.repositories.transactions.TransactionIsolation
import im.repositories.transactions.TransactionManager
import org.hibernate.type.SerializationException
import org.springframework.stereotype.Component
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.DefaultTransactionDefinition
import im.repositories.sessions.SessionRepository
import im.repositories.tokens.AccessTokenRepository
import im.repositories.tokens.RefreshTokenRepository
import im.repositories.user.UserRepository
import org.slf4j.LoggerFactory

private const val MAX_SERIALIZABLE_RETRIES = 3

@Component
class TransactionManagerJpa(
    private val channelRepository: ChannelRepository,
    private val userRepository: UserRepository,
    private val sessionRepository: SessionRepository,
    private val messageRepository: MessageRepository,
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
            logger.debug("Starting transaction with isolation {}", isolation)
            try {
                val result = newTransaction(transaction).block()
                manager.commit(transaction)
                logger.debug("Transaction succeeded")
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
                logger.error("Transaction failed:", e)
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
            channelInvitationRepository,
            messageRepository
        )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(TransactionManagerJpa::class.java)
    }
}