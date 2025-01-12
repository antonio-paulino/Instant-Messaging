import { Identifier } from '../../Domain/wrappers/identifier/Identifier';
import { ApiResult, handle } from '../media/Problem';
import { User, userFromDto } from '../../Domain/user/User';
import { UserOutputModel } from '../../Dto/output/users/UserOutputModel';
import { BaseHTTPService } from '../BaseHTTPService';
import { Uri } from '../Uri';
import { Pagination } from '../../Domain/pagination/Pagination';
import { SortRequest } from '../../Domain/pagination/SortRequest';
import { PaginationRequest } from '../../Domain/pagination/PaginationRequest';
import { UsersPaginatedOutputModel } from '../../Dto/output/users/UsersPaginatedOutputModel';
import { buildQuery } from '../Utils';
import { Channel, channelFromDto } from '../../Domain/channel/Channel';
import { ChannelsPaginatedOutputModel } from '../../Dto/output/channels/ChannelsPaginatedOutputModel';

export namespace UserService {
    import get = BaseHTTPService.get;
    import USER_ROUTE = Uri.USER_ROUTE;
    import USER_ID_PARAM = Uri.USER_ID_PARAM;
    import USERS_ROUTE = Uri.USERS_ROUTE;
    import USER_CHANNELS_ROUTE = Uri.USER_CHANNELS_ROUTE;

    /**
     * Gets a user by their identifier.
     *
     * @param userId The identifier of the user.
     * @param abortSignal The signal to abort the request.
     *
     * @returns The user that was retrieved.
     */
    export async function getUser(userId: Identifier, abortSignal?: AbortSignal): ApiResult<User> {
        return handle(
            get<UserOutputModel>({
                uri: USER_ROUTE.replace(USER_ID_PARAM, userId.value.toString()),
                abortSignal: abortSignal,
            }),
            userFromDto,
        );
    }

    /**
     * Gets a list of users.
     *
     * @param name The name of the user.
     * @param pagination The pagination request.
     * @param sort The sort request.
     * @param abortSignal The signal to abort the request.
     *
     * @returns The users with pagination information.
     */
    export async function getUsers(
        name?: string,
        pagination?: PaginationRequest,
        sort?: SortRequest,
        abortSignal?: AbortSignal,
    ): ApiResult<Pagination<User>> {
        return handle(
            get<UsersPaginatedOutputModel>({
                uri: buildQuery(USERS_ROUTE, name, pagination, sort),
                abortSignal: abortSignal,
            }),
            (outputModel) => ({
                items: outputModel.users.map(userFromDto),
                info: outputModel.pagination,
            }),
        );
    }

    /**
     * Gets the channels of a user.
     *
     * @param user The user to get the channels from.
     * @param filterOwned Whether to filter the channels to only those owned by the user.
     * @param sort The sort request.
     * @param pagination The pagination request.
     * @param after The identifier of the last item in the previous page.
     * @param abortSignal The signal to abort the request.
     */
    export async function getUserChannels(
        user: User,
        filterOwned: boolean = false,
        sort?: SortRequest,
        pagination?: PaginationRequest,
        after?: Identifier,
        abortSignal?: AbortSignal,
    ): ApiResult<Pagination<Channel>> {
        return await handle(
            get<ChannelsPaginatedOutputModel>({
                uri: buildQuery(USER_CHANNELS_ROUTE, null, pagination, sort, filterOwned, after).replace(
                    USER_ID_PARAM,
                    user.id.value.toString(),
                ),
                abortSignal: abortSignal,
            }),
            (outputModel) => ({
                items: outputModel.channels.map(channelFromDto),
                info: outputModel.pagination,
            }),
        );
    }
}
