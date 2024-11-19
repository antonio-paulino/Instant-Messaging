import { Name } from '../../Domain/wrappers/name/Name';
import { ChannelRole } from '../../Domain/channel/ChannelRole';
import { Session } from '../../Domain/sessions/Session';
import { ApiResult, handle } from '../media/Problem';
import {
    Channel,
    channelFromCreation,
    channelFromDto,
} from '../../Domain/channel/Channel';
import { BaseHTTPService } from '../BaseHTTPService';
import { ChannelCreationInputModel } from '../../Dto/input/ChannelCreationInputModel';
import { ChannelCreationOutputModel } from '../../Dto/output/channels/ChannelCreationOutputModel';
import { Identifier } from '../../Domain/wrappers/identifier/Identifier';
import { ChannelOutputModel } from '../../Dto/output/channels/ChannelOutputModel';
import { PaginationRequest } from '../../Domain/pagination/PaginationRequest';
import { SortRequest } from '../../Domain/pagination/SortRequest';
import { Pagination } from '../../Domain/pagination/Pagination';
import { ChannelsPaginatedOutputModel } from '../../Dto/output/channels/ChannelsPaginatedOutputModel';
import { ChannelRoleUpdateInputModel } from '../../Dto/input/ChannelRoleUpdateInputModel';
import { User } from '../../Domain/user/User';
import { Uri } from '../Uri';
import { buildQuery } from '../Utils';

export namespace ChannelService {
    import post = BaseHTTPService.post;
    import get = BaseHTTPService.get;
    import deleteRequest = BaseHTTPService.deleteRequest;
    import patch = BaseHTTPService.patch;

    import CHANNELS_ROUTE = Uri.CHANNELS_ROUTE;
    import CHANNEL_ROUTE = Uri.CHANNEL_ROUTE;
    import CHANNEL_ID_PARAM = Uri.CHANNEL_ID_PARAM;
    import CHANNEL_MEMBERS_ROUTE = Uri.CHANNEL_MEMBERS_ROUTE;
    import CHANNEL_MEMBER_ROUTE = Uri.CHANNEL_MEMBER_ROUTE;
    import USER_ID_PARAM = Uri.USER_ID_PARAM;
    import put = BaseHTTPService.put;

    /**
     * Creates a channel.
     *
     * @param name the name of the channel
     * @param defaultRole the default role of a user in the channel
     * @param isPublic indicates whether the channel is public
     * @param session the session of the user creating the channel
     * @param abortSignal the signal to abort the request
     *
     * @returns the channel that was created
     */
    export async function createChannel(
        name: Name,
        defaultRole: ChannelRole,
        isPublic: boolean,
        session: Session,
        abortSignal?: AbortSignal,
    ): ApiResult<Channel> {
        return handle(
            post<ChannelCreationInputModel, ChannelCreationOutputModel>({
                uri: CHANNELS_ROUTE,
                requestBody: {
                    name: name.value,
                    defaultRole: defaultRole,
                    isPublic: isPublic,
                },
                abortSignal: abortSignal,
            }),
            (outputModel) =>
                channelFromCreation(
                    outputModel,
                    name,
                    defaultRole,
                    session.user,
                    isPublic,
                ),
        );
    }

    /**
     * Gets a channel by its identifier.
     *
     * @param channelId the identifier of the channel
     * @param abortSignal the signal to abort the request
     *
     * @returns the channel
     */
    export async function getChannel(
        channelId: Identifier,
        abortSignal?: AbortSignal,
    ): ApiResult<Channel> {
        return handle(
            get<ChannelOutputModel>({
                uri: CHANNEL_ROUTE.replace(
                    CHANNEL_ID_PARAM,
                    channelId.value.toString(),
                ),
                abortSignal: abortSignal,
            }),
            channelFromDto,
        );
    }

    /**
     * Gets a list of channels.
     *
     * @param name the name of the channel
     * @param pagination the pagination request
     * @param sort the sort request
     * @param abortSignal the signal to abort the request
     *
     * @returns the Channels with pagination information
     */
    export async function getChannels(
        name?: string,
        pagination?: PaginationRequest,
        sort?: SortRequest,
        abortSignal?: AbortSignal,
    ): ApiResult<Pagination<Channel>> {
        return handle(
            get<ChannelsPaginatedOutputModel>({
                uri: buildQuery(CHANNELS_ROUTE, name, pagination, sort),
                abortSignal: abortSignal,
            }),
            (outputModel) => ({
                items: outputModel.channels.map(channelFromDto),
                info: outputModel.pagination,
            }),
        );
    }

    /**
     * Updates a channel.
     *
     * @param channelId - the identifier of the channel
     * @param name - the name of the channel
     * @param defaultRole - the default role of a user in the channel
     * @param isPublic - indicates whether the channel is public
     * @param abortSignal - the signal to abort the request
     *
     * @returns the channel that was updated
     */
    export async function updateChannel(
        channelId: Identifier,
        name: Name,
        defaultRole: ChannelRole,
        isPublic: boolean,
        abortSignal?: AbortSignal,
    ): ApiResult<void> {
        return await put<ChannelCreationInputModel>({
            uri: CHANNEL_ROUTE.replace(
                CHANNEL_ID_PARAM,
                channelId.value.toString(),
            ),
            requestBody: {
                name: name.value,
                defaultRole: defaultRole,
                isPublic: isPublic,
            },
            fetchResBody: false,
            abortSignal: abortSignal,
        });
    }

    /**
     * Deletes a channel.
     *
     * @param channelId the identifier of the channel
     * @param abortSignal the signal to abort the request
     *
     * @returns the result of the request
     */
    export async function deleteChannel(
        channelId: Identifier,
        abortSignal?: AbortSignal,
    ): ApiResult<void> {
        return await deleteRequest({
            uri: CHANNEL_ROUTE.replace(
                CHANNEL_ID_PARAM,
                channelId.value.toString(),
            ),
            abortSignal: abortSignal,
        });
    }

    /**
     * Adds the current session user to a channel.
     *
     * @param channel - the channel
     * @param session - the session
     * @param abortSignal - the signal to abort the request
     *
     * @returns the result of the request
     */
    export async function joinChannel(
        channel: Channel,
        session: Session,
        abortSignal?: AbortSignal,
    ): ApiResult<void> {
        return await put({
            uri: CHANNEL_MEMBERS_ROUTE.replace(
                CHANNEL_ID_PARAM,
                channel.id.value.toString(),
            ).replace(USER_ID_PARAM, session.user.id.value.toString()),
            abortSignal: abortSignal,
        });
    }

    /**
     * Removes a user from a channel.
     *
     * @param channel - the channel
     * @param userId - the identifier of the user
     * @param abortSignal - the signal to abort the request
     *
     * @returns the result of the request
     */
    export async function removeUserFromChannel(
        channel: Channel,
        userId: Identifier,
        abortSignal?: AbortSignal,
    ): ApiResult<void> {
        return await deleteRequest({
            uri: CHANNEL_MEMBER_ROUTE.replace(
                CHANNEL_ID_PARAM,
                channel.id.value.toString(),
            ).replace(USER_ID_PARAM, userId.value.toString()),
            abortSignal: abortSignal,
        });
    }

    /**
     * Updates the role of a member in a channel.
     *
     * @param channel - the channel
     * @param user - the user
     * @param role - the role
     * @param abortSignal - the signal to abort the request
     *
     * @returns the result of the request
     */
    export async function updateMemberRole(
        channel: Channel,
        user: User,
        role: ChannelRole,
        abortSignal?: AbortSignal,
    ): ApiResult<void> {
        return await patch<ChannelRoleUpdateInputModel>({
            uri: CHANNEL_MEMBER_ROUTE.replace(
                CHANNEL_ID_PARAM,
                channel.id.value.toString(),
            ).replace(USER_ID_PARAM, user.id.value.toString()),
            requestBody: {
                role: role,
            },
            abortSignal: abortSignal,
        });
    }
}
