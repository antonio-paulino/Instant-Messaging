import Select from '@mui/material/Select';
import MenuItem from '@mui/material/MenuItem';
import { ChannelRole } from '../../../../Domain/channel/ChannelRole';
import React from 'react';

export function RoleInput({
    id,
    name,
    value,
    handleChange,
    error,
}: {
    id?: string;
    name: string;
    value: ChannelRole;
    handleChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
    error: boolean;
}) {
    return (
        <Select id={id} name={name} value={value} onChange={handleChange} error={error}>
            {Object.values(ChannelRole)
                .filter((role) => role !== ChannelRole.OWNER)
                .map((role) => (
                    <MenuItem key={role} value={role} style={{ justifyContent: 'center', alignItems: 'center' }}>
                        {role}
                    </MenuItem>
                ))}
        </Select>
    );
}
