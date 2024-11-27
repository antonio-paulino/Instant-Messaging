import { ChangeEvent, FormEvent, useEffect, useReducer } from 'react';
import { doAfterDelay } from '../../../Utils/Time';
import { ProblemResponse } from '../../../Services/media/Problem';

type PropKey = string;
type PropValue = any;
type PropValidator = (value: PropValue) => Promise<string[]> | string[];
type ValidationError = Record<PropKey, string[]>;

export interface FormProps {
    initialValues: Record<PropKey, PropValue>;
    validate?: Record<PropKey, PropValidator>;
    onSubmit: (
        values: Record<PropKey, PropValue>,
    ) => Promise<ProblemResponse | void> | void;
    onChange?: (values: Record<PropKey, PropValue>) => void;
}

export interface Form {
    state: FormState;
    handleChange: (e: ChangeEvent<HTMLInputElement>) => void;
    handleSubmit: (e: FormEvent<HTMLFormElement>) => void;
}

type FormAction =
    | { type: 'new-value'; payload: Record<PropKey, PropValue> }
    | {
          type: 'new-validation';
          payload: ValidationError;
          validatedValues: Record<PropKey, PropValue>;
      }
    | { type: 'set-loading'; seenValues: Record<PropKey, PropValue> }
    | { type: 'error'; payload: ProblemResponse }
    | { type: 'success'; payload: Record<PropKey, PropValue> };

type FormState = {
    type: 'loading' | 'loaded' | 'error';
    values: Record<PropKey, PropValue>;
    errors: ValidationError;
    error: ProblemResponse | null;
};

const reducer = (state: FormState, action: FormAction): FormState => {
    switch (state.type) {
        case 'loading': {
            switch (action.type) {
                case 'success':
                    return {
                        ...state,
                        type: 'loaded',
                        values: action.payload,
                        error: null,
                    };
                case 'error':
                    return {
                        ...state,
                        type: 'error',
                        error: action.payload,
                    };
                case 'new-validation':
                    return state.values === action.validatedValues
                        ? {
                              ...state,
                              type: 'loaded',
                              errors: action.payload,
                          }
                        : state;
                case 'new-value':
                    return {
                        ...state,
                        values: {
                            ...state.values,
                            ...action.payload,
                        },
                    };
                default:
                    return state;
            }
        }
        case 'loaded':
        case 'error': {
            switch (action.type) {
                case 'new-value':
                    return {
                        ...state,
                        values: {
                            ...state.values,
                            ...action.payload,
                        },
                    };
                case 'new-validation':
                    return state.values === action.validatedValues
                        ? {
                              ...state,
                              errors: action.payload,
                          }
                        : state;
                case 'set-loading':
                    if (state.values !== action.seenValues) {
                        return state;
                    }
                    return {
                        ...state,
                        type: 'loading',
                        values: action.seenValues,
                    };
                default:
                    return state;
            }
        }
    }
};

export function useForm({
    initialValues,
    validate,
    onSubmit,
}: FormProps): Form {
    const [state, dispatch] = useReducer(reducer, {
        type: 'loaded',
        values: initialValues,
        errors: Object.keys(initialValues).reduce(
            (acc, key) => ({ ...acc, [key]: [] }),
            {},
        ),
        error: null,
    });

    useEffect(() => {
        if (!validate || state.type === 'loading') {
            return;
        }
        const timeout = doAfterDelay(250, () => {
            const nonBlankFields = Object.keys(state.values).filter(
                (key) => state.values[key] !== '' && validate[key],
            );
            const validationPromises = nonBlankFields.map((key) =>
                validate[key](state.values[key]),
            );
            if (validationPromises.length > 0) {
                dispatch({ type: 'set-loading', seenValues: state.values });
                Promise.all(validationPromises).then((results) => {
                    const errors = nonBlankFields.reduce(
                        (acc, key, index) => ({
                            ...acc,
                            [key]: results[index],
                        }),
                        { ...state.errors },
                    );
                    dispatch({
                        type: 'new-validation',
                        payload: errors,
                        validatedValues: state.values,
                    });
                });
            }
        });
        return () => clearTimeout(timeout);
    }, [state.values]);

    const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        dispatch({ type: 'new-value', payload: { [name]: value } });
    };

    const handleSubmit = (e: FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        if (hasErrors(state.errors)) return;

        dispatch({ type: 'set-loading', seenValues: state.values });

        const result = onSubmit(state.values);
        result instanceof Promise
            ? result.then((response) => {
                  response
                      ? dispatch({ type: 'error', payload: response })
                      : dispatch({ type: 'success', payload: state.values });
              })
            : dispatch({ type: 'success', payload: state.values });
    };

    function hasErrors(errors: ValidationError): boolean {
        return Object.values(errors).some((error) => error.length > 0);
    }

    return { state, handleChange, handleSubmit };
}
