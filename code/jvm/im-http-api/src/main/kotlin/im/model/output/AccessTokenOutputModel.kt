package im.model.output

import im.tokens.AccessToken
import java.time.LocalDateTime
import java.util.*

data class AccessTokenOutputModel(
    val token: UUID,
    val expiresAt: LocalDateTime
) {
    companion object {
        fun fromDomain(accessToken: AccessToken): AccessTokenOutputModel {
            return AccessTokenOutputModel(
                token = accessToken.token,
                expiresAt = accessToken.expiresAt
            )
        }
    }
}