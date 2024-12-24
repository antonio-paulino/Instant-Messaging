import { useSessionManager } from '../../UI/Providers/SessionProvider';
import { PaginationRequest } from '../../Domain/pagination/PaginationRequest';
import { User } from '../../Domain/user/User';
import { ApiResult } from '../../Services/media/Problem';
import { Pagination } from '../../Domain/pagination/Pagination';
import { UserService } from '../../Services/users/UserService';

export function useFetchUsersByName(searchValue: string) {
    const sessionManager = useSessionManager();

    return async (pageRequest: PaginationRequest, items: User[], signal: AbortSignal): ApiResult<Pagination<User>> => {
        return await sessionManager.executeWithRefresh(async () => {
            return await UserService.getUsers(searchValue, pageRequest, null, signal);
        });
    };
}
