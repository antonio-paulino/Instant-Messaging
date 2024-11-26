import { EventListener, useEventManager } from '../Providers/Events';
import { useAlert } from '../Providers/Alerts';
import { ChannelInvitation } from '../../../Domain/invitations/ChannelInvitation';
import { useEffect } from 'react';
import { Identifier } from '../../../Domain/wrappers/identifier/Identifier';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import { Button, List, ListItem, ListItemText } from '@mui/material';
import { Replay } from '@mui/icons-material';
import { LoadingSpinner } from '../LoadingSpinner';
import * as React from 'react';
import DropDownMenu from '../DropDownMenu';
import InvitationView from './Invitation';
import { useInfiniteScrollContextInvitations } from '../Providers/InfiniteScrollProvider';

export function InvitationsDropDown() {
    const eventManager = useEventManager();
    const { showAlert } = useAlert();
    const limit = 10;

    const {
        state,
        loadMore,
        handleItemCreate,
        handleItemUpdate,
        handleItemDelete,
    } = useInfiniteScrollContextInvitations();

    useEffect(() => {
        if (state.type === 'error') {
            showAlert({
                message: 'Failed to load invitations',
                severity: 'error',
            });
        }
        if (
            state.type === 'initial' ||
            (state.type === 'loaded' &&
                state.paginationState.items.length < limit)
        ) {
            loadMore();
        }
        const eventListeners: EventListener[] = [
            {
                type: 'invitation-created',
                listener: (event: MessageEvent<string>) => {
                    const invitation = eventManager.handleEvent(event)
                        .data as ChannelInvitation;
                    handleItemCreate(invitation);
                },
            },
            {
                type: 'invitation-updated',
                listener: (event: MessageEvent<string>) =>
                    handleItemUpdate(
                        eventManager.handleEvent(event)
                            .data as ChannelInvitation,
                    ),
            },
            {
                type: 'invitation-deleted',
                listener: (event: MessageEvent<string>) =>
                    handleItemDelete(
                        eventManager.handleEvent(event).data as Identifier,
                    ),
            },
        ];
        eventListeners.forEach(eventManager.addListener);
        return () => {
            eventListeners.forEach(eventManager.removeListener);
        };
    }, [state.type, state.paginationState.items.length]);

    return (
        <DropDownMenu
            maxHeight={'24vh'}
            maxWidth={'100%'}
            loadMore={loadMore}
            bottomScroll={true}
            startOpen={true}
            header={
                <Box>
                    <Typography variant={'body1'}>Invitations</Typography>
                </Box>
            }
        >
            <List>
                {state.type === 'error' &&
                    state.paginationState.items.length === 0 && (
                        <Button
                            size="large"
                            onClick={loadMore}
                            startIcon={<Replay />}
                        >
                            Retry
                        </Button>
                    )}
                {state.paginationState.items.length === 0 &&
                    state.type === 'loaded' && (
                        <ListItem>
                            <ListItemText
                                primary={'No invitations'}
                                secondary={'You have no pending invitations'}
                            />
                        </ListItem>
                    )}
                {state.paginationState.items.map((invitation) => (
                    <ListItem key={invitation.id.value} sx={{ p: 0 }}>
                        <InvitationView
                            handleItemDelete={handleItemDelete}
                            invitation={invitation}
                        />
                    </ListItem>
                ))}
                {state.type === 'loading' && (
                    <LoadingSpinner
                        circularProgressProps={{ size: 24, sx: { mt: 2 } }}
                    />
                )}
            </List>
        </DropDownMenu>
    );
}
