import { useAlertContext } from '../../../../Providers/AlertsProvider';
import { useInfiniteScrollContextChannels } from '../../../../Providers/InfiniteScrollProvider';
import { useSessionManager } from '../../../../Providers/SessionProvider';
import { NameValidator } from '../../../../../Domain/wrappers/name/NameValidator';
import { useForm } from '../../../../State/useForm';
import { ChannelRole } from '../../../../../Domain/channel/ChannelRole';
import { ChannelService } from '../../../../../Services/channels/ChannelService';
import { Name } from '../../../../../Domain/wrappers/name/Name';
import Stack from '@mui/material/Stack';
import TextField from '@mui/material/TextField';
import ErrorList from '../../../../Components/Utils/State/ErrorList';
import FormControl from '@mui/material/FormControl';
import { Alert, InputLabel, SelectChangeEvent, Switch } from '@mui/material';
import Select from '@mui/material/Select';
import MenuItem from '@mui/material/MenuItem';
import { Lock, LockOpen } from '@mui/icons-material';
import { LoadingSpinner } from '../../../../Components/Utils/State/LoadingSpinner';
import Button from '@mui/material/Button';
import React from 'react';

export function EditChannel() {
    const { showAlert } = useAlertContext();
    const { state, selectedChannel } = useInfiniteScrollContextChannels();
    const sessionManager = useSessionManager();

    const channel = state.paginationState.items.find((channel) => channel.id.value === selectedChannel?.value);
    const nameValidator = new NameValidator();

    const {
        state: formState,
        handleChange,
        handleSubmit,
    } = useForm({
        initialValues: {
            channelName: channel.name.toString(),
            isPublic: true,
            defaultRole: channel.defaultRole || ChannelRole.MEMBER,
        },
        validate: {
            channelName: (value) => nameValidator.validate(value).map((error) => error.toErrorMessage()),
        },
        onSubmit: async (values) => {
            const res = await sessionManager.executeWithRefresh(() =>
                ChannelService.updateChannel(
                    channel.id,
                    new Name(values.channelName),
                    values.defaultRole,
                    values.isPublic,
                ),
            );
            if (res.isSuccess()) {
                showAlert({
                    message: 'Channel updated successfully',
                    severity: 'success',
                });
            } else {
                return res.getLeft();
            }
        },
    });

    const isFormComplete =
        formState.values.channelName.trim() && formState.values.defaultRole && formState.values.isPublic !== undefined;

    return (
        <Stack
            direction={'column'}
            justifyContent={'center'}
            alignItems={'center'}
            component={'form'}
            spacing={2}
            width={'50%'}
            onSubmit={handleSubmit}
        >
            <TextField
                label="Channel Name"
                name="channelName"
                value={formState.values.channelName}
                onChange={handleChange}
                fullWidth
                margin="normal"
                error={formState.errors.channelName.length > 0}
                helperText={<ErrorList errors={formState.errors.channelName} />}
            />
            <FormControl fullWidth margin="normal">
                <InputLabel>Default Role</InputLabel>
                <Select
                    name="defaultRole"
                    value={formState.values.defaultRole}
                    onChange={(e: SelectChangeEvent<ChannelRole>) => handleChange(e as any)}
                >
                    <MenuItem value={ChannelRole.MEMBER}>MEMBER</MenuItem>
                    <MenuItem value={ChannelRole.GUEST}>GUEST</MenuItem>
                </Select>
            </FormControl>
            <FormControl>
                <Switch
                    name={'isPublic'}
                    icon={<LockOpen />}
                    checkedIcon={<Lock />}
                    onChange={(e) =>
                        handleChange({
                            target: {
                                name: 'isPublic',
                                // @ts-ignore
                                value: !e.target.checked,
                            },
                        })
                    }
                    value={formState.values.isPublic}
                />
            </FormControl>
            {formState.error && <Alert severity={'error'}>{formState.error.detail}</Alert>}
            {formState.type === 'loading' ? (
                <LoadingSpinner />
            ) : (
                <Button
                    type="submit"
                    variant="contained"
                    color="primary"
                    disabled={!isFormComplete || formState.type === 'error'}
                >
                    Update Channel
                </Button>
            )}
        </Stack>
    );
}
