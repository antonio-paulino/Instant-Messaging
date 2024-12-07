import { AlertMessage, useAlertContext } from '../../../../Providers/AlertsProvider';
import { useSessionManager } from '../../../../Providers/SessionProvider';
import { useSearchField } from '../../../../State/useSearchField';
import { useInfiniteScroll } from '../../../../State/useInfiniteScroll';
import { User } from '../../../../../Domain/user/User';
import React, { useEffect, useMemo, useState } from 'react';
import { useForm } from '../../../../State/useForm';
import { ExpirationInput, expirationOptions } from '../../../../Components/Utils/Input/ExpirationInput';
import { InvitationService } from '../../../../../Services/invitations/InvitationService';
import { Navigate } from 'react-router-dom';
import { Routes } from '../../../../../routes';
import Stack from '@mui/material/Stack';
import FormControl from '@mui/material/FormControl';
import Button from '@mui/material/Button';
import Typography from '@mui/material/Typography';
import TextField from '@mui/material/TextField';
import { UserList } from '../../../../Components/Users/UserList';
import { Alert, InputLabel } from '@mui/material';
import { RoleInput } from '../../../../Components/Utils/Input/RoleInput';
import { Channel } from '../../../../../Domain/channel/Channel';
import { useSearchReset } from '../../../../../Utils/Hooks/search';
import { useChannel } from '../../../../../Utils/Hooks/channel';
import { useFetchUsersByName } from '../../../../../Utils/Hooks/fetchUsersByName';
import { ChannelRole } from '../../../../../Domain/channel/ChannelRole';
import { LoadingSpinner } from '../../../../Components/Utils/State/LoadingSpinner';

const expirationKey = 'expiration';
const roleKey = 'role';

export function InviteMember() {
    const {
        user,
        channel,
        handleSubmit,
        selectedUser,
        setSelectedUser,
        formState,
        userScrollState,
        items,
        handleChange,
        setValue,
        loadMore,
    } = useInviteMemberWindow();

    if (user.id.value !== channel?.owner.id.value) {
        return <Navigate to={Routes.CHANNEL_SETTINGS} />;
    }

    return (
        <Stack
            direction={'column'}
            sx={(theme) => ({
                padding: theme.spacing(2),
                gap: theme.spacing(2),
                width: '60%',
                justifySelf: 'center',
                alignSelf: 'center',
            })}
            component={'form'}
            onSubmit={handleSubmit}
        >
            <FormControl>
                {selectedUser ? (
                    <Button
                        sx={{
                            justifyContent: 'center',
                            alignItems: 'center',
                            width: '100%',
                        }}
                        onClick={() => {
                            setSelectedUser(null);
                            setValue('');
                        }}
                    >
                        <Typography variant={'body1'}>{selectedUser.name.value}</Typography>
                    </Button>
                ) : (
                    <React.Fragment>
                        <TextField
                            label={'Search User'}
                            placeholder={'Search for a user to invite...'}
                            name={'searchValue'}
                            onChange={(e) => setValue(e.target.value)}
                        />
                        <UserList
                            items={items}
                            loadMore={loadMore}
                            onClick={setSelectedUser}
                            isLoaded={userScrollState.type === 'loaded'}
                        />
                    </React.Fragment>
                )}
            </FormControl>
            <FormControl>
                <InputLabel htmlFor={roleKey}>Role</InputLabel>
                <RoleInput
                    id={roleKey}
                    name={roleKey}
                    value={formState.values[roleKey]}
                    handleChange={handleChange}
                    error={formState.errors[roleKey].length > 0}
                />
            </FormControl>
            <FormControl>
                <InputLabel htmlFor={expirationKey}>Expiration Time</InputLabel>
                <ExpirationInput
                    id={expirationKey}
                    name={expirationKey}
                    value={formState.values[expirationKey]}
                    handleChange={handleChange}
                    error={formState.errors[expirationKey].length > 0}
                />
            </FormControl>
            {formState.error && <Alert severity={'error'}>{formState.error.detail}</Alert>}
            {formState.type === 'loading' ? (
                <LoadingSpinner />
            ) : (
                <Button type="submit" variant="contained" color="primary" sx={{ p: 2 }} disabled={!selectedUser}>
                    Invite Member
                </Button>
            )}
        </Stack>
    );
}

function useInviteMemberWindow() {
    const sessionManager = useSessionManager();
    const { showAlert } = useAlertContext();
    const { channel } = useChannel();
    const { searchValue, setValue } = useSearchField(200);
    const fetchUsersByName = useFetchUsersByName(searchValue);

    const {
        state: userScrollState,
        loadMore,
        reset,
    } = useInfiniteScroll<User>({
        fetchItemsRequest: fetchUsersByName,
        limit: 10,
        getCount: false,
        useOffset: true,
    });

    useSearchReset(searchValue, reset);

    const [selectedUser, setSelectedUser] = useState<User | null>(null);

    const {
        state: formState,
        handleChange,
        handleSubmit,
    } = useForm({
        initialValues: {
            [expirationKey]: expirationOptions[0].label,
            [roleKey]: ChannelRole.MEMBER,
        },
        onSubmit: useFormSubmit(channel, selectedUser, showAlert),
    });

    const filteredUsers = useMemo(() => {
        return userScrollState.paginationState.items.filter(
            (user) => !channel.members.some((member) => member.id.value === user.id.value),
        );
    }, [userScrollState.paginationState.items, channel.members]);

    useEffect(() => {
        if (
            filteredUsers.length === 0 &&
            userScrollState.type === 'loaded' &&
            userScrollState.paginationState.info.next
        ) {
            loadMore();
        }
    }, [filteredUsers, userScrollState]);

    return {
        user: sessionManager.session.user,
        channel,
        handleSubmit,
        selectedUser,
        setSelectedUser,
        formState,
        userScrollState,
        items: filteredUsers,
        handleChange,
        setValue,
        loadMore,
    };
}

function useFormSubmit(channel: Channel, selectedUser: User | null, showAlert: (alert: AlertMessage) => void) {
    const sessionManager = useSessionManager();
    return async (values: any, signal: AbortSignal) => {
        const userRole = values[roleKey];
        const expiresAt = expirationOptions.find((option) => option.label === values[expirationKey])?.value;
        const res = await sessionManager.executeWithRefresh(async () => {
            return await InvitationService.createChannelInvitation(channel, selectedUser, expiresAt, userRole, signal);
        });
        if (res.isSuccess()) {
            showAlert({
                message: 'User invited to channel',
                severity: 'success',
            });
        } else {
            return res.getLeft();
        }
    };
}
