import { ChannelInvitation } from '../../../Domain/invitations/ChannelInvitation';
import { ChatBubbleOutline, MenuBookOutlined } from '@mui/icons-material';
import * as React from 'react';

export function InvitationIcon(props: { invitation: ChannelInvitation }) {
    if (props.invitation.role === 'GUEST') {
        return <MenuBookOutlined />;
    } else {
        return <ChatBubbleOutline />;
    }
}
