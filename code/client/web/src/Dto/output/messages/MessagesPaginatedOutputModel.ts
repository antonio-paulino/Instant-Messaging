import { MessageOutputModel } from './MessageOutputModel';
import { PaginationOutputModel } from '../PaginationOutputModel';

/**
 * Output model for messages paginated
 *
 * @param messages The messages.
 * @param pagination The pagination information.
 */
export interface MessagesPaginatedOutputModel {
    messages: MessageOutputModel[];
    pagination: PaginationOutputModel;
}
