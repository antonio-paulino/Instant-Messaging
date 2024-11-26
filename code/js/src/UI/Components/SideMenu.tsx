import { styled } from '@mui/material/styles';
import MuiDrawer from '@mui/material/Drawer';
import { Avatar, drawerClasses } from '@mui/material';
import { useSessionManager } from './Providers/Session';
import * as React from 'react';
import Stack from '@mui/material/Stack';
import { InvitationsDropDown } from './Invitations/InvitationDropDown';
import Divider from '@mui/material/Divider';
import { LogoutRounded, PersonRounded } from '@mui/icons-material';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import IconButton from '@mui/material/IconButton';
import { ChannelsDropDown } from './Channels/ChannelsDropDown';

const Drawer = styled(MuiDrawer)({
    width: 320,
    flexShrink: 0,
    boxSizing: 'border-box',
    mt: 10,
    [`& .${drawerClasses.paper}`]: {
        width: 320,
        boxSizing: 'border-box',
    },
});

export default function SideMenu({
    handleLogout,
}: {
    handleLogout: () => void;
}) {
    const sessionManager = useSessionManager();
    return (
        <React.Fragment>
            <Drawer
                variant={'permanent'}
                sx={{
                    display: { xs: 'none', md: 'block' },
                    [`& .MuiDrawer-paper`]: {
                        backgroundColor: 'background.paper',
                    },
                }}
            >
                <Stack
                    direction={'column'}
                    alignItems={'center'}
                    justifyContent={'center'}
                    sx={{ p: 2, gap: 2 }}
                >
                    <ChannelsDropDown />
                    <InvitationsDropDown />
                </Stack>
                <Divider />
                <Stack
                    direction={'row'}
                    justifyContent={'center'}
                    sx={{
                        p: 2,
                        gap: 2,
                        position: 'absolute',
                        bottom: 0,
                        width: '100%',
                    }}
                >
                    <Avatar>
                        <PersonRounded />
                    </Avatar>
                    <Box>
                        <Typography variant="body1">
                            {sessionManager.session.user.name.value}
                        </Typography>
                        <Typography
                            variant="caption"
                            sx={{ color: 'text.secondary' }}
                        >
                            {sessionManager.session.user.email.value}
                        </Typography>
                    </Box>
                    <IconButton aria-label={'logout'} onClick={handleLogout}>
                        <LogoutRounded />
                    </IconButton>
                </Stack>
            </Drawer>
        </React.Fragment>
    );
}
