import React from 'react';
import { Message } from '../../../Domain/messages/Message';
import { styled } from '@mui/material/styles';
import Typography from '@mui/material/Typography';
import { Card, TextFieldProps, Tooltip } from '@mui/material';
import Stack from '@mui/material/Stack';
import { FormState, useForm } from '../../State/useForm';
import { useAbortSignal } from '../../State/useAbortSignal';
import { useSessionManager } from '../../Providers/SessionProvider';
import { MessageService } from '../../../Services/messages/MessageService';
import IconButton from '@mui/material/IconButton';
import { Close, Delete, Edit } from '@mui/icons-material';
import { Channel } from '../../../Domain/channel/Channel';
import { useAlertContext } from '../../Providers/AlertsProvider';
import { ConfirmActionWindow } from '../Utils/Actions/ConfirmActionWindow';
import { MessageValidator } from '../../../Domain/messages/MessageValidator';
import { LoadingSpinner } from '../Utils/State/LoadingSpinner';
import { MessageInput } from './MessageInput';
import { MessageContent } from './MessageContent';
import { User } from '../../../Domain/user/User';
import { ChannelRole } from '../../../Domain/channel/ChannelRole';

const StyledMessageBubble = styled(Card)(({ theme }) => ({
    display: 'flex',
    flexDirection: 'column',
    alignSelf: 'flex-start',
    height: 'fit-content',
    justifyContent: 'center',
    gap: '0',
    width: '20rem',
    minHeight: '5rem',
    [theme.breakpoints.up('sm')]: {
        width: '30rem',
    },
    padding: '0.75rem',
    borderRadius: '1rem',
    backgroundColor: theme.palette.background.paper,
    boxShadow: 'hsla(220, 30%, 5%, 0.05) 0px 5px 15px 0px, hsla(220, 25%, 10%, 0.05) 0px 15px 35px -5px',
}));

const Author = styled(Typography)(({ theme }) => ({
    fontSize: '1rem',
    fontWeight: 'bold',
    color: theme.palette.primary.main,
    marginBottom: '0.25rem',
    [theme.breakpoints.down('lg')]: {
        fontSize: '0.8rem',
    },
}));

const Timestamp = styled(Typography)(({ theme }) => ({
    alignSelf: 'flex-end',
    fontSize: '0.8rem',
    color: theme.palette.text.secondary,
    textAlign: 'right',
    [theme.breakpoints.down('lg')]: {
        fontSize: '0.6rem',
    },
}));

const TooltipIconButton = styled(IconButton)(() => ({
    border: 'none',
    borderRadius: '50%',
    width: '26px',
    height: '26px',
}));

export const messageKey = 'message';

export function MessageView({
    channel,
    message,
    showAuthor = true,
    isAuthor = false,
    spacing,
}: {
    channel: Channel;
    message: Message;
    showAuthor?: boolean;
    isAuthor?: boolean;
    spacing?: number;
}) {
    const { editMode, setEditMode, loading, handleChange, handleSubmit, state, deleteMessage, user } = useMessageView(
        channel,
        message,
    );
    const [deleteMode, setDeleteMode] = React.useState(false);
    const isGuest = channel.members.find((member) => member.id.value === user.id.value)?.role === ChannelRole.GUEST;
    return (
        <React.Fragment>
            <StyledMessageBubble
                sx={(theme) => ({
                    display: 'flex',
                    color: isAuthor ? 'background.paper' : 'text.primary',
                    alignSelf: isAuthor ? 'flex-end' : 'flex-start',
                    padding: 2,
                    marginTop: spacing ? `${spacing}rem` : '0',
                    [theme.breakpoints.down('lg')]: {
                        alignSelf: 'center',
                    },
                })}
            >
                {showAuthor && <Author>{message.author.name.value}</Author>}
                {editMode && !isGuest ? (
                    <MessageInput
                        formKey={messageKey}
                        message={message}
                        editMode={editMode}
                        setEditMode={setEditMode}
                        enabled={true}
                        setDeleteMode={setDeleteMode}
                        handleChange={handleChange}
                        handleSubmit={handleSubmit}
                        state={state}
                    />
                ) : (
                    <MessageContent content={message.content} />
                )}
                <Stack direction={'row'} justifyContent={'space-between'} spacing={1} width={'100%'}>
                    <Timestamp>
                        {' '}
                        {message.editedAt ? 'Edited ' : ''}
                        {message.editedAt
                            ? message.editedAt.toLocaleString('en-US', {
                                  hour: '2-digit',
                                  minute: '2-digit',
                              })
                            : message.createdAt.toLocaleString('en-US', {
                                  hour: '2-digit',
                                  minute: '2-digit',
                              })}
                    </Timestamp>
                    <Stack direction={'row'} spacing={1}>
                        {isAuthor &&
                            !isGuest &&
                            (editMode ? (
                                <Tooltip title={'Cancel edit'}>
                                    <TooltipIconButton onClick={() => setEditMode(false)}>
                                        <Close />
                                    </TooltipIconButton>
                                </Tooltip>
                            ) : (
                                <Tooltip title={'Edit message'}>
                                    <TooltipIconButton onClick={() => setEditMode(true)}>
                                        <Edit />
                                    </TooltipIconButton>
                                </Tooltip>
                            ))}
                        {deleteMode && (
                            <ConfirmActionWindow
                                message={'Are you sure you want to delete this message?'}
                                onConfirm={() => deleteMessage(message)}
                                onCancel={() => setDeleteMode(false)}
                            />
                        )}
                        {(isAuthor || user.id.value === channel.owner.id.value) &&
                            (loading ? (
                                <LoadingSpinner circularProgressProps={{ size: 24 }} />
                            ) : (
                                <Tooltip title={'Delete message'}>
                                    <TooltipIconButton onClick={() => setDeleteMode(true)} sx={{ color: 'error.main' }}>
                                        <Delete />
                                    </TooltipIconButton>
                                </Tooltip>
                            ))}
                    </Stack>
                </Stack>
            </StyledMessageBubble>
        </React.Fragment>
    );
}

interface MessageHook {
    handleChange: TextFieldProps['onChange'];
    handleSubmit: (event: React.FormEvent<HTMLFormElement>) => void;
    loading: boolean;
    state: FormState;
    deleteMessage: (message: Message) => void;
    user: User;
    editMode: boolean;
    setEditMode: (editMode: boolean) => void;
}

function useMessageView(channel: Channel, message: Message): MessageHook {
    const signal = useAbortSignal();
    const sessionManager = useSessionManager();
    const { showAlert } = useAlertContext();

    const [editMode, setEditMode] = React.useState(false);
    const [loading, setLoading] = React.useState(false);

    const messageKey = 'message';
    const messageValidator = new MessageValidator();

    async function deleteMessage(message: Message) {
        setLoading(true);
        const res = await sessionManager.executeWithRefresh(async () => {
            return await MessageService.deleteMessage(channel, message.id, signal);
        });

        if (signal.aborted) {
            return;
        }

        if (res.isFailure()) {
            showAlert({
                message: 'Failed to delete message',
                severity: 'error',
            });
        }

        setLoading(false);
    }

    const { state, handleChange, handleSubmit } = useForm({
        initialValues: {
            [messageKey]: message.content,
        },
        validate: {
            [messageKey]: (value) => messageValidator.validate(value).map((error) => error.toErrorMessage()),
        },
        onSubmit: async (values, signal) => {
            const content = values[messageKey];
            const res = await sessionManager.executeWithRefresh(async () => {
                return await MessageService.updateMessage(message, content, signal);
            });

            if (signal.aborted) {
                return;
            }

            if (res.isSuccess()) {
                setEditMode(false);
            } else {
                showAlert({
                    message: 'Failed to update message',
                    severity: 'error',
                });
                return res.getLeft();
            }
        },
    });

    return {
        loading,
        handleChange,
        handleSubmit,
        state,
        deleteMessage,
        user: sessionManager.session.user,
        editMode,
        setEditMode,
    };
}
