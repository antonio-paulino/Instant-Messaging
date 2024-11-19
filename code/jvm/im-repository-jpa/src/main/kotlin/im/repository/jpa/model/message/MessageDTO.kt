package im.repository.jpa.model.message

import im.domain.messages.Message
import im.repository.jpa.model.user.UserDTO
import im.repository.jpa.repositories.jpa.messages.MessageRepositoryListenerJpa
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
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
 * Represents a message in the database.
 *
 * - A message is associated to a single channel (many-to-one relationship).
 * - A message is associated to a single user (many-to-one relationship).
 *
 * @property id The unique identifier of the message.
 * @property channelId The unique identifier of the channel where the message was sent.
 * @property user The user that sent the message.
 * @property content The content of the message.
 * @property createdAt The date and time when the message was created.
 * @property editedAt The date and time when the message was last edited.
 */
@Entity
@Table(name = "Message")
@EntityListeners(MessageRepositoryListenerJpa::class)
open class MessageDTO(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open val id: Long = 0,
    @Column(name = "channel_id", nullable = false)
    open val channelId: Long = 0,
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    open val user: UserDTO? = null,
    @Column(nullable = false, columnDefinition = "TEXT")
    open val content: String = "",
    @Column(name = "created_at", nullable = false)
    open val createdAt: LocalDateTime = LocalDateTime.now(),
    @Column(name = "edited_at")
    open val editedAt: LocalDateTime? = null,
) {
    companion object {
        fun fromDomain(message: Message): MessageDTO =
            MessageDTO(
                id = message.id.value,
                channelId = message.channelId.value,
                user = UserDTO.fromDomain(message.user),
                content = message.content,
                createdAt = message.createdAt,
                editedAt = message.editedAt,
            )
    }

    fun toDomain(): Message =
        Message(
            id = id,
            channelId = channelId,
            user = user!!.toDomain(),
            content = content,
            createdAt = createdAt,
            editedAt = editedAt,
        )
}
