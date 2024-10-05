package model.token

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import model.session.SessionDTO
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import tokens.AccessToken
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "access_token")
data class AccessTokenDTO(
    @Id
    @Column(nullable = false, length = 32)
    val token: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    val session: SessionDTO? = null,

    @Column(name = "expires_at", nullable = false)
    val expiresAt: LocalDateTime = LocalDateTime.now().plusDays(1)
) {
    companion object {
        fun fromDomain(token: AccessToken): AccessTokenDTO =
            AccessTokenDTO(token.token, SessionDTO.fromDomain(token.session), token.expiresAt)
    }

    fun toDomain(): AccessToken = AccessToken(token, session!!.toDomain(), expiresAt)
}