import { EventListener, useEventManager } from '../../Providers/EventsProvider';
import { AlertMessage, useAlertContext } from '../../Providers/AlertsProvider';
import { ChannelInvitation } from '../../../Domain/invitations/ChannelInvitation';
import { useEffect } from 'react';
import { Identifier } from '../../../Domain/wrappers/identifier/Identifier';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import { ListItem, ListItemText } from '@mui/material';
import { LoadingSpinner } from '../Utils/State/LoadingSpinner';
import * as React from 'react';
import DropDownMenu from '../Utils/Menu/DropDownMenu';
import InvitationView from './Invitation';
import { useInfiniteScrollContextInvitations } from '../../Providers/InfiniteScrollProvider';
import { RetryButton } from '../Utils/Actions/RetryButton';
import { InfiniteScrollState } from '../../State/useInfiniteScroll';
import { VirtualizedList } from '../Utils/VirtualizedList';
import { useSessionManager } from '../../Providers/SessionProvider';

export function InvitationsDropDown() {
    const { showAlert, invitationNotifications, clearInvitationNotifications } = useAlertContext();
    const { state, items, handleItemDelete, loadMore } = useInvitationsDropDown(showAlert);
    return (
        <DropDownMenu
            startOpen={true}
            notifications={invitationNotifications}
            clearNotifications={clearInvitationNotifications}
            header={
                <Box>
                    <Typography variant={'body1'}>Invitations</Typography>
                </Box>
            }
        >
            <VirtualizedList
                items={items}
                fixedHeight={50}
                listStyle={{ height: '30vh' }}
                itemStyle={{
                    marginBottom: '0.5em',
                    width: '100%',
                    alignSelf: 'center',
                    justifySelf: 'center',
                }}
                renderItem={({ index, style }) => {
                    const invitation = items[index];
                    return (
                        <ListItem
                            key={invitation.id.value}
                            sx={{
                                ...style,
                                p: 0,
                                height: '50px',
                                justifyContent: 'center',
                            }}
                        >
                            <InvitationView handleItemDelete={handleItemDelete} invitation={invitation} />
                        </ListItem>
                    );
                }}
                header={
                    state.type === 'error' &&
                    items.length === 0 && (
                        <Box
                            sx={{
                                width: '100%',
                                display: 'flex',
                                justifyContent: 'center',
                            }}
                        >
                            <RetryButton onClick={loadMore} />
                        </Box>
                    )
                }
                footer={
                    state.type === 'loading' ? (
                        <LoadingSpinner />
                    ) : state.type === 'loaded' && items.length === 0 ? (
                        <ListItemText
                            style={{ textAlign: 'center' }}
                            primary={'No invitations'}
                            secondary={'You have no pending invitations'}
                        />
                    ) : null
                }
                onItemsRendered={(start, end) => {
                    if (end === items.length - 1) {
                        loadMore();
                    }
                }}
            />
        </DropDownMenu>
    );
}

interface InvitationsDropDownHook {
    state: InfiniteScrollState<ChannelInvitation>;
    items: ChannelInvitation[];
    handleItemDelete: (id: Identifier) => void;
    loadMore: () => void;
}

function useInvitationsDropDown(showAlert: (alert: AlertMessage) => void): InvitationsDropDownHook {
    const { state, loadMore, handleItemDelete, handleItemCreate, handleItemUpdate } =
        useInfiniteScrollContextInvitations();
    const sessionManager = useSessionManager();
    const eventManager = useEventManager();

    const eventListeners: EventListener[] = React.useMemo(
        () => [
            {
                type: 'invitation-created',
                listener: (event: MessageEvent<string>) => {
                    const invitation = eventManager.handleEvent(event).data as ChannelInvitation;
                    if (
                        !state.paginationState.info.next &&
                        invitation.invitee.id.value === sessionManager.session.user.id.value
                    ) {
                        handleItemCreate(invitation);
                    }
                },
            },
            {
                type: 'invitation-updated',
                listener: (event: MessageEvent<string>) =>
                    handleItemUpdate(eventManager.handleEvent(event).data as ChannelInvitation),
            },
            {
                type: 'invitation-deleted',
                listener: (event: MessageEvent<string>) =>
                    handleItemDelete(eventManager.handleEvent(event).data as Identifier),
            },
        ],
        [state.paginationState.info.next === null],
    );

    useEffect(() => {
        if (eventManager.isOpen) {
            eventListeners.forEach(eventManager.addListener);
        }
        return () => {
            eventListeners.forEach(eventManager.removeListener);
        };
    }, [eventManager.isOpen]);

    useEffect(() => {
        if (state.type === 'error') {
            showAlert({
                message: 'Failed to load invitations',
                severity: 'error',
            });
        }
    }, [state.type]);

    return {
        state,
        items: state.paginationState.items,
        handleItemDelete,
        loadMore,
    };
}
