import { Channel } from '../../../Domain/channel/Channel';
import { useSessionManager } from '../../Providers/SessionProvider';
import { useAlertContext } from '../../Providers/AlertsProvider';
import React from 'react';
import { ChannelService } from '../../../Services/channels/ChannelService';
import Stack from '@mui/material/Stack';
import { Button, ListItemIcon, ListItemText } from '@mui/material';
import { ChannelRoleIcon } from './ChannelRoleIcon';
import { LoadingSpinner } from '../Utils/State/LoadingSpinner';
import { TransitEnterexitOutlined } from '@mui/icons-material';
import Typography from '@mui/material/Typography';
import { User } from '../../../Domain/user/User';

export function ChannelSearchResult(props: { channel: Channel }) {
    const { channel } = props;
    const { loading, joinChannel, user } = useChannelSearchResult(channel);
    return (
        <Stack
            justifyContent={'center'}
            alignItems={'center'}
            direction="column"
            spacing={1}
            width={'100%'}
            padding={2}
            borderRadius={3}
            border="2px solid"
            height={100}
        >
            <Stack direction={'row'} justifyContent={'center'} alignItems={'center'} spacing={2}>
                <ListItemIcon sx={{ fontSize: '2rem' }}>
                    <ChannelRoleIcon role={channel.defaultRole} />
                </ListItemIcon>
                <ListItemText primary={<Typography variant="h6">{channel.name.value}</Typography>} />
            </Stack>
            <Stack direction={'row'} justifyContent={'center'} alignItems={'center'} spacing={2}>
                <Typography variant="body1">{channel.members.length} members</Typography>
                {loading ? (
                    <LoadingSpinner />
                ) : (
                    !channel.members.some((member) => member.id.value === user.id.value) && (
                        <Button onClick={joinChannel} startIcon={<TransitEnterexitOutlined />} size={'large'}>
                            Join
                        </Button>
                    )
                )}
            </Stack>
        </Stack>
    );
}

interface ChannelSearchResultHook {
    loading: boolean;
    joinChannel: () => void;
    user: User;
}

function useChannelSearchResult(channel: Channel): ChannelSearchResultHook {
    const sessionManager = useSessionManager();
    const { showAlert } = useAlertContext();
    const [loading, setLoading] = React.useState(false);

    async function joinChannel() {
        setLoading(true);
        const res = await sessionManager.executeWithRefresh(async () => {
            return await ChannelService.joinChannel(channel, sessionManager.session);
        });
        if (res.isSuccess()) {
            showAlert({
                message: `Joined ${channel.name.value}`,
                severity: 'success',
            });
        } else {
            showAlert({ message: 'Failed to join channel', severity: 'error' });
        }
        setLoading(false);
    }

    return { loading, joinChannel, user: sessionManager.session.user };
}
