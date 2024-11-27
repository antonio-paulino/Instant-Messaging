import React from 'react';
import {
    Alert,
    InputLabel,
    Switch,
} from '@mui/material';
import { Lock, LockOpen } from '@mui/icons-material';
import FormControl from '@mui/material/FormControl';
import Select from '@mui/material/Select';
import Stack from '@mui/material/Stack';
import MenuItem from '@mui/material/MenuItem';
import TextField from '@mui/material/TextField';
import Button from '@mui/material/Button';
import { useNavigate } from 'react-router-dom';
import { useForm } from '../../Components/State/useForm';
import { NameValidator } from '../../../Domain/wrappers/name/NameValidator';
import ErrorList from '../../Components/ErrorList';
import { useInfiniteScrollContextChannels } from '../../Components/Providers/InfiniteScrollProvider';
import { useSessionManager } from '../../Components/Providers/Session';
import { ChannelService } from '../../../Services/channels/ChannelService';
import { Name } from '../../../Domain/wrappers/name/Name';
import { useAbortSignal } from '../../Components/State/useAbortSignal';
import { useAlert } from '../../Components/Providers/Alerts';
import { ChannelRole } from '../../../Domain/channel/ChannelRole';
import { LoadingSpinner } from '../../Components/LoadingSpinner';
import { Window } from '../../Components/Window';

export function CreateChannelWindow() {
    const { state: scrollState, handleItemCreate } =
        useInfiniteScrollContextChannels();
    const sessionManager = useSessionManager();
    const signal = useAbortSignal();
    const { showAlert } = useAlert();
    const navigate = useNavigate();

    const nameValidator = new NameValidator();
    const channelNameKey = 'channelName';
    const isPublicKey = 'isPublic';
    const defaultRoleKey = 'defaultRole';

    const {
        state: formState,
        handleChange,
        handleSubmit,
    } = useForm({
        initialValues: {
            [channelNameKey]: '',
            [isPublicKey]: false,
            [defaultRoleKey]: 'MEMBER',
        },
        validate: {
            [channelNameKey]: (value) =>
                nameValidator
                    .validate(value)
                    .map((error) => error.toErrorMessage()),
        },
        onSubmit: async (values) => {
            const channelName: string = values[channelNameKey];
            const isPublic: boolean = values[isPublicKey] === 'on';
            const defaultRole: ChannelRole = values[defaultRoleKey];
            const res = await sessionManager.executeWithRefresh(async () => {
                return await ChannelService.createChannel(
                    new Name(channelName),
                    defaultRole,
                    isPublic,
                    sessionManager.session,
                    signal,
                );
            });
            if (res.isSuccess()) {
                showAlert({ message: 'Channel created', severity: 'success' });
                if (!scrollState.paginationState.info.next) {
                    handleItemCreate(res.getRight());
                }
                navigate('/home');
                return;
            } else {
                return res.getLeft();
            }
        },
    });

    return (
        <Window title={'Create Channel'} onClose={() => navigate('/home')}>
            <Stack
                direction={'column'}
                sx={{ gap: 4, padding: 4 }}
                component={'form'}
                onSubmit={handleSubmit}
            >
                <FormControl>
                    <TextField
                        name={channelNameKey}
                        label={'Channel Name'}
                        variant={'outlined'}
                        fullWidth
                        onChange={handleChange}
                        helperText={
                            <ErrorList
                                errors={formState.errors[channelNameKey]}
                            />
                        }
                        error={formState.errors[channelNameKey].length > 0}
                        color={
                            formState.errors[channelNameKey].length > 0
                                ? 'error'
                                : 'primary'
                        }
                    />
                </FormControl>
                <Stack
                    direction={'row'}
                    sx={{ gap: 8, justifyContent: 'center', width: '100%' }}
                >
                    <FormControl>
                        <Switch
                            name={isPublicKey}
                            icon={<LockOpen />}
                            checkedIcon={<Lock />}
                            onChange={handleChange}
                        />
                    </FormControl>
                    <FormControl>
                        <InputLabel htmlFor={defaultRoleKey}>
                            Default Role
                        </InputLabel>
                        <Select
                            id={defaultRoleKey}
                            name={defaultRoleKey}
                            variant={'outlined'}
                            sx={{ width: '200px' }}
                            onChange={handleChange}
                            value={formState.values[defaultRoleKey]} // Ensure the value is controlled
                        >
                            <MenuItem value={'MEMBER'}>Member</MenuItem>
                            <MenuItem value={'GUEST'}>Guest</MenuItem>
                        </Select>
                    </FormControl>
                </Stack>
                {formState.error && (
                    <Alert severity={'error'}>{formState.error.detail}</Alert>
                )}
                {formState.type === 'loading' ? (
                    <LoadingSpinner text={'Creating Channel'} />
                ) : (
                    <Button
                        type={'submit'}
                        variant={'contained'}
                        disabled={Object.values(formState.values).some(
                            (value) => value === '',
                        )}
                    >
                        Create
                    </Button>
                )}
            </Stack>
        </Window>
    );
}
