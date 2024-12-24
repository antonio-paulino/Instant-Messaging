import { ChannelOutputModel } from './ChannelOutputModel';
import { PaginationOutputModel } from '../PaginationOutputModel';

/**
 * Channels paginated output model
 *
 * @param channels The channels.
 * @param pagination The pagination information.
 */
export interface ChannelsPaginatedOutputModel {
    channels: ChannelOutputModel[];
    pagination: PaginationOutputModel;
}
