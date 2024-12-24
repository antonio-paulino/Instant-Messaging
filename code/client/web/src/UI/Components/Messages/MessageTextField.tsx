import { Channel } from '../../../Domain/channel/Channel';
import Box from '@mui/material/Box';
import IconButton from '@mui/material/IconButton';
import { MessageValidator } from '../../../Domain/messages/MessageValidator';
import { FormState, useForm } from '../../State/useForm';
import { useSessionManager } from '../../Providers/SessionProvider';
import { useAlertContext } from '../../Providers/AlertsProvider';
import { MessageService } from '../../../Services/messages/MessageService';
import React from 'react';
import { Send } from '@mui/icons-material';
import { LoadingSpinner } from '../Utils/State/LoadingSpinner';
import { MessageInput } from './MessageInput';
import { messageKey } from './Message';

export function MessageTextField({ channel }: { channel: Channel }) {
    const { state, handleChange, handleSubmit } = useMessageTextField(channel);
    const user = useSessionManager().session.user;
    const canSend =
        user.id.value === channel.owner.id.value ||
        channel.members.some((member) => member.id.value === user.id.value && member.role === 'MEMBER');
    return (
        <Box
            position={'sticky'}
            width={'100%'}
            bottom={0}
            display="flex"
            alignItems="center"
            gap={2}
            padding={1.5}
            bgcolor="background.paper"
            component={'form'}
            onSubmit={handleSubmit}
        >
            <MessageInput
                formKey={messageKey}
                message={state.values[messageKey]}
                handleChange={handleChange}
                handleSubmit={handleSubmit}
                state={state}
                enabled={canSend}
                textFieldProps={{
                    sx: { width: '90%' },
                    placeholder: canSend ? 'Send a message...' : 'You are not allowed to send messages',
                }}
            />
            {state.type === 'loading' ? (
                <LoadingSpinner circularProgressProps={{ size: 20 }} />
            ) : (
                <IconButton
                    type={'submit'}
                    color="primary"
                    aria-label="send message"
                    disabled={state.values[messageKey].length === 0 || !canSend}
                >
                    <Send />
                </IconButton>
            )}
        </Box>
    );
}

interface MessageTextFieldHook {
    state: FormState;
    handleChange: (event: React.ChangeEvent<HTMLInputElement>) => void;
    handleSubmit: (event: React.FormEvent<HTMLFormElement>) => void;
}

function useMessageTextField(channel: Channel): MessageTextFieldHook {
    const sessionManager = useSessionManager();
    const { showAlert } = useAlertContext();

    const validator = new MessageValidator();

    const { state, handleChange, handleSubmit, reset } = useForm({
        initialValues: {
            [messageKey]: '',
        },
        validate: {
            [messageKey]: (value) => validator.validate(value).map((error) => error.toErrorMessage()),
        },
        onSubmit: async (values, signal) => {
            reset();
            const res = await sessionManager.executeWithRefresh(async () => {
                return await MessageService.createMessage(channel, values[messageKey], sessionManager.session, signal);
            });
            if (!signal.aborted && res.isFailure()) {
                showAlert({
                    message: 'Failed to send message',
                    severity: 'error',
                });
                return res.getLeft();
            }
        },
    });

    return { state, handleChange, handleSubmit };
}
