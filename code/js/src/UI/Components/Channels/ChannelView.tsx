import React, { useEffect } from 'react';
import { Channel } from '../../../Domain/channel/Channel';
import MessagesContainer from '../Messages/MessagesContainer';
import { AppBar, ButtonBase, Toolbar } from '@mui/material';
import Typography from '@mui/material/Typography';
import { ChannelAccessIcon } from './ChannelAccessIcon';
import { MessageTextField } from '../Messages/MessageTextField';
import { Routes } from '../../../routes';
import { Link, Navigate } from 'react-router-dom';
import { useInfiniteScrollContextChannels } from '../../Providers/InfiniteScrollProvider';

export default function ChannelView() {
    const { state, selectedChannel } = useInfiniteScrollContextChannels();
    const channel = state.paginationState.items.find((channel: Channel) => channel.id.value === selectedChannel?.value);

    if (!channel) {
        return <Navigate to={Routes.HOME} />;
    }

    return (
        <React.Fragment>
            <ChannelTopBar channel={channel} />
            <MessagesContainer channel={channel} />
            <MessageTextField channel={channel} />
        </React.Fragment>
    );
}

export function ChannelTopBar({ channel }: { channel: Channel }) {
    return (
        <AppBar
            position="static"
            sx={{
                boxShadow: 0,
                bgcolor: 'background.paper',
                backgroundImage: 'none',
                borderBottom: '1px solid',
                borderColor: 'divider',
            }}
        >
            <ButtonBase sx={{ width: '100%', justifyContent: 'flex-start' }}>
                <Link
                    to={Routes.CHANNEL_SETTINGS}
                    style={{
                        width: '100%',
                        display: 'flex',
                        textDecoration: 'none',
                        color: 'inherit',
                    }}
                >
                    <Toolbar variant={'regular'}>
                        <ChannelAccessIcon isPublic={channel.isPublic} />
                        <Typography variant="body1" ml={1}>
                            {channel.name.value}
                        </Typography>
                    </Toolbar>
                </Link>
            </ButtonBase>
        </AppBar>
    );
}
