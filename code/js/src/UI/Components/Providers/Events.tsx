import { Message } from '../../../Domain/messages/Message';
import { MessageOutputModel } from '../../../Dto/output/messages/MessageOutputModel';
import { Identifier } from '../../../Domain/wrappers/identifier/Identifier';
import {
    ChannelInvitation,
    channelInvitationFromDto,
} from '../../../Domain/invitations/ChannelInvitation';
import { ChannelInvitationOutputModel } from '../../../Dto/output/invitations/ChannelInvitationOutputModel';
import { Channel, channelFromDto } from '../../../Domain/channel/Channel';
import { ChannelOutputModel } from '../../../Dto/output/channels/ChannelOutputModel';
import React, { createContext, useEffect, useRef } from 'react';
import { useSessionManager } from './Session';

export interface EventManager {
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

const EVENTS_URI = 'api/sse/listen';

const EventContext = createContext<EventManager>({
    setupEventSource: () => new Promise<void>(() => {}),
    destroyEventSource: () => {},
    handleEvent: () => {
        throw new Error('EventSource not initialized');
    },
    addListener: () => {},
    removeListener: () => {},
});

export const RECONNECT_DELAY = 5000;

export default function EventsProvider({
    children,
}: {
    children: React.ReactNode;
}) {
    const sessionManager = useSessionManager();

    const lastEventId: { current: string | null } = useRef(null);
    const eventSource = useRef<EventSource | null>(null);
    const eventListeners = useRef<EventListener[]>([]);

    useEffect(() => {
        console.log('Setting up EventSource');
        return () => {
            console.log('Destroying EventSource');
        };
    }, []);

    /**
     * Sets up the EventSource connection.
     *
     * If the EventSource is already open, it does nothing.
     *
     * If the EventSource is closed, it creates a new one.
     */
    const setupEventSource = async () => {
        await sessionManager.executeWithRefresh<void>(() => {
            console.log(lastEventId);

            if (
                eventSource.current !== null &&
                eventSource.current.readyState === EventSource.OPEN
            ) {
                console.warn('EventSource is already open');
            }

            const newEventSource = lastEventId
                ? new EventSource(
                      EVENTS_URI + '?lastEventId=' + lastEventId.current,
                      {
                          withCredentials: true,
                      },
                  )
                : new EventSource(EVENTS_URI, { withCredentials: true });

            eventSource.current = newEventSource;

            newEventSource.addEventListener('keep-alive', () => {
                console.log('Keep-alive received');
            });

            newEventSource.onerror = () => {
                newEventSource.close();
                eventSource.current = null;
                console.error(
                    'EventSource error. Reconnecting in 5 seconds...',
                );
                setTimeout(() => {
                    setupEventSource().then(() => {
                        console.log('Reconnected. Re-adding listeners...');
                        eventListeners.current.forEach((listener) =>
                            addListener(listener),
                        );
                    });
                }, RECONNECT_DELAY);
            };
        });
    };

    /**
     * Adds a listener to the EventSource
     *
     * @param listener The listener to add
     */
    const addListener = (listener: EventListener) => {
        eventListeners.current.push(listener);
        return eventSource.current?.addEventListener(
            listener.type.trim(),
            listener.listener,
        );
    };

    /**
     * Removes a listener from the EventSource
     *
     * @param listener The listener to remove
     */
    const removeListener = (listener: EventListener) => {
        eventListeners.current = eventListeners.current.filter(
            (l) => l !== listener,
        );
        return eventSource.current?.removeEventListener(
            listener.type.trim(),
            listener.listener,
        );
    };

    /**
     * Destroys the EventSource connection.
     *
     * If the EventSource is already closed, it does nothing.
     */
    const destroyEventSource = () => {
        eventSource.current?.close();
        eventSource.current = null;
    };

    /**
     * Handles an event from the EventSource
     *
     * @param event The event to handle
     *
     * @returns The event data
     */
    const handleEvent = (event: MessageEvent<any>): ServerEvent => {
        lastEventId.current = !lastEventId.current
            ? event.lastEventId
            : Math.max(
                  parseInt(lastEventId.current),
                  parseInt(event.lastEventId),
              ).toString();
        const data = JSON.parse(event.data);
        console.log('Received event: ', data);
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
                    data: channelInvitationFromDto(
                        data as ChannelInvitationOutputModel,
                    ),
                };
            case 'invitation-updated':
                return {
                    type: 'invitation-updated',
                    data: channelInvitationFromDto(
                        data as ChannelInvitationOutputModel,
                    ),
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
            default:
                console.error('Unknown event type: ', event.type);
        }
    };

    const eventManager = {
        setupEventSource,
        destroyEventSource,
        handleEvent,
        addListener,
        removeListener,
    };

    return (
        <EventContext.Provider value={eventManager}>
            {children}
        </EventContext.Provider>
    );
}

export function useEventManager() {
    const context = React.useContext(EventContext);
    if (context === undefined) {
        throw new Error(
            'useEventManagement must be used within a EventsProvider',
        );
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
    | ChannelDeletedEvent;

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
