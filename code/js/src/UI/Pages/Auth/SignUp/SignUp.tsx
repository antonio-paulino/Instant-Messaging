import * as React from 'react';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import Divider from '@mui/material/Divider';
import FormControl from '@mui/material/FormControl';
import Link from '@mui/material/Link';
import TextField from '@mui/material/TextField';
import Typography from '@mui/material/Typography';
import ErrorList from '../../../Components/ErrorList';
import { useNavigate } from 'react-router-dom';
import { useForm } from '../../../Components/State/useForm';
import { EmailValidator } from '../../../../Domain/wrappers/email/EmailValidator';
import { NameValidator } from '../../../../Domain/wrappers/name/NameValidator';
import {
    checkPwned,
    PasswordValidator,
} from '../../../../Domain/wrappers/password/PasswordValidator';
import { Card } from '../SignIn/SignIn';
import { AuthService } from '../../../../Services/auth/AuthService';
import { Name } from '../../../../Domain/wrappers/name/Name';
import { Email } from '../../../../Domain/wrappers/email/Email';
import { Password } from '../../../../Domain/wrappers/password/Password';
import { ProblemResponse } from '../../../../Services/media/Problem';
import { useAbortSignal } from '../../../Components/State/useAbortSignal';
import { useSessionManager } from '../../../Components/Providers/Session';
import { Alert } from '@mui/material';
import { LoadingSpinner } from '../../../Components/LoadingSpinner';

export default function SignUp() {
    const navigate = useNavigate();

    const queryParams = new URLSearchParams(window.location.search);
    const token = queryParams.get('token') || '';

    const emailKey = 'email';
    const usernameKey = 'username';
    const passwordKey = 'password';
    const tokenKey = 'token';

    const emailValidator = new EmailValidator();
    const usernameValidator = new NameValidator();
    const passwordValidator = new PasswordValidator();
    const sessionManager = useSessionManager();
    const signal = useAbortSignal();

    function validateToken(token: string): string[] {
        const uuidRegex =
            /^[0-9a-f]{8}-[0-9a-f]{4}-[4][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;
        if (!uuidRegex.test(token)) {
            return ['Invalid token format.'];
        }
        return [];
    }

    const { state, handleChange, handleSubmit } = useForm({
        initialValues: {
            [emailKey]: '',
            [usernameKey]: '',
            [passwordKey]: '',
            [tokenKey]: token,
        },
        validate: {
            [emailKey]: (value) =>
                emailValidator
                    .validate(value)
                    .map((error) => error.toErrorMessage()),
            [usernameKey]: (value) =>
                usernameValidator
                    .validate(value)
                    .map((error) => error.toErrorMessage()),
            [passwordKey]: async (value) => {
                const errors = passwordValidator
                    .validate(value)
                    .map((error) => error.toErrorMessage());
                if (errors.length === 0) {
                    const pwned = await checkPwned(value);
                    if (pwned > 0) {
                        errors.push(
                            'This password has been found in ' +
                                pwned +
                                ' data breaches.',
                        );
                    }
                }
                return errors;
            },
            [tokenKey]: (value) => validateToken(value),
        },
        onSubmit: async (values) => {
            const name = new Name(values[usernameKey]);
            const email = new Email(values[emailKey]);
            const password = new Password(values[passwordKey]);
            const token = values[tokenKey];

            const result = await AuthService.register(
                name,
                email,
                password,
                token,
                signal,
            );

            let error: ProblemResponse | null = null;

            if (result.isSuccess()) {
                const login = await AuthService.login(
                    password.value,
                    name.value,
                    null,
                    signal,
                );
                if (login.isSuccess()) {
                    const session = login.getRight();
                    sessionManager.setSession(session);
                    return navigate('/');
                }
                error = login.getLeft();
            } else {
                error = result.getLeft();
            }

            return error;
        },
    });

    return (
        <Card variant="outlined">
            <Typography
                component="h1"
                variant="h4"
                sx={{ width: '100%', fontSize: 'clamp(2rem, 10vw, 2.15rem)' }}
            >
                Sign up
            </Typography>
            <Box
                component="form"
                onSubmit={handleSubmit}
                sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}
            >
                <FormControl>
                    <TextField
                        label="Username"
                        autoComplete="name"
                        name={usernameKey}
                        required
                        fullWidth
                        onChange={handleChange}
                        id={usernameKey}
                        placeholder="John Doe"
                        error={state.errors[usernameKey].length > 0}
                        helperText={
                            <ErrorList errors={state.errors[usernameKey]} />
                        }
                        color={
                            state.errors[usernameKey].length > 0
                                ? 'error'
                                : 'primary'
                        }
                    />
                </FormControl>
                <FormControl>
                    <TextField
                        label={'Email'}
                        required
                        fullWidth
                        id={emailKey}
                        placeholder="your@email.com"
                        onChange={handleChange}
                        name={emailKey}
                        autoComplete="email"
                        variant="outlined"
                        error={state.errors[emailKey].length > 0}
                        helperText={
                            <ErrorList errors={state.errors[emailKey]} />
                        }
                        color={
                            state.errors[emailKey].length > 0
                                ? 'error'
                                : 'primary'
                        }
                    />
                </FormControl>
                <FormControl>
                    <TextField
                        label={'Password'}
                        required
                        fullWidth
                        name="password"
                        placeholder="••••••"
                        type="password"
                        id="password"
                        onChange={handleChange}
                        autoComplete="new-password"
                        variant="outlined"
                        error={state.errors[passwordKey].length > 0}
                        helperText={
                            <ErrorList errors={state.errors[passwordKey]} />
                        }
                        color={
                            state.errors[passwordKey].length > 0
                                ? 'error'
                                : 'primary'
                        }
                    />
                </FormControl>
                <FormControl>
                    <TextField
                        label={'Invite token'}
                        required
                        fullWidth
                        name={tokenKey}
                        placeholder="00000000-0000-0000-0000-000000000000"
                        type="text"
                        id={tokenKey}
                        onChange={handleChange}
                        variant="outlined"
                        error={state.errors[tokenKey].length > 0}
                        helperText={
                            <ErrorList errors={state.errors[tokenKey]} />
                        }
                        color={
                            state.errors[tokenKey].length > 0
                                ? 'error'
                                : 'primary'
                        }
                    />
                </FormControl>
                {state.type === 'loading' ? (
                    <LoadingSpinner></LoadingSpinner>
                ) : (
                    <Button
                        type="submit"
                        fullWidth
                        variant="contained"
                        disabled={Object.values(state.errors).some(
                            (errors) =>
                                errors.length > 0 ||
                                Object.values(state.values).some(
                                    (value) => value === '',
                                ),
                        )}
                    >
                        Sign up
                    </Button>
                )}
            </Box>
            {state.error && (
                <Alert severity="error" sx={{ mb: 2 }}>
                    {state.error.detail}
                </Alert>
            )}
            <Divider>
                <Typography sx={{ color: 'text.secondary' }}>or</Typography>
            </Divider>
            <Typography sx={{ textAlign: 'center' }}>
                Already have an account?{' '}
                <Link
                    onClick={() => navigate('/sign-in')}
                    variant="body2"
                    sx={{ alignSelf: 'center' }}
                >
                    Sign in
                </Link>
            </Typography>
        </Card>
    );
}
