import React from 'react';
import { Alert, InputLabel, Switch } from '@mui/material';
import { Lock, LockOpen } from '@mui/icons-material';
import FormControl from '@mui/material/FormControl';
import Select from '@mui/material/Select';
import Stack from '@mui/material/Stack';
import MenuItem from '@mui/material/MenuItem';
import TextField from '@mui/material/TextField';
import Button from '@mui/material/Button';
import { NavigateFunction, useNavigate } from 'react-router-dom';
import { useForm } from '../../State/useForm';
import { NameValidator } from '../../../Domain/wrappers/name/NameValidator';
import ErrorList from '../../Components/Utils/State/ErrorList';
import { useInfiniteScrollContextChannels } from '../../Providers/InfiniteScrollProvider';
import { useSessionManager } from '../../Providers/SessionProvider';
import { ChannelService } from '../../../Services/channels/ChannelService';
import { Name } from '../../../Domain/wrappers/name/Name';
import { useAlertContext } from '../../Providers/AlertsProvider';
import { ChannelRole } from '../../../Domain/channel/ChannelRole';
import { LoadingSpinner } from '../../Components/Utils/State/LoadingSpinner';
import { Window } from '../../Components/Utils/Layouts/Window';
import { Routes } from '../../../routes';
import { RoleInput } from '../../Components/Utils/Input/RoleInput';

const channelNameKey = 'channelName';
const isPublicKey = 'isPublic';
const defaultRoleKey = 'defaultRole';

export function CreateChannelWindow() {
    const navigate = useNavigate();
    const { handleSubmit, handleChange, formState } = useCreateChannelWindow(navigate);

    return (
        <Window width={'50%'} onClose={() => navigate(Routes.HOME)}>
            <Stack direction={'column'} sx={{ gap: 4, padding: 4 }} component={'form'} onSubmit={handleSubmit}>
                <FormControl>
                    <TextField
                        name={channelNameKey}
                        label={'Channel Name'}
                        variant={'outlined'}
                        fullWidth
                        onChange={handleChange}
                        helperText={<ErrorList errors={formState.errors[channelNameKey]} />}
                        error={formState.errors[channelNameKey].length > 0}
                        color={formState.errors[channelNameKey].length > 0 ? 'error' : 'primary'}
                    />
                </FormControl>
                <Stack direction={'row'} sx={{ gap: 8, justifyContent: 'center', width: '100%' }}>
                    <FormControl>
                        <Switch
                            name={isPublicKey}
                            icon={<LockOpen />}
                            checkedIcon={<Lock />}
                            onChange={(e) =>
                                handleChange({
                                    target: {
                                        name: isPublicKey,
                                        // @ts-ignore
                                        value: !e.target.checked,
                                    },
                                })
                            }
                            value={formState.values[isPublicKey]}
                        />
                    </FormControl>
                    <FormControl>
                        <InputLabel htmlFor={defaultRoleKey}>Default Role</InputLabel>
                        <RoleInput
                            name={defaultRoleKey}
                            value={formState.values[defaultRoleKey]}
                            handleChange={handleChange}
                            error={formState.errors[defaultRoleKey].length > 0}
                        />
                    </FormControl>
                </Stack>
                {formState.error && <Alert severity={'error'}>{formState.error.detail}</Alert>}
                {formState.type === 'loading' ? (
                    <LoadingSpinner text={'Creating Channel'} />
                ) : (
                    <Button
                        type={'submit'}
                        variant={'contained'}
                        disabled={Object.values(formState.values).some((value) => value === '')}
                    >
                        Create
                    </Button>
                )}
            </Stack>
        </Window>
    );
}

interface CreateChannelWindowHook {
    formState: ReturnType<typeof useForm>['state'];
    handleChange: ReturnType<typeof useForm>['handleChange'];
    handleSubmit: ReturnType<typeof useForm>['handleSubmit'];
}

function useCreateChannelWindow(navigate: NavigateFunction): CreateChannelWindowHook {
    const sessionManager = useSessionManager();
    const { showAlert } = useAlertContext();
    const { state: scrollState, handleItemCreate } = useInfiniteScrollContextChannels();

    const nameValidator = new NameValidator();

    const {
        state: formState,
        handleChange,
        handleSubmit,
    } = useForm({
        initialValues: {
            [channelNameKey]: '',
            [isPublicKey]: true,
            [defaultRoleKey]: ChannelRole.MEMBER,
        },
        validate: {
            [channelNameKey]: (value) => nameValidator.validate(value).map((error) => error.toErrorMessage()),
        },
        onSubmit: async (values, signal) => {
            const channelName: string = values[channelNameKey];
            const isPublic: boolean = values[isPublicKey];
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
                navigate(Routes.HOME);
                return;
            } else {
                return res.getLeft();
            }
        },
    });
    return { formState, handleChange, handleSubmit };
}
