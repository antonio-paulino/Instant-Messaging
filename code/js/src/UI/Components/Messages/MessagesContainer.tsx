import { Channel } from '../../../Domain/channel/Channel';
import { Scrollable } from '../Utils/Layouts/Scrollable';
import { InfiniteScrollState, useInfiniteScroll } from '../../State/useInfiniteScroll';
import { PaginationRequest } from '../../../Domain/pagination/PaginationRequest';
import { Message } from '../../../Domain/messages/Message';
import { ApiResult } from '../../../Services/media/Problem';
import { Pagination } from '../../../Domain/pagination/Pagination';
import { useSessionManager } from '../../Providers/SessionProvider';
import { useEventManager } from '../../Providers/EventsProvider';
import { MessageService } from '../../../Services/messages/MessageService';
import { Sort } from '../../../Domain/pagination/Sort';
import { Identifier } from '../../../Domain/wrappers/identifier/Identifier';
import React, { useEffect } from 'react';
import { useAlertContext } from '../../Providers/AlertsProvider';
import { RetryButton } from '../Utils/Actions/RetryButton';
import { LoadingSpinner } from '../Utils/State/LoadingSpinner';
import Stack from '@mui/material/Stack';
import Typography from '@mui/material/Typography';
import { MessageView } from './Message';

function DayDivider(props: { date: Date }) {
    return (
        <React.Fragment>
            <Stack
                direction="row"
                alignSelf={'center'}
                alignItems="center"
                justifyContent="center"
                margin={2}
                border={1}
                borderColor={'divider'}
                borderRadius={1}
                padding={1}
                bgcolor="background.paper"
                width={'fit-content'}
            >
                <Typography variant="caption" color="textSecondary">
                    {props.date.toLocaleDateString()}
                </Typography>
            </Stack>
        </React.Fragment>
    );
}

export default function MessagesContainer({ channel }: { channel: Channel }) {
    const sessionManager = useSessionManager();

    const { state, items: messages, loadMore } = useMessagesContainer(channel);

    return (
        <React.Fragment>
            <Scrollable
                sx={{
                    height: 'calc(100vh - 148px)',
                    width: '100%',
                    alignItems: 'center',
                }}
                scrollDown={false}
                loadMore={loadMore}
            >
                <Stack style={{ width: '80%', flexDirection: 'column-reverse' }}>
                    {messages.map((message, index) => (
                        <React.Fragment key={message.id.value}>
                            {index > 0 &&
                                new Date(message.createdAt).toDateString() !==
                                    new Date(messages[index - 1].createdAt).toDateString() && (
                                    <DayDivider date={new Date(messages[index - 1].createdAt)} />
                                )}
                            <MessageView
                                channel={channel}
                                message={message}
                                isAuthor={message.author.id.value === sessionManager.session.user.id.value}
                                showAuthor={
                                    index === messages.length - 1 ||
                                    messages[index + 1].author.id.value !== message.author.id.value
                                }
                                spacing={
                                    index === messages.length - 1 ||
                                    messages[index + 1].author.id.value !== message.author.id.value
                                        ? 1
                                        : 0.25
                                }
                            />
                            {index === messages.length - 1 && !state.paginationState.info.next && (
                                <DayDivider date={new Date(message.createdAt)} />
                            )}
                        </React.Fragment>
                    ))}
                </Stack>
                {state.type === 'loading' && (
                    <LoadingSpinner
                        circularProgressProps={{
                            size: messages.length > 0 ? 24 : 48,
                        }}
                    />
                )}
                {state.type === 'error' && state.paginationState.items.length === 0 && (
                    <RetryButton onClick={loadMore} />
                )}
            </Scrollable>
        </React.Fragment>
    );
}

interface MessageContainerHook {
    state: InfiniteScrollState<Message>;
    items: Message[];
    loadMore: () => void;
}

function useMessagesContainer(channel: Channel): MessageContainerHook {
    const sessionManager = useSessionManager();
    const { showAlert } = useAlertContext();

    function fetchMessages(
        pageRequest: PaginationRequest,
        items: Message[],
        signal: AbortSignal,
    ): ApiResult<Pagination<Message>> {
        return sessionManager.executeWithRefresh(async () => {
            const before = items.length > 0 ? items[items.length - 1].createdAt : null;
            return await MessageService.getChannelMessages(
                channel,
                pageRequest,
                { sortBy: 'createdAt', direction: Sort.DESC },
                before,
                signal,
            );
        });
    }

    const { state, reset, loadMore, handleItemCreate, handleItemUpdate, handleItemDelete } = useInfiniteScroll({
        fetchItemsRequest: fetchMessages,
        limit: 25,
        getCount: false,
        useOffset: false,
    });

    const [previousChannel, setPreviousChannel] = React.useState<Channel | null>(null);

    useEffect(() => {
        if (channel.id.value !== previousChannel?.id.value) {
            reset();
            setPreviousChannel(channel);
        }
    }, [channel.id.value]);

    useEffect(() => {
        if (state.type === 'error') {
            showAlert({ message: 'Error loading messages', severity: 'error' });
        }
    }, [state.type]);

    const eventManager = useEventManager();

    const listeners = React.useMemo(
        () => [
            {
                type: 'message-created',
                listener: (event: MessageEvent<string>) => {
                    const message = eventManager.handleEvent(event).data as Message;
                    if (message.channelId.value === channel.id.value) {
                        handleItemCreate(message);
                    }
                },
            },
            {
                type: 'message-updated',
                listener: (event: MessageEvent<string>) => {
                    const message = eventManager.handleEvent(event).data as Message;
                    handleItemUpdate(message);
                },
            },
            {
                type: 'message-deleted',
                listener: (event: MessageEvent<string>) => {
                    const messageId = eventManager.handleEvent(event).data as Identifier;
                    handleItemDelete(messageId);
                },
            },
        ],
        [channel],
    );

    useEffect(() => {
        if (eventManager.isOpen) {
            listeners.forEach(eventManager.addListener);
        }
        return () => {
            listeners.forEach(eventManager.removeListener);
        };
    }, [eventManager.isOpen, listeners]);

    return { state, items: state.paginationState.items, loadMore };
}
