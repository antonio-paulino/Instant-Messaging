package user

import channel.Channel

interface User {
    val id: Long
    val name: String
    val password: String
    val ownedChannels : List<Channel>
    val joinedChannels : List<Channel>
}