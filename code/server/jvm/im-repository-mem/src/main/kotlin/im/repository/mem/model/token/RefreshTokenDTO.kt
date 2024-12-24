package im.repository.mem.model.token

import im.domain.tokens.RefreshToken
import im.repository.mem.model.session.SessionDTO
import java.util.UUID

data class RefreshTokenDTO(
    val token: UUID = UUID.randomUUID(),
    val session: SessionDTO,
) {
    companion object {
        fun fromDomain(token: RefreshToken): RefreshTokenDTO =
            RefreshTokenDTO(
                token.token,
                SessionDTO.fromDomain(token.session),
            )
    }

    fun toDomain(): RefreshToken = RefreshToken(token, session.toDomain())
}
