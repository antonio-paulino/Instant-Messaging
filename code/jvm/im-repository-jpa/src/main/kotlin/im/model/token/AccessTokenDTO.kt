package im.model.token

import im.model.session.SessionDTO
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