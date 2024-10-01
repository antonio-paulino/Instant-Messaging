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
import tokens.RefreshToken
import java.util.UUID

@Entity
@Table(name = "RefreshToken")
data class RefreshTokenDTO(
    @Id
    @Column(nullable = false, length = 32)
    val token: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    val session: SessionDTO? = null
) {
    companion object {
        fun fromDomain(token: RefreshToken): RefreshTokenDTO =
            RefreshTokenDTO(token.token, SessionDTO.fromDomain(token.session))
    }

    fun toDomain(): RefreshToken = RefreshToken(token, session!!.toDomain())
}
