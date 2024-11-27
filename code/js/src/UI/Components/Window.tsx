import React from 'react';
import { CardHeader, Fade } from '@mui/material';
import { Card } from '../Pages/Auth/SignIn/SignIn';
import IconButton from '@mui/material/IconButton';
import { Close } from '@mui/icons-material';

export function Window({
    title,
    onClose,
    children,
}: {
    title: string;
    onClose: () => void;
    children: React.ReactNode;
}) {
    return (
        <Fade in={true} timeout={500}>
            <Card
                variant="outlined"
                style={{
                    width: '50%',
                    position: 'absolute',
                    top: '50%',
                    left: '50%',
                    transform: 'translate(-50%, -50%)',
                }}
            >
                <CardHeader
                    title={title}
                    action={
                        <IconButton
                            onClick={onClose}
                            style={{ alignSelf: 'end' }}
                        >
                            <Close />
                        </IconButton>
                    }
                />
                {children}
            </Card>
        </Fade>
    );
}
