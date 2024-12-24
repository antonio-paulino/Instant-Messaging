/**
 * Output model for message creation
 *
 * @param id The id of the message.
 * @param createdAt The date and time when the message was created.
 * @param editedAt The date and time when the message was last edited.
 *
 */
export interface MessageCreationOutputModel {
    id: number;
    createdAt: Date;
    editedAt: Date | null;
}
