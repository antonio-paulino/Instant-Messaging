import React from 'react';
import { Backdrop, CardHeader, Fade, SxProps } from '@mui/material';
import { Card } from '../../../Pages/SignIn/SignIn';
import IconButton from '@mui/material/IconButton';
import { Close } from '@mui/icons-material';
import Box from '@mui/material/Box';

export function Window({
    title,
    onClose,
    children,
    width,
    height,
    sx,
}: {
    width?: string;
    title?: string;
    onClose: () => void;
    children: React.ReactNode;
    height?: string;
    sx?: any;
}) {
    return (
        <React.Fragment>
            <Backdrop open={true} sx={{ backdropFilter: 'blur(5px)' }} onClick={onClose} />
            <Fade in={true} timeout={500}>
                <Card
                    variant="outlined"
                    sx={(theme) => ({
                        ...sx,
                        height: height || 'fit-content',
                        width: width || '50%',
                        minWidth: 'fit-content',
                        display: 'flex',
                        position: 'absolute',
                        top: '50%',
                        left: '55%',
                        transform: 'translate(-50%, -50%)',
                        [theme.breakpoints.down('md')]: {
                            width: '90%',
                            left: '55%',
                            transform: 'translate(-50%, -50%)',
                            bgcolor: 'background.paper',
                        },
                        [theme.breakpoints.down('sm')]: {
                            width: '90%',
                            left: '50%',
                            transform: 'translate(-50%, -50%)',
                        },
                    })}
                >
                    {title && <CardHeader title={title} />}
                    {children}
                </Card>
            </Fade>
        </React.Fragment>
    );
}
