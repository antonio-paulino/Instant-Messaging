import { Message } from '../../Domain/messages/Message';
import { MessageOutputModel } from '../../Dto/output/messages/MessageOutputModel';
import { Identifier } from '../../Domain/wrappers/identifier/Identifier';
import { ChannelInvitation, channelInvitationFromDto } from '../../Domain/invitations/ChannelInvitation';
import { ChannelInvitationOutputModel } from '../../Dto/output/invitations/ChannelInvitationOutputModel';
import { Channel, channelFromDto } from '../../Domain/channel/Channel';
import { ChannelOutputModel } from '../../Dto/output/channels/ChannelOutputModel';
import React, { createContext, useState } from 'react';
import { useSessionManager } from './SessionProvider';
import { doAfterDelay } from '../../Utils/Time';

export interface EventManager {
    readonly isInitialized: boolean;
    readonly setupEventSource: () => void;
    readonly destroyEventSource: () => void;
    readonly handleEvent: (event: MessageEvent<any>) => ServerEvent;
    readonly addListener: (listener: EventListener) => void;
    readonly removeListener: (listener: EventListener) => void;
}

export type EventListener = {
    readonly type: ServerEvents[keyof ServerEvents];
    readonly listener: (event: MessageEvent<string>) => void;
};

const EVENTS_URI = '/api/sse/listen';

const EventContext = createContext<EventManager>({
    isInitialized: false,
    setupEventSource: () =>
        new Promise<void>(() => {
            throw new Error('Not implemented');
        }),
    destroyEventSource: () => {
        throw new Error('Not implemented');
    },
    handleEvent: () => {
        throw new Error('Not implemented');
    },
    addListener: () => {
        throw new Error('Not implemented');
    },
    removeListener: () => {
        throw new Error('Not implemented');
    },
});

export default function EventsProvider({ children }: { children: React.ReactNode }) {
    const sessionManager = useSessionManager();
    const [isInitialized, setIsInitialized] = useState(false);
    const [eventSource, setEventSource] = useState<EventSource | null>(null);

    const setupEventSource = async () => {
        if (eventSource !== null && eventSource.readyState === EventSource.OPEN) {
            return;
        }
        await sessionManager.executeWithRefresh<void>(() => {
            const newEventSource = new EventSource(EVENTS_URI, {
                withCredentials: true,
            });
            setEventSource(newEventSource);
            newEventSource.onopen = () => {
                setIsInitialized(true);
            };
            newEventSource.onerror = () => {
                if (newEventSource.readyState === EventSource.CLOSED) {
                    setIsInitialized(false);
                    doAfterDelay(2000, setupEventSource);
                }
            };
        });
    };

    /**
     * Adds a listener to the EventSource
     *
     * @param listener The listener to add
     */
    const addListener = (listener: EventListener) => {
        if (eventSource !== null) {
            return eventSource.addEventListener(listener.type.trim(), listener.listener);
        }
    };

    /**
     * Removes a listener from the EventSource
     *
     * @param listener The listener to remove
     */
    const removeListener = (listener: EventListener) => {
        if (eventSource !== null) {
            return eventSource.removeEventListener(listener.type.trim(), listener.listener);
        }
    };

    /**
     * Destroys the EventSource connection.
     *
     * If the EventSource is already closed, it does nothing.
     */
    const destroyEventSource = () => {
        if (eventSource !== null) {
            eventSource.close();
            setEventSource(null);
            setIsInitialized(false);
        }
    };

    /**
     * Handles an event from the EventSource
     *
     * @param event The event to handle
     *
     * @returns The event data
     */
    const handleEvent = (event: MessageEvent<any>): ServerEvent => {
        const data = JSON.parse(event.data);
        switch (event.type) {
            case 'message-created':
                return {
                    type: 'message-created',
                    data: Message.fromDto(data as MessageOutputModel),
                };
            case 'message-updated':
                return {
                    type: 'message-updated',
                    data: Message.fromDto(data as MessageOutputModel),
                };
            case 'message-deleted':
                return {
                    type: 'message-deleted',
                    data: Identifier.fromDto(data as IdentifierOutputModel),
                };
            case 'invitation-created':
                return {
                    type: 'invitation-created',
                    data: channelInvitationFromDto(data as ChannelInvitationOutputModel),
                };
            case 'invitation-updated':
                return {
                    type: 'invitation-updated',
                    data: channelInvitationFromDto(data as ChannelInvitationOutputModel),
                };
            case 'invitation-deleted':
                return {
                    type: 'invitation-deleted',
                    data: Identifier.fromDto(data as IdentifierOutputModel),
                };
            case 'channel-updated':
                return {
                    type: 'channel-updated',
                    data: channelFromDto(data as ChannelOutputModel),
                };
            case 'channel-deleted':
                return {
                    type: 'channel-deleted',
                    data: Identifier.fromDto(data as IdentifierOutputModel),
                };
            case 'channel-created':
                return {
                    type: 'channel-created',
                    data: channelFromDto(data as ChannelOutputModel),
                };
            default:
                console.error('Unknown event type: ', event.type);
        }
    };

    const eventManager = {
        isInitialized,
        setupEventSource,
        destroyEventSource,
        handleEvent,
        addListener,
        removeListener,
    };

    return <EventContext.Provider value={eventManager}>{children}</EventContext.Provider>;
}

export function useEventManager() {
    const context = React.useContext(EventContext);
    if (context === undefined) {
        throw new Error('useEventManagement must be used within a EventsProvider');
    }
    return context;
}

export type ServerEvents = {
    messageCreated: 'message-created';
    messageUpdated: 'message-updated';
    messageDeleted: 'message-deleted';
    invitationCreated: 'invitation-created';
    invitationUpdated: 'invitation-updated';
    invitationDeleted: 'invitation-deleted';
    channelUpdated: 'channel-updated';
    channelDeleted: 'channel-deleted';
    channelCreated: 'channel-created';
};

export interface IdentifierOutputModel {
    id: number;
}

export type ServerEvent =
    | MessageCreatedEvent
    | MessageUpdatedEvent
    | MessageDeletedEvent
    | InvitationCreatedEvent
    | InvitationUpdatedEvent
    | InvitationDeletedEvent
    | ChannelUpdatedEvent
    | ChannelDeletedEvent
    | ChannelCreatedEvent;

export type MessageCreatedEvent = {
    type: 'message-created';
    data: Message;
};

export type MessageUpdatedEvent = {
    type: 'message-updated';
    data: Message;
};

export type MessageDeletedEvent = {
    type: 'message-deleted';
    data: Identifier;
};

export type InvitationCreatedEvent = {
    type: 'invitation-created';
    data: ChannelInvitation;
};

export type InvitationUpdatedEvent = {
    type: 'invitation-updated';
    data: ChannelInvitation;
};

export type InvitationDeletedEvent = {
    type: 'invitation-deleted';
    data: Identifier;
};

export type ChannelUpdatedEvent = {
    type: 'channel-updated';
    data: Channel;
};

export type ChannelDeletedEvent = {
    type: 'channel-deleted';
    data: Identifier;
};

export type ChannelCreatedEvent = {
    type: 'channel-created';
    data: Channel;
}
