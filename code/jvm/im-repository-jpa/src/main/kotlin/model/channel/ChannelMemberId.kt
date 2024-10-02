package model.channel

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.io.Serializable

@Embeddable
data class ChannelMemberId (
    @Column(name = "channel_id")
    val channelID: Long = 0,
    @Column(name = "user_id")
    val userID: Long = 0
) : Serializable