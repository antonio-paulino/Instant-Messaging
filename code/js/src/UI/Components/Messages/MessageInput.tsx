import { Message } from '../../../Domain/messages/Message';
import React, { useEffect, useRef } from 'react';
import { FormState } from '../../State/useForm';
import { TextFieldProps } from '@mui/material';
import TextField from '@mui/material/TextField';
import ErrorList from '../Utils/State/ErrorList';
import { MessageContent } from './MessageContent';

export function MessageInput({
    formKey,
    message,
    editMode,
    setEditMode,
    setDeleteMode,
    handleChange,
    handleSubmit,
    enabled,
    state,
    textFieldProps,
}: {
    formKey: string;
    message?: Message;
    editMode?: boolean;
    setEditMode?: (editMode: boolean) => void;
    setDeleteMode?: (deleteMode: boolean) => void;
    handleChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
    handleSubmit: (e: React.FormEvent<HTMLFormElement>) => void;
    enabled: boolean;
    state: FormState;
    textFieldProps?: TextFieldProps;
}) {
    const inputRef = useRef<HTMLInputElement>(null);

    useEffect(() => {
        if (editMode && inputRef.current) {
            const length = inputRef.current.value.length;
            inputRef.current.setSelectionRange(length, length);
            inputRef.current.focus();
        }
    }, [editMode]);

    return (
        <TextField
            {...textFieldProps}
            name={formKey}
            value={state.values[formKey]}
            onChange={handleChange}
            multiline
            minRows={1}
            maxRows={5}
            fullWidth
            disabled={!enabled}
            inputRef={inputRef}
            helperText={<ErrorList errors={state.errors[formKey]} />}
            slotProps={{
                htmlInput: {
                    component: (props: any) => <MessageContent content={state.values[formKey]} />,
                },
            }}
            onKeyDown={(e) => {
                if (e.key === 'Enter' && !e.shiftKey) {
                    e.preventDefault();
                    if (message?.content === state.values[formKey]) {
                        setEditMode?.(false);
                    } else if (state.values[formKey].trim() === '') {
                        setDeleteMode?.(true);
                    } else if (state.errors[formKey].length === 0) {
                        handleSubmit(e as any);
                    }
                } else if (e.key === 'Escape') {
                    setEditMode?.(false);
                }
            }}
        />
    );
}
