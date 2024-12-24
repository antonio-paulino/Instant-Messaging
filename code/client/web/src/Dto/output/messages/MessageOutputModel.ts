import { UserOutputModel } from '../users/UserOutputModel';

/**
 * Output model for message
 *
 * @param id The id of the message.
 * @param channelId The id of the channel the message belongs to.
 * @param author The author of the message.
 * @param content The content of the message.
 * @param createdAt The date and time when the message was created.
 * @param editedAt The date and time when the message was last edited.
 */
export interface MessageOutputModel {
    id: number;
    channelId: number;
    author: UserOutputModel;
    content: string;
    createdAt: Date;
    editedAt: Date | null;
}
