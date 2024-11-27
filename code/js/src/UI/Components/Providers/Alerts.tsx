import React, {
    createContext,
    useContext,
    useState,
    ReactNode,
    useEffect,
} from 'react';
import { Alert, Fade } from '@mui/material';

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
    const [show, setShow] = useState(false);

    const showAlert = (alert: AlertMessage) => {
        setAlert(alert);
        setShow(true);
        setTimeout(() => {
            setShow(false);
            setAlert(null);
        }, 3000);
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
                <Fade in={show} timeout={500}>
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
