import { MessageValidator } from './MessageValidator';
import { Identifier } from '../wrappers/identifier/Identifier';
import { User, userFromDto } from '../user/User';
import { MessageOutputModel } from '../../Dto/output/messages/MessageOutputModel';
import { MessageCreationOutputModel } from '../../Dto/output/messages/MessageCreationOutputModel';

/**
 * Represents a message in the system.
 *
 * @param id The unique identifier of the message.
 * @param channelId The unique identifier of the channel where the message was posted.
 * @param user The user who posted the message.
 * @param content The content of the message.
 * @param createdAt The date and time when the message was created.
 * @param editedAt The date and time when the message was edited.
 */
export class Message {
    private static validator = new MessageValidator();

    constructor(
        public readonly id: Identifier,
        public readonly channelId: Identifier,
        public readonly author: User,
        public readonly content: string,
        public readonly createdAt: Date,
        public readonly editedAt: Date | null = null,
    ) {
        const validation = Message.validator.validate(content);
        if (validation.length > 0) {
            throw new Error(validation.join('\n'));
        }
    }

    static fromDto(dto: MessageOutputModel): Message {
        return new Message(
            new Identifier(dto.id),
            new Identifier(dto.channelId),
            userFromDto(dto.author),
            dto.content,
            new Date(dto.createdAt),
            dto.editedAt ? new Date(dto.editedAt) : null,
        );
    }

    static fromCreation(
        dto: MessageCreationOutputModel,
        channelId: Identifier,
        author: User,
        content: string,
    ): Message {
        return new Message(new Identifier(dto.id), channelId, author, content, new Date(dto.createdAt));
    }
}
