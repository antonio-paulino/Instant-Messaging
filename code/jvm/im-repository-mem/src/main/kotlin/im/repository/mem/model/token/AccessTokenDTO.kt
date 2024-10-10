package im.repository.mem.model.token

import im.repository.mem.model.session.SessionDTO
import im.tokens.AccessToken
import java.time.LocalDateTime
import java.util.*

data class AccessTokenDTO(
    val token: UUID = UUID.randomUUID(),
    val session: SessionDTO,
    val expiresAt: LocalDateTime = LocalDateTime.now().plusDays(1)
) {
    companion object {
        fun fromDomain(token: AccessToken): AccessTokenDTO =
            AccessTokenDTO(
                token.token,
                SessionDTO.fromDomain(token.session),
                token.expiresAt
            )
    }

    fun toDomain(): AccessToken = AccessToken(token, session.toDomain(), expiresAt)
}