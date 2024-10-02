package model.channel

import channel.ChannelMember
import jakarta.persistence.Entity
import model.user.UserDTO

import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction

@Entity
@Table(name = "channelMember")
data class ChannelMemberDTO(
    @EmbeddedId
    val id: ChannelMemberID = ChannelMemberID(),

    @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @MapsId("channelId")
    @JoinColumn(name = "channel_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    val channel: ChannelDTO? = ChannelDTO(),

    @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    val user: UserDTO? = UserDTO(),

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    val role: ChannelRoleDTO? = ChannelRoleDTO.MEMBER
) {
    companion object {
        fun fromDomain(channelMember: ChannelMember): ChannelMemberDTO = ChannelMemberDTO(
            id = ChannelMemberID(channelMember.channel.id, channelMember.user.id),
            channel = ChannelDTO.fromDomain(channelMember.channel),
            user = UserDTO.fromDomain(channelMember.user),
            role = ChannelRoleDTO.valueOf(channelMember.role.name)
        )
    }

    fun toDomain(): ChannelMember = ChannelMember(
        channel = channel!!.toDomain(),
        user = user!!.toDomain(),
        role = role!!.toDomain()
    )
}