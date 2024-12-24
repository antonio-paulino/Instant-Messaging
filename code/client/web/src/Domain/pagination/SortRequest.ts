import { Sort } from './Sort';

/**
 * Sort direction
 *
 * @param sortBy The field to sort by.
 * @enum Sort The sort direction.
 */
export interface SortRequest {
    sortBy: string | null;
    direction: Sort;
}
