import { CircularProgress, CircularProgressProps } from '@mui/material';
import * as React from 'react';

export function LoadingSpinner(props: {
    text?: string;
    circularProgressProps?: CircularProgressProps;
}) {
    return (
        <div style={{ display: 'flex', justifyContent: 'center' }}>
            <CircularProgress {...props.circularProgressProps}>
                {props.text && <div>{props.text}</div>}
            </CircularProgress>
        </div>
    );
}
