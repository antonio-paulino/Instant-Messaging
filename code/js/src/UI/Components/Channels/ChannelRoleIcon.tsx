import { ChannelRole } from '../../../Domain/channel/ChannelRole';
import * as React from 'react';
import { ChatBubbleOutline, GavelOutlined, MenuBookOutlined } from '@mui/icons-material';

export function ChannelRoleIcon({ role }: { role: ChannelRole }) {
    return (
        <React.Fragment>
            {role === ChannelRole.OWNER && <GavelOutlined />}
            {role === ChannelRole.MEMBER && <ChatBubbleOutline />}
            {role === ChannelRole.GUEST && <MenuBookOutlined />}
        </React.Fragment>
    );
}
