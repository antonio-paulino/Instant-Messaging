import { Theme, Components } from '@mui/material/styles';
import { gray } from '../themePrimitives';

/* eslint-disable import/prefer-default-export */
export const feedbackCustomizations: Components<Theme> = {
    MuiAlert: {
        styleOverrides: {
            root: ({ theme }) => ({
                borderRadius: theme.shape.borderRadius,
                border: '1px solid',
            }),
            icon: {
                color: 'inherit',
            },
            message: {
                color: 'inherit',
            },
            action: {
                color: 'inherit',
            },
        },
    },
    MuiDialog: {
        styleOverrides: {
            root: ({ theme }) => ({
                '& .MuiDialog-paper': {
                    borderRadius: '10px',
                    border: '1px solid',
                    borderColor: theme.palette.divider,
                },
            }),
        },
    },
    MuiLinearProgress: {
        styleOverrides: {
            root: ({ theme }) => ({
                height: 8,
                borderRadius: 8,
                backgroundColor: gray[200],
                ...theme.applyStyles('dark', {
                    backgroundColor: gray[800],
                }),
            }),
        },
    },
};
