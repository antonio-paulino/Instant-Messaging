import { Dialog, DialogActions, DialogTitle } from '@mui/material';
import Button from '@mui/material/Button';
import React from 'react';

interface ConfirmActionWindowProps {
    message: string;
    onConfirm: () => void;
    onCancel: () => void;
}

export function ConfirmActionWindow({ message, onConfirm, onCancel }: ConfirmActionWindowProps) {
    return (
        <Dialog open={true} onClose={onCancel}>
            <DialogTitle sx={{ bgcolor: 'background.paper' }}>{message}</DialogTitle>
            <DialogActions sx={{ bgcolor: 'background.paper' }}>
                <Button
                    onClick={onCancel}
                    sx={{
                        bgcolor: 'error.main',
                        '&:hover': {
                            bgcolor: 'error.dark',
                        },
                    }}
                >
                    Cancel
                </Button>
                <Button
                    onClick={() => {
                        onConfirm();
                        onCancel();
                    }}
                    sx={{
                        bgcolor: 'success.main',
                        '&:hover': {
                            bgcolor: 'success.dark',
                        },
                    }}
                >
                    Confirm
                </Button>
            </DialogActions>
        </Dialog>
    );
}
