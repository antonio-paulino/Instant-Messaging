/**
 * Interface for pagination information
 *
 * @param total The total number of items.
 * @param currentPage The current page number.
 * @param totalPages The total number of pages.
 * @param nextPage The next page number.
 * @param prevPage The previous page number.
 *
 */
export interface PaginationInfo {
    total: number | null;
    totalPages: number | null;
    current: number;
    next: number | null;
    previous: number | null;
}
