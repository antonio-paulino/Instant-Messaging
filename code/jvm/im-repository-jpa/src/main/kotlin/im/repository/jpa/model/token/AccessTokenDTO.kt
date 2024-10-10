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
import im.tokens.AccessToken
import java.time.LocalDateTime
import java.util.*

/**
 * Represents an access token in the database.
 *
 * - An access token is associated to a single session (many-to-one relationship).
 *
 * @property token The unique token of the access token.
 * @property session The session that the access token belongs to.
 * @property expiresAt The date and time when the access token expires.
 */
@Entity
@Table(name = "access_token")
open class AccessTokenDTO(
    @Id
    @Column(nullable = false, length = 32)
    open val token: UUID = UUID.randomUUID(),

    @ManyToOne
    @JoinColumn(name = "session_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    open val session: SessionDTO? = null,

    @Column(name = "expires_at", nullable = false)
    open val expiresAt: LocalDateTime = LocalDateTime.now().plusDays(1)
) {
    companion object {
        fun fromDomain(token: AccessToken): AccessTokenDTO =
            AccessTokenDTO(token.token, SessionDTO.fromDomain(token.session), token.expiresAt)
    }

    fun toDomain(): AccessToken = AccessToken(token, session!!.toDomain(), expiresAt)
}