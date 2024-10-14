package im.api.config

import im.domain.user.User
import im.repository.repositories.transactions.TransactionManager
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Configuration
open class StartupConfig(
    private val transactionManager: TransactionManager,
) {
    @Bean
    open fun init() =
        CommandLineRunner {
            transactionManager.run {
                if (userRepository.count() == 0L) {
                    logger.info("Creating default user")
                    userRepository.save(
                        User(
                            0L,
                            "Instant Messaging",
                            "dB0fnKFopK61h3ebruE1Sw==:pM85KqazkHyBn7iUW1-ndtm-EMIxBIAFpPMPnl9n7N8=",
                            "iseldaw@isel.pt",
                        ),
                    )
                }
            }
        }

    companion object {
        private val logger = LoggerFactory.getLogger(StartupConfig::class.java)
    }
}

@Component
class RepositoriesCleanTask(
    private val transactionManager: TransactionManager,
) {
    init {
        logger.info("RepositoriesCleanTask initialized")
    }

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
