import { FormState } from '../../../State/useForm';
import Select from '@mui/material/Select';
import MenuItem from '@mui/material/MenuItem';
import React from 'react';

export const expirationOptions = [
    { label: '30 Minutes', value: new Date(Date.now() + 30 * 60 * 1000) },
    { label: '1 Hour', value: new Date(Date.now() + 3600 * 1000) },
    { label: '12 Hours', value: new Date(Date.now() + 12 * 3600 * 1000) },
    { label: '1 Day', value: new Date(Date.now() + 24 * 3600 * 1000) },
    { label: '1 Week', value: new Date(Date.now() + 7 * 24 * 3600 * 1000) },
    {
        label: '1 Month',
        value: new Date(new Date().setMonth(new Date().getMonth() + 1)),
    },
];

export function ExpirationInput({
    id,
    name,
    handleChange,
    value,
    error,
}: {
    id: string;
    name: string;
    handleChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
    value: string;
    error: boolean;
}) {
    return (
        <Select id={id} name={name} variant={'outlined'} fullWidth onChange={handleChange} value={value} error={error}>
            {expirationOptions.map((option) => (
                <MenuItem
                    key={option.label}
                    value={option.label}
                    style={{ width: '100%', justifyContent: 'center', alignItems: 'center' }}
                >
                    {option.label}
                </MenuItem>
            ))}
        </Select>
    );
}
