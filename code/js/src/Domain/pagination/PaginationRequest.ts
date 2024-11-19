/**
 * Represents a pagination request.
 * @param page The page number.
 * @param size The number of items per page.
 * @param getCount Indicates whether to return the total count of items.
 */
export interface PaginationRequest {
    offset: number;
    limit: number;
    getCount: boolean;
}
