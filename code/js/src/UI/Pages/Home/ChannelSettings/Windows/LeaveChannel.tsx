import { useNavigate } from 'react-router-dom';
import { useAlertContext } from '../../../../Providers/AlertsProvider';
import { useInfiniteScrollContextChannels } from '../../../../Providers/InfiniteScrollProvider';
import { useSessionManager } from '../../../../Providers/SessionProvider';
import { ChannelService } from '../../../../../Services/channels/ChannelService';
import { Routes } from '../../../../../routes';
import { ConfirmActionWindow } from '../../../../Components/Utils/Actions/ConfirmActionWindow';
import React from 'react';

export function LeaveChannel() {
    const navigate = useNavigate();
    const { showAlert } = useAlertContext();
    const { state, selectedChannel, handleItemDelete, setSelectedChannel } = useInfiniteScrollContextChannels();
    const sessionManager = useSessionManager();
    const channel = state.paginationState.items.find((channel) => channel.id.value === selectedChannel?.value);

    const handleConfirm = async () => {
        const res = await sessionManager.executeWithRefresh(async () => {
            return await ChannelService.removeUserFromChannel(channel, sessionManager.session.user.id);
        });

        if (res.isSuccess()) {
            showAlert({
                message: `You have left ${channel.name}`,
                severity: 'success',
            });
            handleItemDelete(channel.id);
            setSelectedChannel(null);
            navigate(Routes.HOME);
        } else {
            showAlert({
                message: res.getLeft()?.detail || `Failed to leave ${channel.name}`,
                severity: 'error',
            });
        }
    };

    const handleCancel = () => {
        navigate(Routes.CHANNEL_SETTINGS);
    };

    return (
        <ConfirmActionWindow
            message={`Are you sure you want to leave the channel "${channel.name}"?`}
            onConfirm={handleConfirm}
            onCancel={handleCancel}
        />
    );
}
