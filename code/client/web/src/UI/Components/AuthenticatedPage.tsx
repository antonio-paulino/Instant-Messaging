import * as React from 'react';
import { useLoggedIn } from '../Providers/SessionProvider';
import { Navigate, useLocation } from 'react-router-dom';
import { Routes } from '../../routes';

export function AuthenticatedPage({ children }: { children: React.ReactNode }) {
    const login = useLoggedIn();
    const location = useLocation();

    if (!login) {
        return <Navigate to={Routes.SIGN_IN} state={{ from: location.pathname }} />;
    }

    return <>{children}</>;
}
