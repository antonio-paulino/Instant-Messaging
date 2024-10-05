package model.channel

import jakarta.persistence.Column
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.MapsId
import jakarta.persistence.Table
import model.user.UserDTO

@Entity
@Table(name = "channel_member")
data class ChannelMemberDTO(
    @EmbeddedId
    val id: ChannelMemberId? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("channelID")
    @JoinColumn(name = "channel_id", nullable = false)
    val channel: ChannelDTO = ChannelDTO(),

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userID")
    @JoinColumn(name = "user_id", nullable = false)
    val user: UserDTO = UserDTO(),

    @Column(name = "role")
    val role: ChannelRoleDTO = ChannelRoleDTO.MEMBER
)