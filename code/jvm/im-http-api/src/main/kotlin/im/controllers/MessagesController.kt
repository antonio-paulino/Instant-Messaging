package pt.isel.daw.daw_api.im.controllers

import org.springframework.web.bind.annotation.*
import im.model.input.body.MessageInputModel
import im.model.input.query.PaginationInputModel
import im.services.messages.MessageService
import jakarta.validation.Valid

@RestController
class MessagesController(private val messageService: MessageService) {

    @GetMapping("/channels/{channelId}/messages")
    fun getChannelMessages(
        @Valid paginationInput: PaginationInputModel,
        @PathVariable channelId: String,
    ) {

        messageService.getChannelMessages(
            channelId.toLong(),
            paginationInput.page.toInt(),
            paginationInput.size.toInt()
        )
    }

    @PostMapping("/channels/{channelId}/messages")
    fun createMessage(
        @PathVariable channelId: String,
        @RequestBody messageInput: MessageInputModel
    ) {
        messageService.createMessage(channelId.toLong(), messageInput.content)
    }

    @PutMapping("/channels/{channelId}/messages/{messageId}")
    fun updateMessage(
        @PathVariable channelId: String,
        @PathVariable messageId: String,
        @RequestBody messageInput: MessageInputModel
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