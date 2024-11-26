import React, {
    createContext,
    useContext,
    useState,
    ReactNode,
    useEffect,
} from 'react';
import { Alert } from '@mui/material';
import { Fade } from 'react-awesome-reveal';

interface AlertMessage {
    message: string;
    severity: 'success' | 'error' | 'info' | 'warning';
}

interface AlertContextType {
    showAlert: (alert: AlertMessage) => void;
}

const AlertContext = createContext<AlertContextType | undefined>(undefined);

export function AlertProvider({ children }: { children: ReactNode }) {
    const [alert, setAlert] = useState<AlertMessage | null>(null);

    const showAlert = (alert: AlertMessage) => {
        setAlert(alert);
        setTimeout(() => setAlert(null), 3000); // Auto-hide after 3 seconds
    };

    useEffect(() => {
        if (alert) {
            console.log(alert);
        }
    }, [alert]);

    return (
        <AlertContext.Provider value={{ showAlert }}>
            {children}
            {alert && (
                <Fade>
                    <Alert
                        severity={alert.severity}
                        sx={{
                            position: 'fixed',
                            zIndex: 1200,
                            minWidth: { xs: '80%', sm: '50%' },
                            left: '50%',
                            top: '5%',
                            transform: 'translate(-50%, -50%)',
                        }}
                    >
                        {alert.message}
                    </Alert>
                </Fade>
            )}
        </AlertContext.Provider>
    );
}

export function useAlert() {
    const context = useContext(AlertContext);
    if (!context) {
        throw new Error('useAlert must be used within an AlertProvider');
    }
    return context;
}
