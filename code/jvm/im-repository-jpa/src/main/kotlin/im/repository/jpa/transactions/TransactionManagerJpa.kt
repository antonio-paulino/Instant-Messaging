package im.repository.jpa.transactions

import im.repository.jpa.repositories.AccessTokenRepositoryImpl
import im.repository.jpa.repositories.ChannelInvitationRepositoryImpl
import im.repository.jpa.repositories.ChannelRepositoryImpl
import im.repository.jpa.repositories.ImInvitationRepositoryImpl
import im.repository.jpa.repositories.MessageRepositoryImpl
import im.repository.jpa.repositories.RefreshTokenRepositoryImpl
import im.repository.jpa.repositories.SessionRepositoryImpl
import im.repository.jpa.repositories.UserRepositoryImpl
import im.repository.repositories.transactions.Transaction
import im.repository.repositories.transactions.TransactionIsolation
import im.repository.repositories.transactions.TransactionManager
import org.hibernate.type.SerializationException
import org.springframework.stereotype.Component
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.DefaultTransactionDefinition
import org.slf4j.LoggerFactory

private const val MAX_SERIALIZABLE_RETRIES = 3

@Component
class TransactionManagerJpa(
    private val channelRepository: ChannelRepositoryImpl,
    private val userRepository: UserRepositoryImpl,
    private val sessionRepository: SessionRepositoryImpl,
    private val messageRepository: MessageRepositoryImpl,
    private val accessTokenRepository: AccessTokenRepositoryImpl,
    private val refreshTokenRepository: RefreshTokenRepositoryImpl,
    private val imInvitationRepository: ImInvitationRepositoryImpl,
    private val channelInvitationRepository: ChannelInvitationRepositoryImpl,
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
                logger.debug("Transaction failed", e)
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