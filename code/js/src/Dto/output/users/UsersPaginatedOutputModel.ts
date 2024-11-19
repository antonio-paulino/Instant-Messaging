import { PaginationOutputModel } from '../PaginationOutputModel';
import { UserOutputModel } from './UserOutputModel';

/**
 * Output model for paginated users
 *
 * @param users The users in the current page.
 * @param pagination The pagination information.
 */
export interface UsersPaginatedOutputModel {
    users: UserOutputModel[];
    pagination: PaginationOutputModel;
}
