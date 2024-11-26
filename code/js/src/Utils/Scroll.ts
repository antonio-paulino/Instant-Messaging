import * as React from 'react';

export function handleScroll(
    event: React.UIEvent<HTMLDivElement>,
    bottomScroll: boolean,
    loadMore: () => void,
) {
    const { scrollTop, scrollHeight, clientHeight } = event.currentTarget;
    const threshold = bottomScroll ? 1000 : 0;
    if (scrollHeight - scrollTop - clientHeight < threshold) {
        loadMore();
    }
}
