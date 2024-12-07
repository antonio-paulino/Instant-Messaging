import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react';

interface VirtualizedListProps<T> {
    items: T[];
    listStyle?: React.CSSProperties;
    itemStyle?: React.CSSProperties;
    getItemHeight?: (index: number) => number;
    fixedHeight?: number;
    overscan?: number;
    onItemsRendered?: (start: number, end: number) => void;
    renderItem: (params: { index: number; style: React.CSSProperties }) => React.ReactNode;
    header?: React.ReactNode;
    footer?: React.ReactNode;
}

// Does not work with reverse scrolling or dynamically changing heights
export function VirtualizedList<T>({
    items,
    getItemHeight,
    fixedHeight,
    overscan = 1,
    itemStyle,
    listStyle,
    renderItem,
    onItemsRendered,
    header,
    footer,
}: VirtualizedListProps<T>) {
    const [visibleItems, setVisibleItems] = useState<number[]>([]);
    const containerRef = useRef<HTMLDivElement>(null);
    const firstItemRef = useRef<HTMLDivElement>(null);
    const lastItemRef = useRef<HTMLDivElement>(null);
    const [initialized, setInitialized] = useState(false);

    const cumulativeHeights = useMemo(() => {
        const heights = [];
        let sum = 0;
        for (let i = 0; i < items.length; i++) {
            sum += fixedHeight ? fixedHeight : getItemHeight(i);
            heights.push(sum);
        }
        return heights;
    }, [items, getItemHeight]);

    const totalHeight = cumulativeHeights[cumulativeHeights.length - 1] || 0;

    const debounceRef = useRef<number | null>(null);

    const calculateVisibleRange = useCallback(() => {
        if (!containerRef.current) {
            return;
        }

        if (debounceRef.current) {
            cancelAnimationFrame(debounceRef.current);
        }

        debounceRef.current = requestAnimationFrame(() => {
            const scrollTop = containerRef.current.scrollTop;

            const containerHeight = containerRef.current.clientHeight;

            const start = Math.max(findStartIndex(scrollTop) - overscan, 0);
            const end = Math.min(findEndIndex(scrollTop + containerHeight) + overscan, items.length - 1);

            setVisibleItems((prev) => {
                if (prev.length === 0 || prev[0] !== start || prev[prev.length - 1] !== end) {
                    return Array.from({ length: end - start + 1 }, (_, i) => i + start);
                }
                return prev;
            });

            if (onItemsRendered) {
                onItemsRendered(start, end);
            }
        });
    }, [items, overscan, cumulativeHeights, onItemsRendered, totalHeight]);

    useEffect(() => {
        calculateVisibleRange();
    }, [totalHeight]);

    const findStartIndex = useCallback(
        (scroll: number) => {
            if (fixedHeight) {
                return Math.floor(scroll / fixedHeight);
            }

            let low = 0,
                high = cumulativeHeights.length - 1;
            while (low < high) {
                const mid = Math.floor((low + high) / 2);
                if (cumulativeHeights[mid] >= scroll) high = mid;
                else low = mid + 1;
            }
            return low;
        },
        [cumulativeHeights],
    );

    const findEndIndex = useCallback(
        (scroll: number) => {
            if (fixedHeight) {
                return Math.min(Math.floor(scroll / fixedHeight), items.length - 1);
            }
            let low = 0,
                high = cumulativeHeights.length - 1;
            while (low < high) {
                const mid = Math.floor((low + high) / 2);
                if (cumulativeHeights[mid] > scroll) high = mid;
                else low = mid + 1;
            }
            return high;
        },
        [cumulativeHeights],
    );

    const translateY = useMemo(() => {
        return cumulativeHeights[visibleItems[0] - 1] || 0;
    }, [visibleItems, cumulativeHeights, totalHeight]);

    const cachedStyle = useMemo(() => {
        const styles: React.CSSProperties[] = [];
        for (let i = 0; i < items.length; i++) {
            styles.push({ height: fixedHeight || getItemHeight(i), ...itemStyle });
        }
        return styles;
    }, [items, getItemHeight]);

    useEffect(() => {
        if (!initialized && items.length > 0 && containerRef.current) {
            calculateVisibleRange();
            setInitialized(true);
        }
        return () => {
            if (!containerRef.current || items.length === 0) {
                setInitialized(false);
            }
        };
    }, [items, initialized, containerRef.current]);

    return (
        <div
            style={{
                height: '100%',
                overflowY: items.length > 0 ? 'auto' : 'hidden',
                ...listStyle,
            }}
            ref={containerRef}
            onScroll={calculateVisibleRange}
        >
            <div style={{ height: totalHeight, position: 'relative' }}>
                {header}
                <div
                    style={{
                        transform: `translateY(${translateY}px)`,
                        width: '100%',
                    }}
                >
                    {visibleItems.map(
                        (i, idx) =>
                            items[i] && (
                                <div
                                    className={'virtualized-item-container'}
                                    key={i}
                                    ref={
                                        idx === 0
                                            ? firstItemRef
                                            : idx === visibleItems.length - 1
                                              ? lastItemRef
                                              : undefined
                                    }
                                    style={cachedStyle[i]}
                                >
                                    {renderItem({
                                        index: i,
                                        style: cachedStyle[i],
                                    })}
                                </div>
                            ),
                    )}
                    {footer}
                </div>
            </div>
        </div>
    );
}
