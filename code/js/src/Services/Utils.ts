import { PaginationRequest } from '../Domain/pagination/PaginationRequest';
import { SortRequest } from '../Domain/pagination/SortRequest';

export function buildQuery(
    uri: string,
    name: string | null,
    paginationRequest: PaginationRequest | null,
    sortRequest: SortRequest | null,
    filterOwned: boolean | null = null,
): string {
    const query = new URLSearchParams();
    if (name) {
        query.set('name', name);
    }
    if (paginationRequest) {
        query.set('offset', paginationRequest.offset.toString());
        query.set('limit', paginationRequest.limit.toString());
        query.set('getCount', paginationRequest.getCount.toString());
    }
    if (sortRequest) {
        query.set('sort', sortRequest.direction);
        sortRequest.sortBy ? query.set('sortBy', sortRequest.sortBy) : null;
    }
    if (filterOwned !== null) {
        query.set('filterOwned', filterOwned.toString());
    }
    return `${uri}?${query.toString()}`;
}

/**
 * Delays execution of the function by the specified number of milliseconds.
 *
 * @param ms
 */
export async function delay(ms: number) {
    return new Promise((resolve) => setTimeout(resolve, ms));
}
