package im

import im.repository.jpa.transactions.TransactionManagerJpa
import im.repository.mem.transactions.MemTransactionManager
import im.repository.repositories.transactions.TransactionManager
import im.services.auth.AuthConfig
import im.services.auth.PasswordEncoderSHA256
import im.services.invitations.InvitationConfig
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

@Component
open class TestConfig(
    private val transactionManagerJpa: TransactionManagerJpa,
) {
    private val memTransactionManager = MemTransactionManager()

    @Profile("Jpa")
    @Bean
    @Primary
    fun trxManagerJpa(): TransactionManager = transactionManagerJpa

    @Profile("inMem")
    @Bean
    @Primary
    fun trxManagerInMem(): TransactionManager = memTransactionManager

    @Bean
    open fun authConfig() =
        AuthConfig(
            accessTokenTTL = 24.hours,
            sessionTTL = 7.days,
            maxSessions = 10,
        )

    @Bean
    open fun passwordEncoder() = PasswordEncoderSHA256()

    @Bean
    open fun invitationConfig() =
        InvitationConfig(
            minImInvitationTTL = 15.minutes,
            defaultImInvitationTTL = 1.days,
            maxImInvitationTTL = 7.days,
            minChannelInvitationTTL = 15.minutes,
            maxChannelInvitationTTL = 30.days,
        )
}
