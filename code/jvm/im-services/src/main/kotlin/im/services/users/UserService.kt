package im.services.users

import im.channel.Channel
import im.user.User
import jakarta.inject.Named

@Named
class UserService {

    fun getUserById(id: Long): User {
        TODO()
    }

    fun getUsers(name: String, page: Int, size: Int): List<User> {
        TODO()
    }

    fun getUserChannels(userId: Long): List<Channel> {
        TODO()
    }

}

