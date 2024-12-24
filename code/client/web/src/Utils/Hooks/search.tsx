import { useEffect, useState } from 'react';

export function useSearchReset(searchValue: string, reset: () => void) {
    const [prevSearchValue, setPrevSearchValue] = useState<string | null>(null);

    useEffect(() => {
        if (searchValue !== prevSearchValue) {
            reset();
            setPrevSearchValue(searchValue);
        }
    }, [searchValue]);
}
