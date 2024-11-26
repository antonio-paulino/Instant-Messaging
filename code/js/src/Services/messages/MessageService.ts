import { Channel } from '../../Domain/channel/Channel';
import { PaginationRequest } from '../../Domain/pagination/PaginationRequest';
import { SortRequest } from '../../Domain/pagination/SortRequest';
import { ApiResult, handle } from '../media/Problem';
import { MessagesPaginatedOutputModel } from '../../Dto/output/messages/MessagesPaginatedOutputModel';
import { buildQuery } from '../Utils';
import { BaseHTTPService } from '../BaseHTTPService';
import { Uri } from '../Uri';
import { Pagination } from '../../Domain/pagination/Pagination';
import { Message } from '../../Domain/messages/Message';
import { Identifier } from '../../Domain/wrappers/identifier/Identifier';
import { MessageOutputModel } from '../../Dto/output/messages/MessageOutputModel';
import { MessageCreationInputModel } from '../../Dto/input/MessageCreationInputModel';
import { MessageCreationOutputModel } from '../../Dto/output/messages/MessageCreationOutputModel';
import { Session } from '../../Domain/sessions/Session';

export namespace MessageService {
    import get = BaseHTTPService.get;
    import CHANNEL_ID_PARAM = Uri.CHANNEL_ID_PARAM;
    import MESSAGES_ROUTE = Uri.MESSAGES_ROUTE;
    import MESSAGE_ROUTE = Uri.MESSAGE_ROUTE;
    import MESSAGE_ID_PARAM = Uri.MESSAGE_ID_PARAM;
    import deleteRequest = BaseHTTPService.deleteRequest;
    import post = BaseHTTPService.post;
    import put = BaseHTTPService.put;

    /**
     * Gets the messages of a channel.
     *
     * @param channel - The channel to get the messages from.
     * @param pagination - The pagination request.
     * @param sort - The sort request.
     * @param before - The date to get messages before.
     * @param abortSignal - The signal to abort the request.
     *
     * @returns The messages of the channel.
     */
    export async function getChannelMessages(
        channel: Channel,
        pagination?: PaginationRequest,
        sort?: SortRequest,
        before?: Date,
        abortSignal?: AbortSignal,
    ): ApiResult<Pagination<Message>> {
        return await handle(
            get<MessagesPaginatedOutputModel>({
                uri: buildQuery(
                    MESSAGES_ROUTE,
                    null,
                    pagination,
                    sort,
                    false,
                    null,
                    before,
                ).replace(CHANNEL_ID_PARAM, channel.id.value.toString()),
                abortSignal: abortSignal,
            }),
            (outputModel) => ({
                items: outputModel.messages.map(Message.fromDto),
                info: outputModel.pagination,
            }),
        );
    }

    /**
     * Gets a message by its identifier.
     *
     * @param channel - The channel the message belongs to.
     * @param messageId - The identifier of the message.
     * @param abortSignal - The signal to abort the request.
     *
     * @returns The message.
     */
    export async function getMessage(
        channel: Channel,
        messageId: Identifier,
        abortSignal?: AbortSignal,
    ): ApiResult<Message> {
        return await handle(
            get<MessageOutputModel>({
                uri: MESSAGE_ROUTE.replace(
                    CHANNEL_ID_PARAM,
                    channel.id.value.toString(),
                ).replace(MESSAGE_ID_PARAM, messageId.value.toString()),
                abortSignal: abortSignal,
            }),
            (outputModel) => Message.fromDto(outputModel),
        );
    }

    /**
     * Creates a message.
     *
     * @param channel - The channel to create the message in.
     * @param content - The content of the message.
     * @param session - The session of the user creating the message.
     * @param abortSignal - The signal to abort the request.
     */
    export async function createMessage(
        channel: Channel,
        content: string,
        session: Session,
        abortSignal?: AbortSignal,
    ): ApiResult<Message> {
        return await handle(
            post<MessageCreationInputModel, MessageCreationOutputModel>({
                uri: MESSAGES_ROUTE.replace(
                    CHANNEL_ID_PARAM,
                    channel.id.value.toString(),
                ),
                requestBody: {
                    content: content,
                },
                abortSignal: abortSignal,
            }),
            (outputModel) =>
                Message.fromCreation(
                    outputModel,
                    channel.id,
                    session.user,
                    content,
                ),
        );
    }

    /**
     * Updates a message.
     *
     * @param message - The message to update.
     * @param content - The new content of the message.
     * @param abortSignal - The signal to abort the request.
     */
    export async function updateMessage(
        message: Message,
        content: string,
        abortSignal?: AbortSignal,
    ): ApiResult<void> {
        return await put<MessageCreationInputModel>({
            uri: MESSAGE_ROUTE.replace(
                CHANNEL_ID_PARAM,
                message.channelId.value.toString(),
            ).replace(MESSAGE_ID_PARAM, message.id.value.toString()),
            requestBody: {
                content: content,
            },
            fetchResBody: false,
            abortSignal: abortSignal,
        });
    }

    /**
     * Deletes a message.
     *
     * @param channel - The channel the message belongs to.
     * @param messageId - The identifier of the message.
     */
    export async function deleteMessage(
        channel: Channel,
        messageId: Identifier,
    ): ApiResult<void> {
        return await deleteRequest({
            uri: MESSAGE_ROUTE.replace(
                CHANNEL_ID_PARAM,
                channel.id.value.toString(),
            ).replace(MESSAGE_ID_PARAM, messageId.value.toString()),
        });
    }
}
