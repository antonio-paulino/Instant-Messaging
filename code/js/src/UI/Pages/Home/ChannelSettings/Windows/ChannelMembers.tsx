import { SessionManager, useSessionManager } from '../../../../Providers/SessionProvider';
import React from 'react';
import { useChannel } from '../../../../../Utils/Hooks/channel';
import Stack from '@mui/material/Stack';
import Typography from '@mui/material/Typography';
import { ChannelRoleIcon } from '../../../../Components/Channels/ChannelRoleIcon';
import Button from '@mui/material/Button';
import { ChannelRole } from '../../../../../Domain/channel/ChannelRole';
import { Identifier } from '../../../../../Domain/wrappers/identifier/Identifier';
import { ChannelService } from '../../../../../Services/channels/ChannelService';
import { Channel } from '../../../../../Domain/channel/Channel';
import { useAlertContext } from '../../../../Providers/AlertsProvider';
import { ChannelMember } from '../../../../../Domain/channel/ChannelMember';
import { useMediaQuery, useTheme } from '@mui/material';

export function ChannelMembers() {
    const { channel } = useChannel();
    const sessionManager = useSessionManager();
    const isOwner = channel.owner.id.value === sessionManager.session?.user.id.value;
    const { updateMemberRole, removeMember } = useChannelMembers(sessionManager);

    return (
        <Stack
            direction={'column'}
            justifyContent={'center'}
            alignItems={'center'}
            padding={2}
            sx={(theme) => ({
                width: '100%',
                height: '100%',
                overflow: 'auto',
                [theme.breakpoints.down('md')]: {
                    width: '100%',
                },
            })}
        >
            <Typography variant="h6">Members</Typography>
            <Stack
                height={'100%'}
                width={'50%'}
                sx={(theme) => ({
                    pt: 2,
                    pb: 2,
                    [theme.breakpoints.down('md')]: {
                        width: '100%',
                    },
                })}
            >
                {channel.members
                    .sort((a, b) => -a.role.localeCompare(b.role))
                    .map((member) => (
                        <Stack
                            key={member.id.value}
                            direction={'row'}
                            marginTop={1}
                            marginBottom={1}
                            gap={2}
                            alignItems={'center'}
                        >
                            <ChannelRoleIcon role={member.role} />
                            <Typography>{member.name.value}</Typography>
                            {isOwner && member.role !== ChannelRole.OWNER && (
                                <Stack
                                    direction={useMediaQuery(useTheme().breakpoints.down('sm')) ? 'column' : 'row'}
                                    gap={1}
                                >
                                    <Button
                                        variant="outlined"
                                        size="small"
                                        onClick={() =>
                                            updateMemberRole(
                                                channel,
                                                member,
                                                member.role === ChannelRole.GUEST
                                                    ? ChannelRole.MEMBER
                                                    : ChannelRole.GUEST,
                                            )
                                        }
                                    >
                                        {member.role === ChannelRole.GUEST
                                            ? 'Make Member'
                                            : member.role === ChannelRole.MEMBER
                                              ? 'Make Guest'
                                              : null}
                                    </Button>
                                    <Button
                                        variant="outlined"
                                        size="small"
                                        color="error"
                                        onClick={() => removeMember(channel, member.id)}
                                    >
                                        Kick
                                    </Button>
                                </Stack>
                            )}
                        </Stack>
                    ))}
            </Stack>
        </Stack>
    );
}

function useChannelMembers(sessionManager: SessionManager) {
    const { showAlert } = useAlertContext();

    async function updateMemberRole(channel: Channel, member: ChannelMember, newRole: ChannelRole) {
        const res = await sessionManager.executeWithRefresh(async () => {
            return await ChannelService.updateMemberRole(channel, member, newRole);
        });
        if (res.isFailure()) {
            showAlert({ message: res.getLeft().detail, severity: 'error' });
        }
    }

    async function removeMember(channel: Channel, memberId: Identifier) {
        const res = await sessionManager.executeWithRefresh(async () => {
            return await ChannelService.removeUserFromChannel(channel, memberId);
        });
        if (res.isFailure()) {
            showAlert({ message: res.getLeft().detail, severity: 'error' });
        }
    }

    return { updateMemberRole, removeMember };
}
