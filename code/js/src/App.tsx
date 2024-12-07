import SessionProvider, { useLoggedIn } from './UI/Providers/SessionProvider';
import { Navigate, Outlet, useLocation, useNavigate } from 'react-router-dom';
import AppTheme from './UI/Theme/AppTheme';
import { CssBaseline } from '@mui/material';
import * as React from 'react';
import { useEffect } from 'react';
import ColorModeSelect from './UI/Theme/ColorModeSelect';
import { styled } from '@mui/material/styles';
import Stack from '@mui/material/Stack';
import { Routes } from './routes';
import EventsProvider from './UI/Providers/EventsProvider';

const AppContainer = styled(Stack)(({ theme }) => ({
    height: 'calc((1 - var(--template-frame-height, 0)) * 100dvh)',
    minHeight: '100%',
    '&::before': {
        content: '""',
        display: 'block',
        position: 'absolute',
        zIndex: -1,
        inset: 0,
        backgroundImage: 'radial-gradient(ellipse at 50% 50%, hsl(210, 100%, 97%), hsl(0, 0%, 100%))',
        backgroundRepeat: 'no-repeat',
        ...theme.applyStyles('dark', {
            backgroundImage: 'radial-gradient(at 50% 50%, hsla(210, 100%, 16%, 0.5), hsl(220, 30%, 5%))',
        }),
    },
}));

export function App() {
    const navigate = useNavigate();
    const location = useLocation();

    useEffect(() => {
        if (location.pathname === '/') {
            navigate(Routes.HOME);
        }
    }, []);

    return (
        <div className={'App'}>
            <AppTheme>
                <CssBaseline />
                <ColorModeSelect
                    sx={{
                        position: 'fixed',
                        top: '0.5rem',
                        right: '1rem',
                        padding: 1,
                        zIndex: 9999,
                    }}
                />
                <AppContainer>
                    <SessionProvider>
                        <EventsProvider>
                            <Outlet />
                        </EventsProvider>
                    </SessionProvider>
                </AppContainer>
            </AppTheme>
        </div>
    );
}
