import { PaginationRequest } from '../../Domain/pagination/PaginationRequest';
import { Channel } from '../../Domain/channel/Channel';
import { ApiResult } from '../../Services/media/Problem';
import { Pagination } from '../../Domain/pagination/Pagination';
import { ChannelService } from '../../Services/channels/ChannelService';
import { useSessionManager } from '../../UI/Providers/SessionProvider';

export function useFetchChannelsByName(searchValue: string) {
    const sessionManager = useSessionManager();
    return async (
        pageRequest: PaginationRequest,
        items: Channel[],
        signal: AbortSignal,
    ): ApiResult<Pagination<Channel>> => {
        return await sessionManager.executeWithRefresh(async () => {
            const after = items.length > 0 ? items[items.length - 1].id : null;
            return await ChannelService.getChannels(searchValue, pageRequest, null, after, signal);
        });
    };
}
