package im.repository.jpa.model.token

import im.repository.jpa.model.session.SessionDTO
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import im.tokens.RefreshToken
import java.util.UUID

/**
 * Represents a refresh token in the database.
 *
 * - A refresh token is associated to a single session (many-to-one relationship).
 *
 * @property token The unique token of the refresh token.
 * @property session The session that the refresh token belongs to.
 */
@Entity
@Table(name = "refresh_token")
open class RefreshTokenDTO(
    @Id
    @Column(nullable = false, length = 32)
    open val token: UUID = UUID.randomUUID(),

    @ManyToOne
    @JoinColumn(name = "session_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    open val session: SessionDTO? = null
) {
    companion object {
        fun fromDomain(token: RefreshToken): RefreshTokenDTO =
            RefreshTokenDTO(token.token, SessionDTO.fromDomain(token.session))
    }

    fun toDomain(): RefreshToken = RefreshToken(token, session!!.toDomain())
}
