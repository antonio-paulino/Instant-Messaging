import { EventListener, useEventManager } from '../../Providers/EventsProvider';
import { useAlertContext } from '../../Providers/AlertsProvider';
import * as React from 'react';
import { useEffect, useMemo } from 'react';
import { Identifier } from '../../../Domain/wrappers/identifier/Identifier';
import DropDownMenu from '../Utils/Menu/DropDownMenu';
import Typography from '@mui/material/Typography';
import { Button, ListItem, ListItemText, Stack } from '@mui/material';
import { Add } from '@mui/icons-material';
import { LoadingSpinner } from '../Utils/State/LoadingSpinner';
import { Channel } from '../../../Domain/channel/Channel';
import { useInfiniteScrollContextChannels } from '../../Providers/InfiniteScrollProvider';
import { Link } from 'react-router-dom';
import { ChannelSideView } from './ChannelSideView';
import { RetryButton } from '../Utils/Actions/RetryButton';
import { Routes } from '../../../routes';
import { InfiniteScrollState } from '../../State/useInfiniteScroll';
import { VirtualizedList } from '../Utils/VirtualizedList';
import Box from '@mui/material/Box';
import { useSessionManager } from '../../Providers/SessionProvider';

export function ChannelsDropDown() {
    const { state, loadMore, selectedChannel, setSelectedChannel } = useChannelsDropDown();
    const channels = state.paginationState.items;
    return (
        <DropDownMenu
            bottomScroll={true}
            startOpen={true}
            header={
                <Stack direction={'row'} width={'100%'} alignItems={'center'} gap={1}>
                    <Typography variant={'body1'}>Channels</Typography>
                </Stack>
            }
        >
            <Link to={Routes.CREATE_CHANNEL}>
                <Button
                    fullWidth
                    variant={'text'}
                    sx={{
                        justifyContent: 'center',
                        p: 1,
                        mb: 1,
                        width: '100%',
                    }}
                >
                    <Add />
                    <Typography variant={'body1'} style={{ marginRight: '0.5em' }}>
                        Create Channel
                    </Typography>
                </Button>
            </Link>
            <VirtualizedList
                listStyle={{ height: '30vh' }}
                itemStyle={{
                    width: '100%',
                    alignSelf: 'center',
                    justifySelf: 'center',
                }}
                items={channels}
                fixedHeight={30}
                header={
                    <Box
                        sx={{
                            width: '100%',
                            display: 'flex',
                            justifyContent: 'center',
                        }}
                    >
                        {state.type === 'error' && channels.length === 0 && <RetryButton onClick={loadMore} />}
                        {channels.length === 0 && state.type === 'loaded' && (
                            <ListItem sx={{ mt: 2 }}>
                                <ListItemText
                                    sx={{ textAlign: 'center' }}
                                    primary={'No channels'}
                                    secondary={'Create a new channel to start chatting'}
                                />
                            </ListItem>
                        )}
                    </Box>
                }
                renderItem={({ index, style }) => {
                    const channel = channels[index];
                    return (
                        <ListItem
                            key={channel.id.value}
                            sx={{
                                p: 1,
                                color: 'primary.main',
                                justifyContent: 'center',
                                height: '30px',
                                width: '100%',
                            }}
                        >
                            <ChannelSideView
                                channel={channel}
                                selected={selectedChannel?.value === channel.id.value}
                                setSelected={setSelectedChannel}
                            />
                        </ListItem>
                    );
                }}
                footer={state.type === 'loading' && <LoadingSpinner />}
                onItemsRendered={(start, end) => {
                    if (end === channels.length - 1) {
                        loadMore();
                    }
                }}
            />
        </DropDownMenu>
    );
}

interface ChannelsDropDownHook {
    state: InfiniteScrollState<Channel>;
    loadMore: () => void;
    selectedChannel: Identifier | undefined;
    setSelectedChannel: (channel: Identifier) => void;
}

function useChannelsDropDown(): ChannelsDropDownHook {
    const { state, loadMore, handleItemUpdate, handleItemDelete, selectedChannel, setSelectedChannel } =
        useInfiniteScrollContextChannels();
    const eventManager = useEventManager();
    const { showAlert } = useAlertContext();
    const user = useSessionManager().session?.user;

    useEffect(() => {
        if (state.paginationState.items.length > 0 && !selectedChannel) {
            setSelectedChannel(state.paginationState.items[0].id);
        }
    }, [state.paginationState.items.length > 0]);

    useEffect(() => {
        if (state.type === 'error') {
            showAlert({
                message: 'Failed to load Channels',
                severity: 'error',
            });
        }
    }, [state.type]);

    const eventListeners: EventListener[] = useMemo(
        () => [
            {
                type: 'channel-updated',
                listener: (event: MessageEvent<string>) => {
                    const channel = eventManager.handleEvent(event).data as Channel;
                    handleItemUpdate(channel);
                    if (!channel.members.find((member) => member.id.value === user.id.value)) {
                        handleItemDelete(channel.id);
                        showAlert({
                            message: `You were removed from ${channel.name.value}`,
                            severity: 'info',
                        });
                        if (selectedChannel?.value === channel.id.value) {
                            setSelectedChannel(null);
                        }
                    }
                },
            },
            {
                type: 'channel-deleted',
                listener: (event: MessageEvent<string>) => {
                    const channelId = eventManager.handleEvent(event).data as Identifier;
                    if (selectedChannel?.value === channelId.value) {
                        setSelectedChannel(null);
                        showAlert({
                            message: 'The channel you were in was deleted',
                            severity: 'info',
                        });
                    }
                    handleItemDelete(channelId);
                },
            },
        ],
        [selectedChannel, user],
    );

    useEffect(() => {
        if (eventManager.isInitialized) {
            eventListeners.forEach(eventManager.addListener);
        }
        return () => {
            eventListeners.forEach(eventManager.removeListener);
        };
    }, [eventManager.isInitialized, eventListeners]);

    return { state, loadMore, selectedChannel, setSelectedChannel };
}
