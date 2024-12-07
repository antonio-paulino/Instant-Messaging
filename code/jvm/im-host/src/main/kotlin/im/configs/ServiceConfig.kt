package im.configs

import im.services.auth.AuthConfig
import im.services.auth.PasswordEncoderSHA256
import im.services.invitations.InvitationConfig
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

@Configuration
open class ServiceConfig {
    @Bean
    open fun authConfig() =
        AuthConfig(
            accessTokenTTL = 24.hours,
            sessionTTL = 7.days,
            maxSessions = 1000,
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
