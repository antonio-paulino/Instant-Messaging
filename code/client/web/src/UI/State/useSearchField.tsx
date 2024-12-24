import { useEffect, useReducer } from 'react';

type SearchFieldState = {
    fieldValue: string;
    searchValue: string;
};

type SearchFieldAction =
    | {
          type: 'new-value';
          payload: string;
      }
    | {
          type: 'new-search-value';
          payload: string;
      };

function searchFieldReducer(state: SearchFieldState, action: SearchFieldAction): SearchFieldState {
    switch (action.type) {
        case 'new-value':
            return {
                ...state,
                fieldValue: action.payload,
            };
        case 'new-search-value':
            return {
                ...state,
                searchValue: action.payload,
            };
        default:
            return state;
    }
}

type SearchField = {
    searchValue: string;
    setValue: (value: string) => void;
};

export function useSearchField(searchTimeout: number): SearchField {
    const [state, dispatch] = useReducer(searchFieldReducer, {
        fieldValue: '',
        searchValue: '',
    });

    useEffect(() => {
        const timeout = setTimeout(() => {
            dispatch({
                type: 'new-search-value',
                payload: state.fieldValue,
            });
        }, searchTimeout);
        return () => {
            clearTimeout(timeout);
        };
    }, [state.fieldValue]);

    function setValue(value: string) {
        dispatch({
            type: 'new-value',
            payload: value,
        });
    }

    return {
        searchValue: state.searchValue,
        setValue,
    };
}
