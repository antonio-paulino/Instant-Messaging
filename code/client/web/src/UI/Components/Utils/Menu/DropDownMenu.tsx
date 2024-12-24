import * as React from 'react';
import { Badge, Button, Collapse, Fade } from '@mui/material';
import Stack from '@mui/material/Stack';
import { ArrowDropDown, ArrowDropUp } from '@mui/icons-material';
import Box from '@mui/material/Box';

interface DropDownMenuProps {
    maxHeight?: string;
    maxWidth?: string;
    startOpen: boolean;
    scrollable?: boolean;
    onToggleOpen?: (isOpen: boolean) => void;
    bottomScroll?: boolean;
    notifications?: number;
    clearNotifications?: () => void;
    header: React.ReactNode;
    children: React.ReactNode;
}

export default function DropDownMenu({
    maxHeight,
    maxWidth,
    startOpen,
    onToggleOpen,
    notifications,
    clearNotifications,
    header,
    children,
}: DropDownMenuProps) {
    const [open, setOpen] = React.useState(startOpen);
    return (
        <React.Fragment>
            <Button
                id="slide-down-menu-button"
                aria-controls={open ? 'slide-down-menu' : undefined}
                aria-haspopup="true"
                aria-expanded={open ? 'true' : undefined}
                onClick={() => {
                    setOpen(!open);
                    if (onToggleOpen) {
                        onToggleOpen(!open);
                    }
                    if (clearNotifications) {
                        clearNotifications();
                    }
                }}
                variant="text"
                fullWidth
                sx={{ p: 1, overflowX: 'hidden' }}
            >
                <Stack direction={'row'} justifyContent={'space-between'} alignItems={'center'} width={'100%'}>
                    <Badge badgeContent={notifications} invisible={notifications === 0 || open} color={'error'}>
                        {header}
                    </Badge>
                    {open ? <ArrowDropUp /> : <ArrowDropDown />}
                </Stack>
            </Button>
            <Collapse in={open} timeout={250} sx={{ width: '100%' }}>
                <Fade in={open} timeout={500} style={{ width: '100%' }}>
                    <Box
                        id="slide-down-menu"
                        sx={{
                            maxHeight: maxHeight,
                            maxWidth: maxWidth,
                            overflow: 'hidden',
                            width: '100%',
                        }}
                    >
                        {children}
                    </Box>
                </Fade>
            </Collapse>
        </React.Fragment>
    );
}
