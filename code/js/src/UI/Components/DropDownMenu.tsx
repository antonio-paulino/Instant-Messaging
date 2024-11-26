import * as React from 'react';
import { Button, Collapse, Fade } from '@mui/material';
import Stack from '@mui/material/Stack';
import { ArrowDropDown, ArrowDropUp } from '@mui/icons-material';
import Box from '@mui/material/Box';
import { handleScroll } from '../../Utils/Scroll';

interface DropDownMenuProps {
    maxHeight?: string;
    maxWidth?: string;
    loadMore?: () => void;
    startOpen: boolean;
    bottomScroll?: boolean;
    header: React.ReactNode;
    children: React.ReactNode;
}

export default function DropDownMenu({
    maxHeight,
    maxWidth,
    loadMore,
    startOpen,
    bottomScroll,
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
                onClick={() => setOpen((prevOpen) => !prevOpen)}
                variant="text"
                fullWidth
            >
                <Stack
                    direction={'row'}
                    justifyContent={'space-between'}
                    alignItems={'center'}
                    width={'100%'}
                >
                    {header}
                    {open ? <ArrowDropUp /> : <ArrowDropDown />}
                </Stack>
            </Button>
            <Collapse in={open} timeout={250} sx={{ width: '100%' }}>
                <Fade in={open} timeout={500} style={{ width: '100%' }}>
                    <Box
                        id="slide-down-menu"
                        sx={{
                            backgroundColor: 'background.paper',
                            overflow: 'auto',
                            width: maxWidth,
                            maxHeight: maxHeight,
                        }}
                        onScroll={(event) =>
                            handleScroll(event, bottomScroll, loadMore)
                        }
                    >
                        {children}
                    </Box>
                </Fade>
            </Collapse>
        </React.Fragment>
    );
}