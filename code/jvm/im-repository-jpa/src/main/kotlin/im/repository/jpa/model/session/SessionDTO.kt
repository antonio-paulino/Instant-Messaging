package im.repository.jpa.model.session

import im.domain.sessions.Session
import im.repository.jpa.model.user.UserDTO
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.time.LocalDateTime

/**
 * Represents a session in the database.
 *
 * - A session is associated to a single user (many-to-one relationship).
 *
 * @property id The unique identifier of the session.
 * @property user The user that the session belongs to.
 * @property expiresAt The date and time when the session expires.
 */
@Entity
@Table(name = "Session")
open class SessionDTO(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open val id: Long = 0,
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    open val user: UserDTO? = null,
    @Column(name = "expires_at", nullable = false)
    open val expiresAt: LocalDateTime = LocalDateTime.now().plusDays(90),
) {
    companion object {
        fun fromDomain(session: Session) =
            SessionDTO(
                id = session.id.value,
                user = UserDTO.fromDomain(session.user),
                expiresAt = session.expiresAt,
            )
    }

    fun toDomain() =
        Session(
            id = id,
            user = user!!.toDomain(),
            expiresAt = expiresAt,
        )
}
