import { useEffect, useRef } from 'react';

/**
 * Returns an AbortSignal that is aborted when the component is unmounted
 *
 * @returns The AbortSignal
 */

export function useAbortSignal(): AbortSignal {
    const abortController = useRef(new AbortController());

    useEffect(() => {
        return () => {
            abortController.current.abort();
        };
    }, []);

    return abortController.current.signal;
}

export function useAbortController(): AbortController {
    const abortController = useRef(new AbortController());

    useEffect(() => {
        return () => {
            abortController.current.abort();
        };
    }, []);

    return abortController.current;
}
