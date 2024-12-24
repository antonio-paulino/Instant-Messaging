import * as React from 'react';
import { Badge, drawerClasses } from '@mui/material';
import IconButton from '@mui/material/IconButton';
import { MenuRounded } from '@mui/icons-material';
import { MenuContent } from './MenuContent';
import { styled } from '@mui/material/styles';
import MuiDrawer from '@mui/material/Drawer';

export const MobileDrawer = styled(MuiDrawer)({
    width: '100%',
    flexShrink: 0,
    boxSizing: 'border-box',
    mt: 10,
    backgroundColor: 'background.paper',
    [`& .${drawerClasses.paper}`]: {
        width: '100%',
        boxSizing: 'border-box',
    },
});

export function SideMenuMobile({ handleLogout }: { handleLogout: () => void }) {
    const [open, setOpen] = React.useState(false);
    return (
        <React.Fragment>
            <Badge
                color={'error'}
                invisible={open}
                sx={{
                    position: 'fixed',
                    right: '7rem',
                    padding: 1,
                    zIndex: 9999,
                    display: { xs: 'block', md: 'none' },
                }}
            >
                <IconButton size={'small'} onClick={() => setOpen(!open)}>
                    <MenuRounded />
                </IconButton>
            </Badge>
            <MobileDrawer
                open={open}
                onClose={() => setOpen(false)}
                variant={'temporary'}
                sx={{
                    width: '100%',
                    display: { xs: 'block', md: 'none' },
                    backgroundColor: 'background.paper',
                    [`& .MuiDrawer-paper`]: {
                        backgroundColor: 'background.paper',
                    },
                }}
            >
                <MenuContent handleLogout={handleLogout} setOpened={setOpen} />
            </MobileDrawer>
        </React.Fragment>
    );
}
