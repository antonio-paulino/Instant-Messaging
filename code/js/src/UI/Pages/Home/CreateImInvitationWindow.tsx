import { AuthService } from '../../../Services/auth/AuthService';
import Stack from '@mui/material/Stack';
import Button from '@mui/material/Button';
import React, { useState } from 'react';
import IconButton from '@mui/material/IconButton';
import { ContentCopy } from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { useAlertContext } from '../../Providers/AlertsProvider';
import { useSessionManager } from '../../Providers/SessionProvider';
import { FormState, useForm } from '../../State/useForm';
import { Window } from '../../Components/Utils/Layouts/Window';
import Typography from '@mui/material/Typography';
import { LoadingSpinner } from '../../Components/Utils/State/LoadingSpinner';
import { Routes } from '../../../routes';
import { ExpirationInput, expirationOptions } from '../../Components/Utils/Input/ExpirationInput';

const expirationKey = 'expiration';

export function CreateImInvitationWindow() {
    const navigate = useNavigate();
    const { token, handleCopy, handleChange, handleSubmit, formState } = useCreateImInvitationWindow();

    return (
        <Window onClose={() => navigate(Routes.HOME)} width={'fit-content'}>
            <Stack spacing={2} component="form" onSubmit={handleSubmit}>
                <ExpirationInput
                    id={expirationKey}
                    name={expirationKey}
                    value={formState.values[expirationKey]}
                    error={formState.errors[expirationKey].length > 0}
                    handleChange={handleChange}
                />
                {formState.type !== 'loading' ? (
                    <Button type="submit" variant="contained" color="primary" sx={{ p: 2 }}>
                        Create Invitation
                    </Button>
                ) : (
                    <LoadingSpinner />
                )}
                {token && (
                    <Stack direction={'row'} spacing={1} alignItems={'center'}>
                        <Typography
                            component="a"
                            href={window.location.origin + Routes.REFERRAL + '?' + Routes.TOKEN_PARAM + '=' + token}
                            sx={{
                                marginRight: 1,
                                textDecoration: 'none',
                                maxWidth: '100%',
                            }}
                        >
                            {window.location.origin + Routes.REFERRAL + '?' + Routes.TOKEN_PARAM + '=' + token}
                        </Typography>
                        <IconButton onClick={handleCopy}>
                            <ContentCopy />
                        </IconButton>
                    </Stack>
                )}
            </Stack>
        </Window>
    );
}

interface CreateImInvitationWindowHook {
    token: string | null;
    handleCopy: () => void;
    handleChange: (event: React.ChangeEvent<HTMLInputElement>) => void;
    handleSubmit: (event: React.FormEvent<HTMLFormElement>) => void;
    formState: FormState;
}

function useCreateImInvitationWindow(): CreateImInvitationWindowHook {
    const sessionManager = useSessionManager();
    const { showAlert } = useAlertContext();

    const [token, setToken] = useState<string | null>(null);

    const {
        state: formState,
        handleChange,
        handleSubmit,
    } = useForm({
        initialValues: {
            [expirationKey]: expirationOptions[0].label,
        },
        onSubmit: async (values, signal) => {
            const label: string = values[expirationKey];
            const expiration: Date = expirationOptions.find((option) => option.label === label)?.value;
            const res = await sessionManager.executeWithRefresh(async () => {
                return await AuthService.createInvitation(expiration, signal);
            });
            if (res.isSuccess()) {
                const invite = res.getRight();
                setToken(invite.token);
                showAlert({
                    message: 'Invitation created',
                    severity: 'success',
                });
                return;
            } else {
                if (signal.aborted) {
                    return;
                }
                showAlert({
                    message: 'Failed to create invitation',
                    severity: 'error',
                });
                return res.getLeft();
            }
        },
    });

    const handleCopy = () => {
        navigator.clipboard
            .writeText(window.location.origin + Routes.REFERRAL + '?' + Routes.TOKEN_PARAM + '=' + token)
            .then(() => {
                showAlert({
                    message: 'Token copied to clipboard',
                    severity: 'info',
                });
            });
    };

    return { token, handleCopy, handleChange, handleSubmit, formState };
}
