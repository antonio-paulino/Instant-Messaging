package im

import im.api.middlewares.authentication.AuthInterceptor
import im.api.middlewares.logging.LoggingInterceptor
import im.api.middlewares.resolvers.AuthenticatedUserArgumentResolver
import im.api.utils.RequestHelper
import im.repository.jpa.transactions.TransactionManagerJpa
import im.repository.mem.transactions.MemTransactionManager
import im.repository.repositories.transactions.TransactionManager
import im.services.auth.AuthConfig
import im.services.auth.AuthService
import im.services.auth.PasswordEncoderSHA256
import im.services.invitations.InvitationConfig
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

@Component
class TestConfig(
    private val transactionManagerJpa: TransactionManagerJpa,
) {
    @Profile("Jpa")
    @Bean
    @Primary
    fun trxManagerJpa(): TransactionManager = transactionManagerJpa

    @Profile("inMem")
    @Bean
    @Primary
    fun trxManagerInMem(): TransactionManager = MemTransactionManager()

    @Bean
    fun authConfig() =
        AuthConfig(
            accessTokenTTL = 24.hours,
            sessionTTL = 7.days,
            maxSessions = 10,
        )

    @Bean
    fun passwordEncoder() = PasswordEncoderSHA256()

    @Bean
    fun invitationConfig() =
        InvitationConfig(
            minImInvitationTTL = 15.minutes,
            defaultImInvitationTTL = 1.days,
            maxImInvitationTTL = 7.days,
            minChannelInvitationTTL = 15.minutes,
            maxChannelInvitationTTL = 30.days,
        )
}

@Component
class WebMvcConfig(
    private val authService: AuthService,
) : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(LoggingInterceptor())
        registry.addInterceptor(AuthInterceptor(authService, RequestHelper()))
    }

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(AuthenticatedUserArgumentResolver())
    }
}
