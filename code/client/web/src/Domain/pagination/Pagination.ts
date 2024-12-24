import { PaginationInfo } from './PaginationInfo';

/**
 * Represents the pagination information for a list of items.
 *
 * @param items The items.
 * @param info The pagination information.
 */
export interface Pagination<T> {
    readonly items: T[];
    readonly info: PaginationInfo;
}
