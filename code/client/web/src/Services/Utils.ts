import { PaginationRequest } from '../Domain/pagination/PaginationRequest';
import { SortRequest } from '../Domain/pagination/SortRequest';
import { Identifier } from '../Domain/wrappers/identifier/Identifier';

export function buildQuery(
    uri: string,
    name: string | null,
    paginationRequest: PaginationRequest | null,
    sortRequest: SortRequest | null,
    filterOwned: boolean | null = null,
    after: Identifier | null = null,
    before: Date | null = null,
): string {
    const query = new URLSearchParams();
    if (name) {
        query.set('name', name);
    }
    if (paginationRequest) {
        paginationRequest.offset ? query.set('offset', paginationRequest.offset.toString()) : null;
        paginationRequest.limit ? query.set('limit', paginationRequest.limit.toString()) : null;
        query.set('getCount', paginationRequest.getCount ? 'true' : 'false');
    }
    if (sortRequest) {
        query.set('sort', sortRequest.direction);
        sortRequest.sortBy ? query.set('sortBy', sortRequest.sortBy) : null;
    }
    if (filterOwned !== null) {
        query.set('filterOwned', filterOwned.toString());
    }
    if (after) {
        query.set('after', after.value.toString());
    }
    if (before) {
        query.set('before', before.toISOString());
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
