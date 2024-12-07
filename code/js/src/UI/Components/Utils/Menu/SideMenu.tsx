import { styled } from '@mui/material/styles';
import MuiDrawer from '@mui/material/Drawer';
import { drawerClasses } from '@mui/material';
import * as React from 'react';
import { MenuContent } from './MenuContent';
import { AuthService } from '../../../../Services/auth/AuthService';
import { Routes } from '../../../../routes';
import { useSessionManager } from '../../../Providers/SessionProvider';
import { useNavigate } from 'react-router-dom';
import { useAlertContext } from '../../../Providers/AlertsProvider';
import { SideMenuMobile } from './SideMenuMobile';

const Drawer = styled(MuiDrawer)({
    width: 360,
    flexShrink: 0,
    boxSizing: 'border-box',
    mt: 10,
    backgroundColor: 'background.paper',
    [`& .${drawerClasses.paper}`]: {
        width: 360,
        boxSizing: 'border-box',
    },
});

export default function SideMenu() {
    const sessionManager = useSessionManager();
    const navigate = useNavigate();
    const { showAlert } = useAlertContext();

    async function handleLogout() {
        const res = await sessionManager.executeWithRefresh(() => AuthService.logout());
        if (res.isSuccess()) {
            sessionManager.clearSession();
            navigate(Routes.SIGN_IN);
        } else {
            showAlert({
                message: 'Failed to logout',
                severity: 'error',
            });
        }
    }
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
                <MenuContent handleLogout={handleLogout} />
            </Drawer>
            <SideMenuMobile handleLogout={handleLogout} />
        </React.Fragment>
    );
}
