package pt.isel.daw.daw_api.controllers

import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pt.isel.daw.daw_api.model.input.ChannelInput
import pt.isel.daw.daw_api.model.input.MessageInput
import pt.isel.daw.daw_api.model.input.PaginationInput
import pt.isel.daw.daw_api.services.ChannelService

@RestController
@RequestMapping("/channels")
class ChannelsController(private val channelService: ChannelService) {

    @PostMapping
    fun createChannel(
        @RequestBody channelInput: ChannelInput
    ) {
        channelService.createChannel()
    }

    @GetMapping("/{channelId}")
    fun getChannelById(
        @PathVariable channelId: String
    ) {
        channelService.getChannelById(channelId.toLong())
    }

    @PatchMapping("/{channelId}")
    fun updateChannel(
        @PathVariable channelId: String,
        @RequestBody channelInput: ChannelInput
    ) {
        channelService.updateChannel(channelId.toLong(), channelInput)
    }

    @DeleteMapping("/{channelId}")
    fun deleteChannel(
        @PathVariable channelId: String
    ) {
        channelService.deleteChannel(channelId.toLong())
    }

    @GetMapping("/{channelId}/members")
    fun getChannelUsers(
        @PathVariable channelId: String
    ) {
        channelService.getChannelMembers(channelId.toLong())
    }

    @PostMapping("/{channelId}/members/{userId}")
    fun addChannelUser(
        @PathVariable channelId: String,
        @PathVariable userId: String
    ) {
        channelService.addChannelMember(channelId.toLong(), userId.toLong())
    }

    @DeleteMapping("/{channelId}/members/{userId}")
    fun removeChannelUser(
        @PathVariable channelId: String,
        @PathVariable userId: String
    ) {
        channelService.removeChannelMember(channelId.toLong(), userId.toLong())
    }

}