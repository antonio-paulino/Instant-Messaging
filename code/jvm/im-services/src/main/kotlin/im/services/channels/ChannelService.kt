package im.services.channels

import jakarta.inject.Named

@Named
class ChannelService {

    fun createChannel() {
        TODO()
    }

    fun getChannelById(channelId: Long) {
        TODO()
    }

    fun updateChannel(channelId: Long, isPublic: Boolean, name: String) {
        TODO()
    }

    fun deleteChannel(channelId: Long) {
        TODO()
    }

    fun getChannelMembers(channelId: Long) {
        TODO()
    }

    fun addChannelMember(channelId: Long, userId: Long) {
        TODO()
    }

    fun removeChannelMember(channelId: Long, userId: Long) {
        TODO()
    }

}