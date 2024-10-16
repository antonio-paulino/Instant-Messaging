package im.tasks

import im.repository.repositories.transactions.TransactionManager
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class RepositoriesCleanTask(
    private val transactionManager: TransactionManager,
) {
    @Scheduled(cron = "0 0 0 * * 0")
    fun cleanUpExpiredEntries() {
        logger.info("Cleaning up expired entries")
        transactionManager.run {
            sessionRepository.deleteExpired()
            accessTokenRepository.deleteExpired()
            channelInvitationRepository.deleteExpired()
            imInvitationRepository.deleteExpired()
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(RepositoriesCleanTask::class.java)
    }
}
