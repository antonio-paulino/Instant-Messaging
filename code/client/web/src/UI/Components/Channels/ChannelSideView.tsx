import { Channel } from '../../../Domain/channel/Channel';
import { useSessionManager } from '../../Providers/SessionProvider';
import { Badge, Button, Stack } from '@mui/material';
import { ChannelRoleIcon } from './ChannelRoleIcon';
import Typography from '@mui/material/Typography';
import { ChannelAccessIcon } from './ChannelAccessIcon';
import * as React from 'react';
import { useAlertContext } from '../../Providers/AlertsProvider';
import { Identifier } from '../../../Domain/wrappers/identifier/Identifier';

export function ChannelSideView({
    channel,
    selected,
    setSelected,
}: {
    channel: Channel;
    selected: boolean;
    setSelected: (channel: Identifier) => void;
}) {
    const sessionManager = useSessionManager();
    const { channelNotifications, clearChannelNotifications } = useAlertContext();

    const userRole = channel.members.find((member) => member.id.value === sessionManager.session.user.id.value)!.role;

    return (
        <Stack direction={'row'} spacing={1} width={'90%'}>
            <Badge
                color={'error'}
                badgeContent={channelNotifications[channel.id.value]}
                invisible={channelNotifications[channel.id.value] === 0}
                sx={{ width: '100%', marginTop: '0.5em' }}
            >
                <Button
                    fullWidth
                    onClick={() => {
                        setSelected(channel.id);
                        clearChannelNotifications(channel.id);
                    }}
                    sx={{
                        justifyContent: 'space-between',
                        height: 'fit-content',
                    }}
                    variant={selected ? 'outlined' : 'text'}
                >
                    <Stack direction={'row'} spacing={1} alignItems={'center'}>
                        <ChannelRoleIcon role={userRole} />
                        <Typography variant={'body1'}>{channel.name.value}</Typography>
                    </Stack>
                    <ChannelAccessIcon isPublic={channel.isPublic} />
                </Button>
            </Badge>
        </Stack>
    );
}
