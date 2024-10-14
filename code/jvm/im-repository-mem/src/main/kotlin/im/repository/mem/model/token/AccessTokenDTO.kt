package im.repository.mem.model.token

import im.domain.tokens.AccessToken
import im.repository.mem.model.session.SessionDTO
import java.time.LocalDateTime
import java.util.UUID

data class AccessTokenDTO(
    val token: UUID = UUID.randomUUID(),
    val session: SessionDTO,
    val expiresAt: LocalDateTime = LocalDateTime.now().plusDays(1),
) {
    companion object {
        fun fromDomain(token: AccessToken): AccessTokenDTO =
            AccessTokenDTO(
                token.token,
                SessionDTO.fromDomain(token.session),
                token.expiresAt,
            )
    }

    fun toDomain(): AccessToken = AccessToken(token, session.toDomain(), expiresAt)
}
