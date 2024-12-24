import React, { createContext, useContext } from 'react';
import { InfiniteScroll, InfiniteScrollProps, useInfiniteScroll } from '../State/useInfiniteScroll';
import { IdentifiableValue } from '../../Domain/IdentifiableValue';
import { Channel } from '../../Domain/channel/Channel';
import { ChannelInvitation } from '../../Domain/invitations/ChannelInvitation';
import { Identifier } from '../../Domain/wrappers/identifier/Identifier';

type InfiniteScrollChannelsContextType = InfiniteScroll<IdentifiableValue<Channel>> & {
    selectedChannel: Identifier | null;
    setSelectedChannel: (channel: Identifier | null) => void;
};

const InfiniteScrollContextChannels = createContext<InfiniteScrollChannelsContextType | undefined>(undefined);

const InfiniteScrollContextInvitations = createContext<InfiniteScroll<ChannelInvitation> | undefined>(undefined);

export function InfiniteScrollChannelProvider(
    props: InfiniteScrollProps<IdentifiableValue<Channel>> & {
        selectedChannel: Identifier | null;
        setSelectedChannel: (channel: Identifier | null) => void;
        children: React.ReactNode;
    },
) {
    const value = {
        ...useInfiniteScroll(props),
        selectedChannel: props.selectedChannel,
        setSelectedChannel: props.setSelectedChannel,
    };

    return (
        <InfiniteScrollContextChannels.Provider value={value}>{props.children}</InfiniteScrollContextChannels.Provider>
    );
}

export function InfiniteScrollInvitationProvider(
    props: InfiniteScrollProps<IdentifiableValue<ChannelInvitation>> & {
        children: React.ReactNode;
    },
) {
    const value = useInfiniteScroll(props);
    return (
        <InfiniteScrollContextInvitations.Provider value={value}>
            {props.children}
        </InfiniteScrollContextInvitations.Provider>
    );
}

export function useInfiniteScrollContextChannels(): InfiniteScrollChannelsContextType {
    const context = useContext(InfiniteScrollContextChannels);
    if (context === undefined) {
        throw new Error('useInfiniteScrollContextChannels must be used within a InfiniteScrollProvider');
    }
    return context;
}

export function useInfiniteScrollContextInvitations(): InfiniteScroll<ChannelInvitation> {
    const context = useContext(InfiniteScrollContextInvitations);
    if (context === undefined) {
        throw new Error('useInfiniteScrollContextInvitations must be used within a InfiniteScrollProvider');
    }
    return context;
}
