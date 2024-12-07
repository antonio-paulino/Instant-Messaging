import { User } from '../../../Domain/user/User';
import Stack from '@mui/material/Stack';
import Typography from '@mui/material/Typography';
import React from 'react';

export function UserView({ user }: { user: User }) {
    return (
        <Stack direction={'row'} width={'100%'} justifyContent={'center'} alignItems={'center'}>
            <Typography variant={'body1'} textAlign={'center'}>
                {user.name.value}
            </Typography>
        </Stack>
    );
}
