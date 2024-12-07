import { Button } from '@mui/material';
import { Replay } from '@mui/icons-material';
import * as React from 'react';

export function RetryButton(props: { onClick: () => void }) {
    return (
        <Button size="large" onClick={props.onClick} startIcon={<Replay />} sx={{ justifyContent: 'center' }}>
            Retry
        </Button>
    );
}
