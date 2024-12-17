import { InfiniteScrollState, useInfiniteScroll } from '../../State/useInfiniteScroll';
import { Channel } from '../../../Domain/channel/Channel';
import React, { useEffect, useMemo } from 'react';
import { useEventManager } from '../../Providers/EventsProvider';
import { Identifier } from '../../../Domain/wrappers/identifier/Identifier';
import { Window } from '../../Components/Utils/Layouts/Window';
import { useNavigate } from 'react-router-dom';
import Stack from '@mui/material/Stack';
import TextField from '@mui/material/TextField';
import { useSearchField } from '../../State/useSearchField';
import { LoadingSpinner } from '../../Components/Utils/State/LoadingSpinner';
import Typography from '@mui/material/Typography';
import { ListItem } from '@mui/material';
import { useAlertContext } from '../../Providers/AlertsProvider';
import { RetryButton } from '../../Components/Utils/Actions/RetryButton';
import { Routes } from '../../../routes';
import { ChannelSearchResult } from '../../Components/Channels/ChannelSearchResult';
import { VirtualizedList } from '../../Components/Utils/VirtualizedList';
import Box from '@mui/material/Box';
import { useFetchChannelsByName } from '../../../Utils/Hooks/fetchChannelsByName';
import { useSessionManager } from '../../Providers/SessionProvider';

export function ChannelSearchWindow() {
    const navigate = useNavigate();
    const { items, state, loadMore, setValue: setSearchValue } = useChannelSearchWindow();
    return (
        <Window onClose={() => navigate(Routes.HOME)} width={'50%'}>
            <Stack direction={'column'} spacing={2} padding={2}>
                <TextField label={'Search'} name={'searchValue'} onChange={(e) => setSearchValue(e.target.value)} />
                <Box sx={{ height: '56vh' }}>
                    {state.type === 'error' && items.length === 0 && <RetryButton onClick={loadMore} />}
                    {items.length === 0 && state.type === 'loaded' && !state.paginationState.info.next && (
                        <Typography variant={'subtitle1'} sx={{ textAlign: 'center', mt: 4 }}>
                            No channels found
                        </Typography>
                    )}
                    <VirtualizedList
                        items={items}
                        fixedHeight={100}
                        itemStyle={{ marginBottom: '1em', width: '100%' }}
                        listStyle={{ width: '100%', height: '100%' }}
                        renderItem={({ index, style }) => {
                            const channel = items[index];
                            return (
                                <ListItem
                                    key={index}
                                    style={{
                                        ...style,
                                        justifyContent: 'center',
                                        alignItems: 'center',
                                    }}
                                >
                                    <ChannelSearchResult channel={channel} />
                                </ListItem>
                            );
                        }}
                        onItemsRendered={(_, end) => {
                            if (end === items.length - 1) {
                                loadMore();
                            }
                        }}
                        footer={state.type === 'loading' && <LoadingSpinner />}
                    />
                </Box>
            </Stack>
        </Window>
    );
}

interface ChannelSearchWindowHook {
    state: InfiniteScrollState<Channel>;
    items: Channel[];
    loadMore: () => void;
    setValue: (value: string) => void;
}

const limit = 25;

function useChannelSearchWindow(): ChannelSearchWindowHook {
    const eventManager = useEventManager();
    const { showAlert } = useAlertContext();
    const user = useSessionManager().session.user;

    const { searchValue, setValue } = useSearchField(250);

    const fetchChannelsByName = useFetchChannelsByName(searchValue);

    const scrollState = useInfiniteScroll<Channel>({
        fetchItemsRequest: fetchChannelsByName,
        limit: limit,
        getCount: false,
        useOffset: false,
    });

    useEffect(() => {
        scrollState.reset();
    }, [searchValue]);

    useEffect(() => {
        if (scrollState.state.type === 'error') {
            showAlert({
                message: 'Failed to load Channels',
                severity: 'error',
            });
        }
    }, [scrollState.state.type]);

    const listeners = React.useMemo(
        () => [
            {
                type: 'channel-updated',
                listener: (event: MessageEvent<string>) => {
                    const channel = eventManager.handleEvent(event).data as Channel;
                    scrollState.handleItemUpdate(channel);
                },
            },
            {
                type: 'channel-deleted',
                listener: (event: MessageEvent<string>) => {
                    const channelId = eventManager.handleEvent(event).data as Identifier;
                    scrollState.handleItemDelete(channelId);
                },
            },
        ],
        [],
    );

    useEffect(() => {
        if (eventManager.isOpen) {
            listeners.forEach(eventManager.addListener);
        }
        return () => {
            listeners.forEach(eventManager.removeListener);
        };
    }, [eventManager.isOpen]);

    const filteredItems = useMemo(() => {
        return scrollState.state.paginationState.items.filter((channel) => {
            return !channel.members.find((member) => member.id.value === user.id.value);
        });
    }, [scrollState.state.paginationState.items]);

    useEffect(() => {
        if (
            filteredItems.length < limit &&
            scrollState.state.type === 'loaded' &&
            scrollState.state.paginationState.info.next
        ) {
            scrollState.loadMore();
        }
    }, [filteredItems, scrollState]);

    return {
        state: scrollState.state,
        items: filteredItems,
        loadMore: scrollState.loadMore,
        setValue,
    };
}
