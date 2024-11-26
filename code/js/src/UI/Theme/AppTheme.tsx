import * as React from 'react';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import { inputsCustomizations } from './customizations/inputs';
import { dataDisplayCustomizations } from './customizations/dataDisplay';
import { feedbackCustomizations } from './customizations/feedback';
import { navigationCustomizations } from './customizations/navigation';
import { surfacesCustomizations } from './customizations/surfaces';
import { colorSchemes, typography, shadows, shape } from './themePrimitives';

// Theme from https://github.com/mui/material-ui/tree/v6.1.7/docs/data/material/getting-started/templates/shared-theme

interface AppThemeProps {
    children: React.ReactNode;
}

export default function AppTheme({ children }: AppThemeProps) {
    const theme = React.useMemo(() => {
        return createTheme({
            cssVariables: {
                colorSchemeSelector: 'data-mui-color-scheme',
                cssVarPrefix: 'template',
            },
            colorSchemes,
            typography,
            shadows,
            shape,
            components: {
                ...inputsCustomizations,
                ...dataDisplayCustomizations,
                ...feedbackCustomizations,
                ...navigationCustomizations,
                ...surfacesCustomizations,
            },
        });
    }, []);
    return (
        <ThemeProvider theme={theme} disableTransitionOnChange={true}>
            {children}
        </ThemeProvider>
    );
}
