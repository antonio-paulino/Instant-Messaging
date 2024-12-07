import * as React from 'react';

const threshold = 250;

export function handleScroll(ref: HTMLDivElement, bottomScroll: boolean, loadMore?: () => void) {
    const { scrollTop, scrollHeight, clientHeight } = ref;
    const scroll = bottomScroll ? scrollTop : -scrollTop;
    if (scrollHeight - scroll - clientHeight < threshold) {
        loadMore && loadMore();
    }
}

export function handleScrollThrottled(
    throttleRef: React.MutableRefObject<boolean>,
    scrollRef: React.MutableRefObject<HTMLDivElement>,
    scrollDown?: boolean,
    loadMore?: () => void,
    throttle: number = 100,
) {
    if (throttleRef.current) {
        return;
    }
    throttleRef.current = true;
    setTimeout(() => {
        handleScroll(scrollRef.current, scrollDown, loadMore);
        throttleRef.current = false;
    }, 100);
}
