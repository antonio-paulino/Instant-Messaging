import Stack from '@mui/material/Stack';
import { ChannelsDropDown } from '../../Channels/ChannelsDropDown';
import { InvitationsDropDown } from '../../Invitations/InvitationDropDown';
import { Link } from 'react-router-dom';
import { Routes } from '../../../../routes';
import Button from '@mui/material/Button';
import { LogoutRounded, PersonAdd, PersonRounded, Search } from '@mui/icons-material';
import Typography from '@mui/material/Typography';
import Divider from '@mui/material/Divider';
import { Avatar } from '@mui/material';
import Box from '@mui/material/Box';
import IconButton from '@mui/material/IconButton';
import * as React from 'react';
import { useSessionManager } from '../../../Providers/SessionProvider';

export function MenuContent(props: { handleLogout: () => void; setOpened?: (opened: boolean) => void }) {
    const sessionManager = useSessionManager();
    return (
        <Stack
            direction={'column'}
            alignItems={'center'}
            justifyContent={'center'}
            sx={(theme) => ({
                p: 2,
                gap: 1,
                [theme.breakpoints.down('md')]: {
                    p: 1,
                    gap: 0,
                },
            })}
        >
            <ChannelsDropDown />
            <InvitationsDropDown />
            <Stack
                direction={'column'}
                justifyContent={'center'}
                sx={{
                    p: 2,
                    gap: 2,
                    position: 'absolute',
                    bottom: 0,
                    width: '100%',
                }}
            >
                <Link to={Routes.SEARCH_CHANNELS}>
                    <Button
                        fullWidth
                        variant={'text'}
                        sx={{
                            justifyContent: 'center',
                            gap: 1,
                            p: 2,
                            textAlign: 'center',
                        }}
                        onClick={() => props.setOpened && props.setOpened(false)}
                    >
                        <Search />
                        <Typography variant={'body1'}>Search Channels</Typography>
                    </Button>
                </Link>

                <Link to={Routes.CREATE_IM_INVITATION}>
                    <Button
                        fullWidth
                        variant={'text'}
                        sx={{
                            justifyContent: 'center',
                            gap: 1,
                            p: 2,
                            textAlign: 'center',
                        }}
                        onClick={() => props.setOpened && props.setOpened(false)}
                    >
                        <PersonAdd />
                        <Typography variant={'body1'}>Invite others</Typography>
                    </Button>
                </Link>
                <Divider />
                <Stack direction={'row'} gap={1} alignItems={'center'} justifyContent={'center'}>
                    <Avatar>
                        <PersonRounded />
                    </Avatar>
                    <Box>
                        <Typography variant="body1">{sessionManager.session.user.name.value}</Typography>
                        <Typography variant="caption" sx={{ color: 'text.secondary' }}>
                            {sessionManager.session.user.email.value}
                        </Typography>
                    </Box>
                    <IconButton aria-label={'logout'} onClick={props.handleLogout}>
                        <LogoutRounded />
                    </IconButton>
                </Stack>
            </Stack>
        </Stack>
    );
}
