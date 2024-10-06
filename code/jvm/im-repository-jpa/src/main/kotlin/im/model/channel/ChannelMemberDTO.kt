package im.model.channel

import im.model.user.UserDTO
import jakarta.persistence.Column
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.MapsId
import jakarta.persistence.Table

@Entity
@Table(name = "channel_member")
open class ChannelMemberDTO(
    @EmbeddedId
    open val id: ChannelMemberId? = null,

    @ManyToOne
    @MapsId("channelID")
    @JoinColumn(name = "channel_id", nullable = false)
    open val channel: ChannelDTO = ChannelDTO(),

    @ManyToOne
    @MapsId("userID")
    @JoinColumn(name = "user_id", nullable = false)
    open val user: UserDTO = UserDTO(),

    @Column(name = "role")
    open val role: ChannelRoleDTO = ChannelRoleDTO.MEMBER
)