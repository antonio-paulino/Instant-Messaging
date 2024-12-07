import { Outlet, useNavigate } from 'react-router-dom';
import * as React from 'react';
import Box from '@mui/material/Box';
import { useLoggedIn, useSessionManager } from '../../Providers/SessionProvider';
import { PaginationRequest } from '../../../Domain/pagination/PaginationRequest';
import { Channel } from '../../../Domain/channel/Channel';
import { UserService } from '../../../Services/users/UserService';
import { ChannelInvitation } from '../../../Domain/invitations/ChannelInvitation';
import { InvitationService } from '../../../Services/invitations/InvitationService';
import {
    InfiniteScrollChannelProvider,
    InfiniteScrollInvitationProvider,
} from '../../Providers/InfiniteScrollProvider';
import { AlertProvider } from '../../Providers/AlertsProvider';
import { useEffect } from 'react';
import { useEventManager } from '../../Providers/EventsProvider';
import ChannelView from '../../Components/Channels/ChannelView';
import SideMenu from '../../Components/Utils/Menu/SideMenu';
import { Identifier } from '../../../Domain/wrappers/identifier/Identifier';
import { ApiResult } from '../../../Services/media/Problem';
import { Pagination } from '../../../Domain/pagination/Pagination';
import { AuthenticatedPage } from '../../Components/AuthenticatedPage';

export function Home() {
    const [selectedChannel, setSelectedChannel] = React.useState<Identifier | null>(null);
    const { fetchInvitations, fetchChannels } = useHome();
    return (
        <AuthenticatedPage>
            <InfiniteScrollChannelProvider
                fetchItemsRequest={fetchChannels}
                limit={20}
                useOffset={false}
                getCount={false}
                selectedChannel={selectedChannel}
                setSelectedChannel={setSelectedChannel}
            >
                <InfiniteScrollInvitationProvider
                    fetchItemsRequest={fetchInvitations}
                    limit={20}
                    useOffset={false}
                    getCount={false}
                >
                    <AlertProvider>
                        <Box sx={{ display: 'flex', overflow: 'hidden' }}>
                            <SideMenu />
                            <Box
                                component={'main'}
                                sx={{
                                    flexGrow: 1,
                                    overflow: 'hidden',
                                }}
                            >
                                {selectedChannel && <ChannelView />}
                                <Outlet />
                            </Box>
                        </Box>
                    </AlertProvider>
                </InfiniteScrollInvitationProvider>
            </InfiniteScrollChannelProvider>
        </AuthenticatedPage>
    );
}

interface HomeHook {
    fetchChannels: (page: PaginationRequest, items: Channel[], signal: AbortSignal) => ApiResult<Pagination<Channel>>;
    fetchInvitations: (
        page: PaginationRequest,
        items: ChannelInvitation[],
        signal: AbortSignal,
    ) => ApiResult<Pagination<ChannelInvitation>>;
}

function useHome(): HomeHook {
    const eventManager = useEventManager();
    const sessionManager = useSessionManager();
    const login = useLoggedIn();

    useEffect(() => {
        if (login && !eventManager.isInitialized) {
            eventManager.setupEventSource();
        }
        return () => {
            eventManager.destroyEventSource();
        };
    }, [login, eventManager.isInitialized]);

    const fetchChannels = async (page: PaginationRequest, items: Channel[], signal: AbortSignal) => {
        return await sessionManager.executeWithRefresh(async () => {
            const after = items.length > 0 ? items[items.length - 1].id : null;
            return await UserService.getUserChannels(sessionManager.session.user, false, null, page, after, signal);
        });
    };

    const fetchInvitations = async (page: PaginationRequest, items: ChannelInvitation[], signal: AbortSignal) => {
        return await sessionManager.executeWithRefresh(async () => {
            const after = items.length > 0 ? items[items.length - 1].id : null;
            return await InvitationService.getUserInvitations(sessionManager.session.user, page, null, after, signal);
        });
    };

    return { fetchInvitations, fetchChannels };
}
