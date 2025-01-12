import { Identifier } from '../../../Domain/wrappers/identifier/Identifier';
import { ChannelInvitation } from '../../../Domain/invitations/ChannelInvitation';
import { useSessionManager } from '../../Providers/SessionProvider';
import { useAlertContext } from '../../Providers/AlertsProvider';
import { InvitationService } from '../../../Services/invitations/InvitationService';
import * as React from 'react';
import Stack from '@mui/material/Stack';
import { CheckRounded, CloseRounded } from '@mui/icons-material';
import { ListItemText } from '@mui/material';
import IconButton from '@mui/material/IconButton';
import { InvitationIcon } from './InvitationIcon';

export default function InvitationView(props: {
    handleItemDelete: (itemId: Identifier) => void;
    invitation: ChannelInvitation;
}) {
    const { acceptInvitation, declineInvitation } = useInvitationView(props);
    return (
        <React.Fragment>
            <Stack
                direction={'row'}
                alignItems={'center'}
                justifyContent={'space-between'}
                sx={{ gap: 2 }}
                width="90%"
                height={'100%'}
            >
                <InvitationIcon invitation={props.invitation} />
                <ListItemText
                    sx={{ overflow: 'hidden', m: 0.5 }}
                    primary={props.invitation.channel.name.value}
                    secondary={props.invitation.inviter.name.value}
                />
                <IconButton aria-label={'accept'} onClick={acceptInvitation} sx={{ color: 'success.main' }}>
                    <CheckRounded />
                </IconButton>
                <IconButton aria-label={'decline'} sx={{ color: 'error.main' }} onClick={declineInvitation}>
                    <CloseRounded />
                </IconButton>
            </Stack>
        </React.Fragment>
    );
}

interface InvitationHook {
    acceptInvitation: () => void;
    declineInvitation: () => void;
}

function useInvitationView(props: { invitation: ChannelInvitation }): InvitationHook {
    const sessionManager = useSessionManager();
    const { showAlert } = useAlertContext();

    async function acceptInvitation() {
        const res = await sessionManager.executeWithRefresh(async () => {
            return await InvitationService.acceptInvitation(props.invitation);
        });
        if (res.isSuccess()) {
            showAlert({ message: 'Invitation accepted', severity: 'success' });
        } else {
            showAlert({
                message: res.getLeft().detail,
                severity: 'error',
            });
        }
    }

    async function declineInvitation() {
        const res = await sessionManager.executeWithRefresh(async () => {
            return await InvitationService.declineInvitation(props.invitation);
        });

        if (res.isSuccess()) {
            showAlert({ message: 'Invitation declined', severity: 'success' });
        } else {
            showAlert({
                message: res.getLeft().detail,
                severity: 'error',
            });
        }
    }

    return { acceptInvitation, declineInvitation };
}
