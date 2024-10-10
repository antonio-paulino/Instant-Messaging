package im.repository.jpa.model.message

import jakarta.persistence.*
import im.messages.Message
import im.repository.jpa.model.channel.ChannelDTO
import im.repository.jpa.model.user.UserDTO
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
 * @property channel The channel that the message belongs to.
 * @property user The user that sent the message.
 * @property content The content of the message.
 * @property createdAt The date and time when the message was created.
 * @property editedAt The date and time when the message was last edited.
 */
@Entity
@Table(name = "Message")
open class MessageDTO(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open val id: Long = 0,

    @ManyToOne
    @JoinColumn(name = "channel_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    open val channel: ChannelDTO? = null,

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    open val user: UserDTO? = null,

    @Column(nullable = false, columnDefinition = "TEXT")
    open val content: String = "",

    @Column(name = "created_at", nullable = false)
    open val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "edited_at")
    open val editedAt: LocalDateTime? = null
) {
    companion object {
        fun fromDomain(message: Message): MessageDTO = MessageDTO(
            id = message.id.value,
            channel = ChannelDTO.fromDomain(message.channel),
            user = UserDTO.fromDomain(message.user),
            content = message.content,
            createdAt = message.createdAt,
            editedAt = message.editedAt
        )
    }

    fun toDomain(): Message = Message(
        id = id,
        channel = channel!!.toDomain(),
        user = user!!.toDomain(),
        content = content,
        createdAt = createdAt,
        editedAt = editedAt
    )
}
