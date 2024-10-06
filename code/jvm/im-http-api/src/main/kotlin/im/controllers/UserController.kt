package im.controllers

import im.channel.Channel
import im.user.User
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import im.model.input.query.PaginationInputModel
import im.services.users.UserService

@RestController
@RequestMapping("/users")
class UserController(private val userService: UserService) {

    @GetMapping
    @RequestMapping("/{userId}")
    fun getUserById(@PathVariable userId: String): User { // TODO: Change to ResponseEntity<Any> with UserOutputModel
        val result = userService.getUserById(userId.toLong())
        return result
    }

    @GetMapping
    fun getUsers(
        @RequestParam name: String,
        @Valid paginationInput: PaginationInputModel
    ): List<User> { // TODO: Change to ResponseEntity<Any> with UsersOutputModel
        val result = userService.getUsers(name, paginationInput.page.toInt(), paginationInput.size.toInt())
        return result
    }

    @GetMapping("/{userId}/channels")
    fun getUserChannels(
        @PathVariable userId: String
    ): List<Channel> { // TODO: Change to ResponseEntity<Any> with ChannelsOutputModel
        val result = userService.getUserChannels(userId.toLong())
        return result
    }

}