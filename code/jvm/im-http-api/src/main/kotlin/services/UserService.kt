package pt.isel.daw.daw_api.services

import channel.Channel
import org.springframework.stereotype.Service
import pt.isel.daw.daw_api.model.input.UserInput
import pt.isel.daw.daw_api.model.output.UserOutput
import repositories.UserRepositoryImpl
import user.User


@Service
class UserService{

    fun getAllUsers(page:Int, size: Int): List<UserOutput> {
        TODO()
    }

    fun getUserById(id: Long): User? {
        TODO()
    }

    fun updateUser(id: Long, userInput: UserInput): UserOutput {
        TODO()
    }

    fun getUserByPartialName(partialName: String): List<User> {
        TODO()
    }

    fun deleteUser(id: Long): UserOutput {
        TODO()
    }

    fun getUserChannels(userId: Long): List<Channel> {
        TODO()
    }



}

