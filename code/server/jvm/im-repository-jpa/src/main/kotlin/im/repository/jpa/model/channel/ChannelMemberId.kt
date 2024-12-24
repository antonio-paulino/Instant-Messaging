package im.repository.jpa.model.channel

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.io.Serializable

@Embeddable
open class ChannelMemberId(
    @Column(name = "channel_id")
    open val channelID: Long = 0,
    @Column(name = "user_id")
    open val userID: Long = 0,
) : Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ChannelMemberId) return false

        if (channelID != other.channelID) return false
        if (userID != other.userID) return false

        return true
    }

    override fun hashCode(): Int {
        var result = channelID.hashCode()
        result = 31 * result + userID.hashCode()
        return result
    }
}
