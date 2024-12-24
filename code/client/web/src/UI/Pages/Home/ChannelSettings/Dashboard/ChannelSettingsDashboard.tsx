import { Routes } from '../../../../../routes';
import { useInfiniteScrollContextChannels } from '../../../../Providers/InfiniteScrollProvider';
import { useSessionManager } from '../../../../Providers/SessionProvider';
import Stack from '@mui/material/Stack';
import { useLocation, useNavigate } from 'react-router-dom';
import Button from '@mui/material/Button';
import Typography from '@mui/material/Typography';
import React from 'react';

type ChannelSettingsDashboardOption = {
    label: string;
    path: string;
    owner?: boolean;
    member?: boolean;
    bottom?: boolean;
    danger?: boolean;
};

const ChannelSettingsDashboardOptions = [
    { label: 'Edit Channel', owner: true, path: Routes.EDIT_CHANNEL },
    { label: 'Members', path: Routes.CHANNEL_MEMBERS },
    {
        label: 'Manage Invitations',
        owner: true,
        path: Routes.CHANNEL_INVITATIONS,
    },
    { label: 'Invite Member', owner: true, path: Routes.INVITE_CHANNEL_MEMBER },
    {
        label: 'Delete Channel',
        owner: true,
        bottom: true,
        danger: true,
        path: Routes.DELETE_CHANNEL,
    },
    {
        label: 'Leave Channel',
        member: true,
        bottom: true,
        danger: true,
        path: Routes.LEAVE_CHANNEL,
    },
];

export function ChannelSettingsDashboard() {
    const { state, selectedChannel } = useInfiniteScrollContextChannels();
    const channel = state.paginationState.items.find((channel) => channel.id.value === selectedChannel?.value);
    const isOwner = channel?.owner.id.value === useSessionManager().session.user.id.value;
    return (
        <Stack
            height={'100%'}
            sx={(theme) => ({
                width: '20%',
                pt: 2,
                pb: 2,
                borderRight: '1px solid',
                borderColor: 'divider',
                [theme.breakpoints.down('md')]: {
                    width: '30%',
                    gap: 1,
                },
            })}
        >
            {ChannelSettingsDashboardOptions.map((option) =>
                (option.owner && !isOwner) || (option.member && isOwner) ? null : (
                    <ChannelSettingsDashboardOption key={option.label} option={option} />
                ),
            )}
        </Stack>
    );
}

function ChannelSettingsDashboardOption({ option }: { option: ChannelSettingsDashboardOption }) {
    const navigate = useNavigate();
    const location = useLocation();
    const selected = location.pathname === option.path;
    return (
        <Button
            variant={'text'}
            color={option.danger ? 'error' : 'primary'}
            fullWidth
            onClick={() => navigate(option.path)}
            sx={{
                borderRadius: 0,
                marginTop: option.bottom ? 'auto' : undefined,
                bgcolor: selected ? 'primary' : 'transparent',
                pt: 2,
                pb: 2,
            }}
        >
            <Typography
                variant={'body1'}
                style={{ fontWeight: selected ? 'bold' : 'normal' }}
                color={selected ? 'primary' : 'text.primary'}
            >
                {option.label}
            </Typography>
        </Button>
    );
}
