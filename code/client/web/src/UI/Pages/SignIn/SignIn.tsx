import * as React from 'react';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import Divider from '@mui/material/Divider';
import FormLabel from '@mui/material/FormLabel';
import FormControl from '@mui/material/FormControl';
import Link from '@mui/material/Link';
import TextField from '@mui/material/TextField';
import Typography from '@mui/material/Typography';
import MuiCard from '@mui/material/Card';
import { styled } from '@mui/material/styles';
import { FormState, useForm } from '../../State/useForm';
import { NavigateFunction, useLocation, useNavigate } from 'react-router-dom';
import { Alert } from '@mui/material';
import { AuthService } from '../../../Services/auth/AuthService';
import { SessionManager, useLoggedIn, useSessionManager } from '../../Providers/SessionProvider';
import { LoadingSpinner } from '../../Components/Utils/State/LoadingSpinner';
import { Routes } from '../../../routes';
import { useEffect } from 'react';

export const Card = styled(MuiCard)(({ theme }) => ({
    display: 'flex',
    flexDirection: 'column',
    alignSelf: 'center',
    width: '100%',
    padding: theme.spacing(4),
    gap: theme.spacing(2),
    margin: 'auto',
    backgroundColor: theme.palette.background.paper,
    [theme.breakpoints.up('sm')]: {
        width: '40rem',
        backgroundColor: theme.palette.background.paper,
    },
    boxShadow: 'hsla(220, 30%, 5%, 0.05) 0px 5px 15px 0px, hsla(220, 25%, 10%, 0.05) 0px 15px 35px -5px',
    ...theme.applyStyles('dark', {
        boxShadow: 'hsla(220, 30%, 5%, 0.5) 0px 5px 15px 0px, hsla(220, 25%, 10%, 0.08) 0px 15px 35px -5px',
    }),
}));

const usernameOrEmailKey = 'usernameOrEmail';
const passwordKey = 'password';

export default function SignIn() {
    const navigate = useNavigate();
    const { state, handleChange, handleSubmit } = useSignIn(navigate);

    return (
        <Card variant="outlined">
            <Typography
                component="h1"
                variant="h4"
                sx={{
                    width: '100%',
                    fontSize: 'clamp(2rem, 10vw, 2.15rem)',
                    marginBottom: '0.5rem',
                }}
            >
                Sign in
            </Typography>
            <Box
                component="form"
                onSubmit={handleSubmit}
                noValidate
                sx={{
                    display: 'flex',
                    flexDirection: 'column',
                    width: '100%',
                    gap: 2,
                }}
            >
                <FormControl>
                    <TextField
                        sx={{ marginBottom: '0.5rem' }}
                        label="Username or Email"
                        id={usernameOrEmailKey}
                        type="text"
                        name={usernameOrEmailKey}
                        placeholder=""
                        onChange={handleChange}
                        autoFocus
                        required
                        fullWidth
                        variant="outlined"
                    />
                </FormControl>
                <FormControl>
                    <FormLabel htmlFor="password"></FormLabel>
                    <TextField
                        sx={{ marginBottom: '0.5rem' }}
                        label="Password"
                        name={passwordKey}
                        placeholder="••••••"
                        type="password"
                        id={passwordKey}
                        autoComplete="current-password"
                        onChange={handleChange}
                        required
                        fullWidth
                        variant="outlined"
                    />
                </FormControl>
                {state.type == 'loading' ? (
                    <LoadingSpinner text="Logging in" />
                ) : (
                    <Button
                        sx={{ marginTop: 2 }}
                        type="submit"
                        fullWidth
                        variant="contained"
                        disabled={Object.values(state.values).some((value) => value === '')}
                    >
                        Sign in
                    </Button>
                )}
                {state.error && (
                    <Alert severity="error" sx={{ mb: 2 }}>
                        {state.error.detail}
                    </Alert>
                )}
            </Box>
            <Divider>or</Divider>
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                <Typography sx={{ textAlign: 'center' }}>
                    Don't have an account?
                    <Link onClick={() => navigate(Routes.SIGN_UP)} variant="body2" sx={{ alignSelf: 'center' }}>
                        Sign up
                    </Link>
                </Typography>
            </Box>
        </Card>
    );
}

interface SignInHook {
    state: FormState;
    handleChange: (event: React.ChangeEvent<HTMLInputElement>) => void;
    handleSubmit: (event: React.FormEvent<HTMLFormElement>) => void;
}

function useSignIn(navigate: NavigateFunction): SignInHook {
    const location = useLocation();
    const sessionManager = useSessionManager();
    const loggedIn = useLoggedIn();

    useEffect(() => {
        if (loggedIn) {
            navigate(location.state ? location.state.from : Routes.HOME);
        }
    }, [loggedIn]);

    const onSubmit = useFormSubmit(sessionManager);

    const { state, handleChange, handleSubmit } = useForm({
        initialValues: {
            [usernameOrEmailKey]: '',
            [passwordKey]: '',
        },
        onSubmit: onSubmit,
    });

    return { state, handleChange, handleSubmit };
}

function useFormSubmit(sessionManager: SessionManager) {
    return async (values: any, signal: AbortSignal) => {
        const usernameOrEmail = values[usernameOrEmailKey];
        const password = values[passwordKey];

        const login = usernameOrEmail.includes('@')
            ? await AuthService.login(password, null, usernameOrEmail, signal)
            : await AuthService.login(password, usernameOrEmail, null, signal);

        if (login.isSuccess()) {
            sessionManager.setSession(login.getRight());
            return;
        }

        let error = login.getLeft();

        if (error.status === 400 || error.status === 401) {
            error = {
                ...error,
                detail: 'Invalid credentials. Please try again.',
            };
        }

        return error;
    };
}
