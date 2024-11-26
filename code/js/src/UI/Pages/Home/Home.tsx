import { Outlet, useNavigate } from 'react-router-dom';
import * as React from 'react';
import Box from '@mui/material/Box';
import { useSessionManager } from '../../Components/Providers/Session';
import { useAbortSignal } from '../../Components/State/useAbortSignal';
import { AuthService } from '../../../Services/auth/AuthService';
import SideMenu from '../../Components/SideMenu';
import { PaginationRequest } from '../../../Domain/pagination/PaginationRequest';
import { Channel } from '../../../Domain/channel/Channel';
import { UserService } from '../../../Services/users/UserService';
import { ChannelInvitation } from '../../../Domain/invitations/ChannelInvitation';
import { InvitationService } from '../../../Services/invitations/InvitationService';
import {
    InfiniteScrollChannelProvider,
    InfiniteScrollInvitationProvider,
} from '../../Components/Providers/InfiniteScrollProvider';
import { AuthenticatedPage } from '../../../App';
import { useAlert } from '../../Components/Providers/Alerts';

export function Home() {
    const sessionManager = useSessionManager();
    const navigate = useNavigate();
    const signal = useAbortSignal();
    const { showAlert } = useAlert();

    const [selectedChannel, setSelectedChannel] = React.useState<Channel | null>(null);

    const fetchChannels = async (page: PaginationRequest, items: Channel[]) => {
        return await sessionManager.executeWithRefresh(async () => {
            const after = items.length > 0 ? items[items.length - 1].id : null;
            return await UserService.getUserChannels(
                sessionManager.session.user,
                false,
                null,
                page,
                after,
            );
        });
    };

    const fetchInvitations = async (
        page: PaginationRequest,
        items: ChannelInvitation[],
    ) => {
        return await sessionManager.executeWithRefresh(async () => {
            const after = items.length > 0 ? items[items.length - 1].id : null;
            return await InvitationService.getUserInvitations(
                sessionManager.session.user,
                page,
                null,
                after,
            );
        });
    };

    function handleLogout() {
        sessionManager
            .executeWithRefresh(() => {
                return AuthService.logout(signal);
            })
            .then((res) => {
                if (res.isSuccess()) {
                    sessionManager.clearSession();
                    navigate('/sign-in');
                } else {
                    showAlert({
                        message: 'Failed to logout',
                        severity: 'error',
                    });
                }
            });
    }

    // The main box should include the currently selected channel's view
    return (
        <AuthenticatedPage>
            <InfiniteScrollChannelProvider
                fetchItemsRequest={fetchChannels}
                limit={10}
                useOffset={false}
                getCount={false}
                selectedChannel={selectedChannel}
                setSelectedChannel={setSelectedChannel}
            >
                <InfiniteScrollInvitationProvider
                    fetchItemsRequest={fetchInvitations}
                    limit={10}
                    useOffset={false}
                    getCount={false}
                >
                    <Box sx={{ display: 'flex' }}>
                        <SideMenu handleLogout={handleLogout} />
                        <Box
                            component={'main'}
                            sx={(theme) => ({
                                flexGrow: 1,
                                backgroundColor:
                                    theme.palette.background.default,
                                overflow: 'auto',
                            })}
                        >
                            <Outlet />
                        </Box>
                    </Box>
                </InfiniteScrollInvitationProvider>
            </InfiniteScrollChannelProvider>
        </AuthenticatedPage>
    );
}
