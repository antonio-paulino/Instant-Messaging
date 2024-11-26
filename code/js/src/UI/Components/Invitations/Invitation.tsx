import { Identifier } from '../../../Domain/wrappers/identifier/Identifier';
import { ChannelInvitation } from '../../../Domain/invitations/ChannelInvitation';
import { useSessionManager } from '../Providers/Session';
import { useAlert } from '../Providers/Alerts';
import { InvitationService } from '../../../Services/invitations/InvitationService';
import * as React from 'react';
import { Fade } from 'react-awesome-reveal';
import Stack from '@mui/material/Stack';
import {
    ChatBubbleOutline,
    CheckRounded,
    CloseRounded,
    MenuBookOutlined,
} from '@mui/icons-material';
import { ListItemText } from '@mui/material';
import IconButton from '@mui/material/IconButton';
import { useInfiniteScrollContextChannels } from '../Providers/InfiniteScrollProvider';

export default function InvitationView(props: {
    handleItemDelete: (itemId: Identifier) => void;
    invitation: ChannelInvitation;
}) {
    const sessionManager = useSessionManager();
    const { showAlert } = useAlert();
    const { state, handleItemCreate: handleCreateChannel } =
        useInfiniteScrollContextChannels();

    const acceptInvitation = async () => {
        const res = await sessionManager.executeWithRefresh(async () => {
            return await InvitationService.acceptInvitation(props.invitation);
        });
        if (res.isSuccess()) {
            props.handleItemDelete(props.invitation.id);
            if (
                state.paginationState.items.length === 0 ||
                state.paginationState.items[
                    state.paginationState.items.length - 1
                ].id.value > props.invitation.channel.id.value
            ) {
                handleCreateChannel({
                    ...props.invitation.channel,
                    members: [
                        ...props.invitation.channel.members,
                        {
                            id: props.invitation.invitee.id,
                            name: props.invitation.invitee.name,
                            role: props.invitation.role,
                        },
                    ],
                });
            }
            showAlert({ message: 'Invitation accepted', severity: 'success' });
        } else {
            showAlert({
                message: 'Failed to accept invitation',
                severity: 'error',
            });
        }
    };

    const declineInvitation = async () => {
        const res = await sessionManager.executeWithRefresh(async () => {
            return await InvitationService.declineInvitation(props.invitation);
        });
        if (res.isSuccess()) {
            props.handleItemDelete(props.invitation.id);
            showAlert({ message: 'Invitation declined', severity: 'success' });
        } else {
            showAlert({
                message: 'Failed to decline invitation',
                severity: 'error',
            });
        }
    };

    return (
        <React.Fragment>
            <Fade direction={'up'} delay={0.25} triggerOnce>
                <Stack
                    direction={'row'}
                    alignItems={'center'}
                    justifyContent={'space-between'}
                    sx={{ gap: 2 }}
                    width="100%"
                >
                    {props.invitation.role === 'GUEST' ? (
                        <MenuBookOutlined />
                    ) : (
                        <ChatBubbleOutline />
                    )}
                    <ListItemText
                        sx={{ overflow: 'hidden', textWrap: 'wrap' }}
                        primary={props.invitation.channel.name.value}
                        secondary={props.invitation.invitee.name.value}
                    />
                    <IconButton
                        aria-label={'accept'}
                        size={'small'}
                        onClick={acceptInvitation}
                    >
                        <CheckRounded />
                    </IconButton>
                    <IconButton
                        aria-label={'decline'}
                        size={'small'}
                        onClick={declineInvitation}
                    >
                        <CloseRounded />
                    </IconButton>
                </Stack>
            </Fade>
        </React.Fragment>
    );
}
