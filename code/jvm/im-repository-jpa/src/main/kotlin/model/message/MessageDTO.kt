package model.message

import jakarta.persistence.*
import messages.Message
import model.channel.ChannelDTO
import model.user.UserDTO
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.time.LocalDateTime

@Entity
@Table(name = "Message")
data class MessageDTO(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    val channel: ChannelDTO? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    val user: UserDTO? = null,

    @Column(nullable = false, columnDefinition = "TEXT")
    val content: String = "",

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "edited_at")
    val editedAt: LocalDateTime? = null
) {
    companion object {
        fun fromDomain(message: Message): MessageDTO = MessageDTO(
            id = message.id,
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
