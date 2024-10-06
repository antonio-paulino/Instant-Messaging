package pt.isel.daw.daw_api.controllers

import channel.Channel
import org.springframework.web.bind.annotation.*
import pt.isel.daw.daw_api.model.input.PaginationInput
import pt.isel.daw.daw_api.model.input.UserInput
import pt.isel.daw.daw_api.model.output.UserOutput
import pt.isel.daw.daw_api.services.UserService
import user.User

@RestController
@RequestMapping("/users")
class UserController(private val userService: UserService) {

    @GetMapping
    fun getAllUsers(
        @RequestBody paginationInput: PaginationInput,
    ): List<UserOutput> {
        val result = userService.getAllUsers(paginationInput.page, paginationInput.size)
        return listOf()
    }

    @GetMapping
    @RequestMapping("/{userId}")
    fun getUserById(@PathVariable userId: String): User? {

        val result = userService.getUserById(userId.toLong())
        return result
    }

    @GetMapping
    @RequestMapping("/{partialName}")
    fun getUserByPartialName(@PathVariable partialName: String): List<User> {
        val result = userService.getUserByPartialName(partialName)
        return result
    }

    @PutMapping("/{userId}")
    fun updateUser(
        @PathVariable userId: String,
        @RequestBody userInput: UserInput
    ): UserOutput {
        val result = userService.updateUser(userId.toLong(), userInput)
        return result
    }

    @DeleteMapping("/{userId}")
    fun deleteUser(
        @PathVariable userId: String
    ): UserOutput {
        val result = userService.deleteUser(userId.toLong())
        return result
    }

    @GetMapping("/{userId}/channels")
    fun getUserChannels(
        @PathVariable userId: String
    ): List<Channel> {
        val result = userService.getUserChannels(userId.toLong())
        return result
    }
}