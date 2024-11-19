/**
 * Output model for pagination information.
 *
 * @param total The total number of items.
 * @param totalPages The total number of pages.
 * @param current The current page number.
 * @param next The next page number.
 * @param previous The previous page number.
 */
export interface PaginationOutputModel {
    total: number | null;
    totalPages: number | null;
    current: number;
    next: number | null;
    previous: number | null;
}
