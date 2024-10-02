package model.session

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import model.user.UserDTO
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import sessions.Session
import java.time.LocalDateTime

@Entity
@Table(name = "Session")
data class SessionDTO(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    val user: UserDTO? = null,

    @Column(name = "expires_at", nullable = false)
    val expiresAt: LocalDateTime = LocalDateTime.now().plusDays(90),
) {
    companion object {
        fun fromDomain(session: Session) = SessionDTO(
            id = session.id,
            user = UserDTO.fromDomain(session.user),
            expiresAt = session.expiresAt,
        )
    }

    fun toDomain() = Session(
        id = id,
        user = user!!.toDomain(),
        expiresAt = expiresAt
    )
}