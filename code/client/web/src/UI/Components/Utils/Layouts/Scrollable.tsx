import { SxProps } from '@mui/material';
import React from 'react';
import Box from '@mui/material/Box';
import { Theme } from '@mui/material/styles';
import { handleScrollThrottled } from '../../../../Utils/Scroll';

export function Scrollable(props: {
    sx?: SxProps<Theme>;
    scrollDown: boolean;
    loadMore: () => void;
    children: React.ReactNode;
}) {
    const scrollRef = React.useRef<HTMLDivElement>(null);
    const throttleRef = React.useRef<boolean>(false);
    return (
        <Box
            ref={scrollRef}
            sx={{
                ...props.sx,
                display: 'flex',
                overflow: 'auto',
                flexDirection: props.scrollDown ? 'column' : 'column-reverse',
            }}
            onScroll={() => handleScrollThrottled(throttleRef, scrollRef, props.scrollDown, props.loadMore)}
        >
            {props.children}
        </Box>
    );
}
