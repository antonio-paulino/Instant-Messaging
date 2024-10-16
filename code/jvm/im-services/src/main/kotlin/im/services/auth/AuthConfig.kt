package im.services.auth

import kotlin.time.Duration

/**
 * Configuration for the authentication service.
 *
 * Contains a set of configuration values that are used to validate
 * business rules in the authentication service.
 *
 * @param accessTokenTTL the access token time-to-live
 * @param sessionTTL the session time-to-live
 * @param maxSessions the maximum number of active sessions per user
 */
data class AuthConfig(
    val accessTokenTTL: Duration,
    val sessionTTL: Duration,
    val maxSessions: Int,
)
