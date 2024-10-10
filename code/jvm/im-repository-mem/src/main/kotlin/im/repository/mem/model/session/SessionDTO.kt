package im.repository.mem.model.session

import im.repository.mem.model.user.UserDTO
import im.sessions.Session
import java.time.LocalDateTime

data class SessionDTO(
    val id: Long = 0,
    val user: UserDTO,
    val expiresAt: LocalDateTime = LocalDateTime.now().plusDays(90),
) {
    companion object {
        fun fromDomain(session: Session) = SessionDTO(
            id = session.id.value,
            user = UserDTO.fromDomain(session.user),
            expiresAt = session.expiresAt,
        )
    }

    fun toDomain() = Session(
        id = id,
        user = user.toDomain(),
        expiresAt = expiresAt
    )
}