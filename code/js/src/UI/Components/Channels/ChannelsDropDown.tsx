import { useSessionManager } from '../Providers/Session';
import { EventListener, useEventManager } from '../Providers/Events';
import { useAlert } from '../Providers/Alerts';
import * as React from 'react';
import { useEffect } from 'react';
import { Identifier } from '../../../Domain/wrappers/identifier/Identifier';
import DropDownMenu from '../DropDownMenu';
import Typography from '@mui/material/Typography';
import {
    Button,
    List,
    ListItem,
    ListItemText,
    Stack,
} from '@mui/material';
import {
    Add,
    ChatBubbleOutline,
    GavelOutlined,
    MenuBookOutlined,
    Replay,
} from '@mui/icons-material';
import { LoadingSpinner } from '../LoadingSpinner';
import { Channel } from '../../../Domain/channel/Channel';
import { ChannelRole } from '../../../Domain/channel/ChannelRole';
import { useInfiniteScrollContextChannels } from '../Providers/InfiniteScrollProvider';
import { Fade } from 'react-awesome-reveal';
import { Link } from 'react-router-dom';

export function ChannelsDropDown() {
    const eventManager = useEventManager();
    const { showAlert } = useAlert();
    const { selectedChannel, setSelectedChannel } = useInfiniteScrollContextChannels();
    const limit = 10;

    const { state, loadMore, handleItemUpdate, handleItemDelete } =
        useInfiniteScrollContextChannels();

    const channels = state.paginationState.items;

    useEffect(() => {
        const eventListeners: EventListener[] = [
            {
                type: 'channel-updated',
                listener: (event: MessageEvent<string>) => {
                    const channel = eventManager.handleEvent(event)
                        .data as Channel;
                    handleItemUpdate(channel);
                    if (selectedChannel?.id.value === channel.id.value) {
                        setSelectedChannel(channel);
                    }
                },
            },
            {
                type: 'channel-deleted',
                listener: (event: MessageEvent<string>) => {
                    const channelId = eventManager.handleEvent(event).data as Identifier;
                    handleItemDelete(channelId);
                    if (selectedChannel?.id.value === channelId.value) {
                        setSelectedChannel(null);
                        showAlert({
                            message: 'The channel was deleted',
                            severity: 'info',
                        });
                    }
                },
            },
        ];
        eventListeners.forEach(eventManager.addListener);
        return () => {
            eventListeners.forEach(eventManager.removeListener);
        };
    }, []);

    useEffect(() => {
        if (state.type === 'error') {
            showAlert({
                message: 'Failed to load Channels',
                severity: 'error',
            });
        }
        if (
            state.type === 'initial' ||
            (state.type === 'loaded' && channels.length < limit)
        ) {
            loadMore();
        }
    }, [state.type, state.paginationState.items.length]);

    return (
        <DropDownMenu
            maxHeight={'24vh'}
            maxWidth={'100%'}
            loadMore={loadMore}
            bottomScroll={true}
            startOpen={true}
            header={
                <Stack direction={'row'} width={'100%'} alignItems={'center'} gap={1}>
                    <Typography variant={'body1'}>Channels</Typography>
                </Stack>
            }
        >
            <Link to={'/home/create-channel'}>
                <Button
                    fullWidth
                    variant={'text'}
                    sx={{ justifyContent: 'flex-start', gap: 1 }}
                >
                    <Add />
                    <Typography variant={'body1'}>Create Channel</Typography>
                </Button>
            </Link>
            <List style={{ width: '100%' }}>
                {state.type === 'error' && channels.length === 0 && (
                    <Button
                        size="large"
                        onClick={loadMore}
                        startIcon={<Replay />}
                        sx={{ justifyContent: 'center' }}
                    >
                        Retry
                    </Button>
                )}
                {channels.length === 0 && state.type === 'loaded' && (
                    <ListItem sx={{ mt: 2 }}>
                        <ListItemText
                            sx={{ textAlign: 'center' }}
                            primary={'No Channels yet'}
                        />
                    </ListItem>
                )}
                {channels.length > 0 &&
                    channels.map((channel) => (
                        <ListItem
                            key={channel.id.value}
                            sx={{
                                p: 0,
                                color: 'primary.main',
                                justifyContent: 'center',
                                width: '100%',
                            }}
                        >
                            <ChannelSideView
                                channel={channel}
                                selected={
                                    selectedChannel?.id.value ===
                                    channel.id.value
                                }
                                setSelected={setSelectedChannel}
                            />
                        </ListItem>
                    ))}
                {state.type === 'loading' && (
                    <LoadingSpinner
                        circularProgressProps={{ size: 24, sx: { mt: 2 } }}
                    />
                )}
            </List>
        </DropDownMenu>
    );
}

function ChannelSideView({
    channel,
    selected,
    setSelected,
}: {
    channel: Channel;
    selected: boolean;
    setSelected: (channel: Channel) => void;
}) {
    const sessionManager = useSessionManager();
    const userRole = channel.members.find(
        (member) => member.id.value === sessionManager.session.user.id.value,
    )?.role;
    return (
        <Fade style={{ width: '100%' }} direction={'up'} triggerOnce>
            <Stack direction={'row'} spacing={1} width={'100%'}>
                <Button
                    fullWidth
                    onClick={() => setSelected(channel)}
                    sx={{ justifyContent: 'flex-start' }}
                    variant={selected ? 'outlined' : 'text'}
                >
                    {userRole === ChannelRole.OWNER && <GavelOutlined />}
                    {userRole === ChannelRole.MEMBER && <ChatBubbleOutline />}
                    {userRole === ChannelRole.GUEST && <MenuBookOutlined />}
                    <Typography variant={'body1'}>
                        {channel.name.value}
                    </Typography>
                </Button>
            </Stack>
        </Fade>
    );
}
