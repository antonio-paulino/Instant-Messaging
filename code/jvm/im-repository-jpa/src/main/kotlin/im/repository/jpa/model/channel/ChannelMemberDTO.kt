package im.repository.jpa.model.channel

import im.repository.jpa.model.user.UserDTO
import jakarta.persistence.Column
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.MapsId
import jakarta.persistence.Table

/**
 * Represents a channel member in the database (join table for many-to-many relationship between channels and users).
 *
 *
 * @property id The unique identifier of the channel member.
 * @property channel The channel that the member belongs to.
 * @property user The user that is a member of the channel.
 * @property role The role of the user in the channel.
 */
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