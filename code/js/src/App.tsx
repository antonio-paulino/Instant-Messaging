import SessionProvider, { isLoggedIn } from './UI/Components/Providers/Session';
import { Navigate, Outlet, useLocation, useNavigate } from 'react-router-dom';
import AppTheme from './UI/Theme/AppTheme';
import { CssBaseline } from '@mui/material';
import * as React from 'react';
import { useEffect } from 'react';
import ColorModeSelect from './UI/Theme/ColorModeSelect';
import { styled } from '@mui/material/styles';
import Stack from '@mui/material/Stack';
import EventsProvider, {
    useEventManager,
} from './UI/Components/Providers/Events';
import { AlertProvider } from './UI/Components/Providers/Alerts';

const AppContainer = styled(Stack)(({ theme }) => ({
    height: 'calc((1 - var(--template-frame-height, 0)) * 100dvh)',
    minHeight: '100%',
    padding: theme.spacing(2),
    [theme.breakpoints.up('sm')]: {
        padding: theme.spacing(4),
    },
    '&::before': {
        content: '""',
        display: 'block',
        position: 'absolute',
        zIndex: -1,
        inset: 0,
        backgroundImage:
            'radial-gradient(ellipse at 50% 50%, hsl(210, 100%, 97%), hsl(0, 0%, 100%))',
        backgroundRepeat: 'no-repeat',
        ...theme.applyStyles('dark', {
            backgroundImage:
                'radial-gradient(at 50% 50%, hsla(210, 100%, 16%, 0.5), hsl(220, 30%, 5%))',
        }),
    },
}));

export function AuthenticatedPage({ children }: { children: React.ReactNode }) {
    const login = isLoggedIn();
    const location = useLocation();

    if (!login) {
        return <Navigate to={'/sign-in'} state={{ from: location.pathname }} />;
    }

    return <>{children}</>;
}

export function App() {
    const login = isLoggedIn();
    const eventManager = useEventManager();
    const navigate = useNavigate();

    useEffect(() => {
        if (login) {
            eventManager.setupEventSource();
        }
        return () => {
            eventManager.destroyEventSource();
        };
    }, [login, eventManager]);

    useEffect(() => {
        navigate('/home');
    }, []);

    return (
        <div className={'App'}>
            <AppTheme>
                <CssBaseline />
                <ColorModeSelect
                    sx={{ position: 'fixed', top: '1rem', right: '1rem' }}
                />
                <AppContainer>
                    <AlertProvider>
                        <SessionProvider>
                            <EventsProvider>
                                <Outlet />
                            </EventsProvider>
                        </SessionProvider>
                    </AlertProvider>
                </AppContainer>
            </AppTheme>
        </div>
    );
}
