import React, { useEffect } from 'react';
import { SessionManager, useSessionManager } from '../../../../Providers/SessionProvider';
import { useChannel } from '../../../../../Utils/Hooks/channel';
import { Navigate } from 'react-router-dom';
import { Routes } from '../../../../../routes';
import { InvitationService } from '../../../../../Services/invitations/InvitationService';
import { InfiniteScrollState, useInfiniteScroll } from '../../../../State/useInfiniteScroll';
import { ChannelInvitation } from '../../../../../Domain/invitations/ChannelInvitation';
import { PaginationRequest } from '../../../../../Domain/pagination/PaginationRequest';
import { useAlertContext } from '../../../../Providers/AlertsProvider';
import { ApiResult } from '../../../../../Services/media/Problem';
import { Pagination } from '../../../../../Domain/pagination/Pagination';
import { ChannelRole } from '../../../../../Domain/channel/ChannelRole';
import { Stack, Typography } from '@mui/material';
import { LoadingSpinner } from '../../../../Components/Utils/State/LoadingSpinner';
import { VirtualizedList } from '../../../../Components/Utils/VirtualizedList';
import { Channel } from '../../../../../Domain/channel/Channel';
import { RoleInput } from '../../../../Components/Utils/Input/RoleInput';
import { EventListener, useEventManager } from '../../../../Providers/EventsProvider';
import IconButton from '@mui/material/IconButton';
import { Delete } from '@mui/icons-material';
import { Identifier } from '../../../../../Domain/wrappers/identifier/Identifier';

export function ChannelInvitations() {
    const sessionManager = useSessionManager();
    const user = sessionManager.session.user;
    const { channel } = useChannel();

    const { state, loadMore, updateInvitation, deleteInvitation } = useChannelInvitations(sessionManager, channel);

    const isOwner = channel.owner.id.value === user.id.value;

    if (!isOwner) {
        return <Navigate to={Routes.CHANNEL_MEMBERS} />;
    }

    return (
        <VirtualizedList
            items={state.paginationState.items}
            onItemsRendered={(_, end) => {
                if (end === state.paginationState.items.length - 1) {
                    loadMore();
                }
            }}
            listStyle={{ height: '100%', width: '100%', padding: '1em' }}
            itemStyle={{ width: '90%', alignSelf: 'center', justifySelf: 'center' }}
            fixedHeight={80}
            renderItem={({ index, style }) => {
                const invitation = state.paginationState.items[index];
                return (
                    <Stack
                        key={invitation.id.value}
                        direction={'row'}
                        style={style}
                        gap={4}
                        justifyContent={'center'}
                        alignItems={'center'}
                    >
                        <Typography variant={'body1'}>{invitation.invitee.name.value}</Typography>
                        <RoleInput
                            name={'Role'}
                            value={invitation.role}
                            handleChange={(e) =>
                                updateInvitation(invitation, invitation.expiresAt, e.target.value as ChannelRole)
                            }
                            error={false}
                        />
                        <Typography>{`Expires ${invitation.expiresAt.toLocaleDateString()} at ${invitation.expiresAt.toLocaleTimeString()}`}</Typography>
                        <IconButton onClick={() => deleteInvitation(invitation)}>
                            <Delete />
                        </IconButton>
                    </Stack>
                );
            }}
            footer={state.type === 'loading' ? <LoadingSpinner /> : null}
        />
    );
}

interface ChannelInvitationsHook {
    state: InfiniteScrollState<ChannelInvitation>;
    loadMore: () => void;
    updateInvitation: (
        invitation: ChannelInvitation,
        newExpiration: Date | null,
        newRole: ChannelRole,
    ) => Promise<void>;
    deleteInvitation: (invitation: ChannelInvitation) => Promise<void>;
}

function useChannelInvitations(sessionManager: SessionManager, channel: Channel): ChannelInvitationsHook {
    const { showAlert } = useAlertContext();
    const eventManager = useEventManager();

    const { state, handleItemDelete, handleItemUpdate, loadMore, reset } = useInfiniteScroll<ChannelInvitation>({
        fetchItemsRequest: fetchChannelInvitations,
        limit: 10,
        useOffset: false,
        getCount: false,
    });

    const [prevChannel, setPrevChannel] = React.useState<Channel | null>(null);

    useEffect(() => {
        if (channel.id.value !== prevChannel?.id.value) {
            setPrevChannel(channel);
            reset();
        }
    }, [channel.id.value]);

    async function fetchChannelInvitations(
        pageRequest: PaginationRequest,
        items: ChannelInvitation[],
        signal: AbortSignal,
    ): ApiResult<Pagination<ChannelInvitation>> {
        return await sessionManager.executeWithRefresh(async () => {
            const after = items.length > 0 ? items[items.length - 1].id : null;
            return await InvitationService.getChannelInvitations(channel, pageRequest, null, after, signal);
        });
    }

    async function updateInvitation(invitation: ChannelInvitation, newExpiration: Date | null, newRole: ChannelRole) {
        const res = await sessionManager.executeWithRefresh(async () => {
            return await InvitationService.updateInvitation(
                invitation,
                newRole !== invitation.role ? newRole : undefined,
                newExpiration !== invitation.expiresAt ? newExpiration : undefined,
            );
        });
        if (res.isSuccess()) {
            showAlert({ message: 'Invitation updated', severity: 'success' });
            handleItemUpdate({
                ...invitation,
                expiresAt: newExpiration,
                role: newRole,
            });
        } else {
            showAlert({ message: res.getLeft()?.detail || 'Failed to update invitation', severity: 'error' });
        }
    }

    async function deleteInvitation(invitation: ChannelInvitation) {
        const res = await sessionManager.executeWithRefresh(async () => {
            return await InvitationService.deleteInvitation(invitation);
        });
        if (res.isSuccess()) {
            showAlert({ message: 'Invitation deleted', severity: 'success' });
            handleItemDelete(invitation.id);
        } else {
            showAlert({ message: res.getLeft()?.detail || 'Failed to delete invitation', severity: 'error' });
        }
    }

    const eventListeners: EventListener[] = [
        {
            type: 'invitation-updated',
            listener: (event: MessageEvent<string>) => {
                const invitation = eventManager.handleEvent(event).data as ChannelInvitation;
                handleItemUpdate(invitation);
            },
        },
        {
            type: 'invitation-deleted',
            listener: (event: MessageEvent<string>) => {
                const invitation = eventManager.handleEvent(event).data as Identifier;
                handleItemDelete(invitation);
            },
        },
    ];

    useEffect(() => {
        if (state.type === 'error') {
            showAlert({
                message: 'Failed to load invitations',
                severity: 'error',
            });
        }
    }, [state.type]);

    useEffect(() => {
        if (eventManager.isInitialized) {
            eventListeners.forEach(eventManager.addListener);
        }
        return () => {
            eventListeners.forEach(eventManager.removeListener);
        };
    }, [eventManager.isInitialized]);

    return {
        state,
        loadMore,
        updateInvitation,
        deleteInvitation,
    };
}
