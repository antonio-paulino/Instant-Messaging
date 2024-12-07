import { useInfiniteScrollContextChannels } from '../../UI/Providers/InfiniteScrollProvider';

export function useChannel() {
    const { state, selectedChannel, setSelectedChannel } = useInfiniteScrollContextChannels();
    return {
        channel: state.paginationState.items.find((channel) => channel.id.value === selectedChannel?.value),
        setSelectedChannel,
    };
}
