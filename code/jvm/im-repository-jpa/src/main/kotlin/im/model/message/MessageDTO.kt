package im.model.message

import jakarta.persistence.*
import im.messages.Message
import im.model.channel.ChannelDTO
import im.model.user.UserDTO
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.time.LocalDateTime

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
