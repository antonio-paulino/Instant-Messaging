package pt.isel.daw.daw_api.controllers

import org.springframework.web.bind.annotation.*
import pt.isel.daw.daw_api.model.input.MessageInput
import pt.isel.daw.daw_api.model.input.PaginationInput
import pt.isel.daw.daw_api.services.MessageService

@RestController
class MessagesController(private val messageService: MessageService) {

    @GetMapping("/channels/{channelId}/messages")
    fun getChannelMessages(
        @RequestBody paginationInput: PaginationInput,
        @PathVariable channelId: String,

        ) {

        messageService.getChannelMessages(channelId.toLong(), paginationInput.page, paginationInput.size)
    }

    @PostMapping("/channels/{channelId}/messages")
    fun createMessage(
        @PathVariable channelId: String,
        @RequestBody messageInput: MessageInput
    ) {
        messageService.createMessage(channelId.toLong(), messageInput.content)
    }

    @PutMapping("/channels/{channelId}/messages/{messageId}")
    fun updateMessage(
        @PathVariable channelId: String,
        @PathVariable messageId: String,
        @RequestBody messageInput: MessageInput
    ) {
        messageService.updateMessage(channelId.toLong(), messageId.toLong(), messageInput.content)
    }

    @DeleteMapping("/channels/{channelId}/messages/{messageId}")
    fun deleteMessage(
        @PathVariable channelId: String,
        @PathVariable messageId: String,
    ) {
        messageService.deleteMessage(channelId.toLong(), messageId.toLong())
    }
}