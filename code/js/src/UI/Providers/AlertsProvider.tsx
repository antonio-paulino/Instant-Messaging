import React, { createContext, useContext, useState, ReactNode, useRef, useEffect } from 'react';
import { Alert, Fade } from '@mui/material';
import { EventListener, useEventManager } from './EventsProvider';
import { Message } from '../../Domain/messages/Message';
import { Identifier } from '../../Domain/wrappers/identifier/Identifier';
import { useInfiniteScrollContextChannels } from './InfiniteScrollProvider';

export interface AlertMessage {
    message: string;
    severity: 'success' | 'error' | 'info' | 'warning';
}

export type NotificationCountsByIdentifier = Record<number, number>;
export type NotificationCounts = number;

interface AlertContextType {
    showAlert: (alert: AlertMessage) => void;
    channelNotifications: NotificationCountsByIdentifier;
    clearChannelNotifications: (id: Identifier) => void;
    invitationNotifications: NotificationCounts;
    clearInvitationNotifications: () => void;
}

const AlertContext = createContext<AlertContextType | undefined>(undefined);

export function AlertProvider({ children }: { children: ReactNode }) {
    const eventManager = useEventManager();
    const [alert, setAlert] = useState<AlertMessage | null>(null);
    const [show, setShow] = useState(false);
    const alertQueue = useRef<AlertMessage[]>([]);

    const [channelNotifications, setChannelNotifications] = useState<NotificationCountsByIdentifier>(
        JSON.parse(localStorage.getItem('channelNotifications') || '{}'),
    );

    const [invitationNotifications, setInvitationNotifications] = useState<NotificationCounts>(
        localStorage.getItem('invitationNotifications')
            ? parseInt(localStorage.getItem('invitationNotifications')!)
            : 0,
    );

    const { selectedChannel } = useInfiniteScrollContextChannels();

    const selectedChannelRef = useRef(selectedChannel); // To use inside the static event listeners

    useEffect(() => {
        selectedChannelRef.current = selectedChannel;
    }, [selectedChannel]);

    const listeners: EventListener[] = [
        {
            type: 'message-created',
            listener: (event: MessageEvent<string>) => {
                const message = eventManager.handleEvent(event).data as Message;
                setChannelNotifications((prev) => {
                    if (message.channelId.value === selectedChannelRef.current?.value) {
                        return prev;
                    }
                    const newNotifications = {
                        ...prev,
                        [message.channelId.value]: (prev[message.channelId.value] || 0) + 1,
                    };
                    localStorage.setItem('channelNotifications', JSON.stringify(newNotifications));
                    return newNotifications;
                });
            },
        },
        {
            type: 'invitation-created',
            listener: () => {
                setInvitationNotifications((prev) => {
                    localStorage.setItem('invitationNotifications', (prev + 1).toString());
                    return prev + 1;
                });
            },
        },
    ];

    useEffect(() => {
        if (eventManager.isOpen) {
            listeners.forEach(eventManager.addListener);
        }
        return () => {
            listeners.forEach(eventManager.removeListener);
        };
    }, [eventManager.isOpen]);

    const showAlert = (alert: AlertMessage) => {
        if (show) {
            alertQueue.current.push(alert);
            return;
        }
        setAlert(alert);
        setShow(true);

        const handleNextAlert = () => {
            setShow(false);
            setAlert(null);
            if (alertQueue.current.length) {
                const nextAlert = alertQueue.current.shift();
                if (nextAlert) {
                    showAlert(nextAlert);
                }
            }
        };

        setTimeout(handleNextAlert, 3000);
    };

    return (
        <AlertContext.Provider
            value={{
                showAlert,
                channelNotifications,
                clearChannelNotifications: (id: Identifier) => {
                    setChannelNotifications((prev) => {
                        const newNotifications = {
                            ...prev,
                            [id.value]: 0,
                        };
                        localStorage.setItem('channelNotifications', JSON.stringify(newNotifications));
                        return newNotifications;
                    });
                },
                invitationNotifications,
                clearInvitationNotifications: () => {
                    setInvitationNotifications(0);
                    localStorage.setItem('invitationNotifications', '0');
                },
            }}
        >
            {children}
            {alert && (
                <Fade in={show} timeout={500}>
                    <Alert
                        severity={alert.severity}
                        sx={{
                            position: 'fixed',
                            zIndex: 1200,
                            minWidth: { xs: '80%', sm: '50%' },
                            left: '50%',
                            top: '5%',
                            transform: 'translate(-50%, -50%)',
                        }}
                    >
                        {alert.message}
                    </Alert>
                </Fade>
            )}
        </AlertContext.Provider>
    );
}

export function useAlertContext() {
    const context = useContext(AlertContext);
    if (!context) {
        throw new Error('useAlert must be used within an AlertProvider');
    }
    return context;
}
