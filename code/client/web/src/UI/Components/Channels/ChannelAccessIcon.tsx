import * as React from 'react';
import { Lock, Public } from '@mui/icons-material';

export function ChannelAccessIcon({ isPublic }: { isPublic: boolean }) {
    return <React.Fragment>{isPublic ? <Public /> : <Lock />}</React.Fragment>;
}
