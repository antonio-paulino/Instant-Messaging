import React, { createContext, useContext, useState } from 'react';
import { Session } from '../../../Domain/sessions/Session';
import { ApiResult } from '../../../Services/media/Problem';
import { useNavigate } from 'react-router-dom';
import { AuthService } from '../../../Services/auth/AuthService';
import { AccessToken } from '../../../Domain/tokens/AccessToken';
import { RefreshToken } from '../../../Domain/tokens/RefreshToken';
import { Identifier } from '../../../Domain/wrappers/identifier/Identifier';

const SESSION_STORAGE_KEY = 'session';

/**
 * Session manager
 *
 * @param session The current session.
 * @param setSession Sets the current session.
 * @param clearSession Clears the current session.
 */
export interface SessionManager {
    readonly session: Session | null;
    readonly setSession: (session: Session) => void;
    readonly clearSession: () => void;
    executeWithRefresh<T>(request: () => ApiResult<T> | void): ApiResult<T>;
}

const SessionContext = createContext<SessionManager>({
    session: null,
    setSession: () => {},
    clearSession: () => {},
    executeWithRefresh<T>(): ApiResult<T> {
        throw new Error('Not implemented');
    },
});

export default function SessionProvider({
    children,
}: {
    children: React.ReactNode;
}) {
    const [session, setSession] = useState<Session | null>(() => {
        const sessionJson = localStorage.getItem(SESSION_STORAGE_KEY);
        if (sessionJson) {
            const session = JSON.parse(sessionJson);
            const user = session.user;
            const accessToken = new AccessToken(
                session.accessToken.token,
                new Date(session.accessToken.expiresAt),
            );
            const refreshToken = new RefreshToken(
                session.refreshToken.token,
                new Date(session.refreshToken.expiresAt),
            );
            return new Session(
                new Identifier(session.id),
                user,
                accessToken,
                refreshToken,
                new Date(session.expiresAt),
            );
        }
    });

    const navigate = useNavigate();

    const clearSession = () => {
        localStorage.removeItem(SESSION_STORAGE_KEY);
        setSession(null);
    };

    async function executeWithRefresh<T>(
        request: () => ApiResult<T>,
        signal?: AbortSignal,
    ): ApiResult<T> {
        if (!session || new Date(session.expiresAt) < new Date()) {
            clearSession();
            navigate('/sign-in');
            return;
        }

        if (session.accessToken.isExpired()) {
            const result = await AuthService.refresh(signal);
            if (result.isSuccess()) {
                updateSession(result.getRight());
            } else {
                if (result.getLeft().status === 401) {
                    clearSession();
                    navigate('/sign-in');
                    return;
                }
                // @ts-ignore
                return result as ApiResult<T>;
            }
        }

        const afterRefresh = await request();

        if (
            afterRefresh &&
            afterRefresh.isFailure() &&
            afterRefresh.getLeft().status === 401
        ) {
            clearSession();
            return;
        }

        return afterRefresh;
    }

    const updateSession = (session: Session) => {
        localStorage.setItem(SESSION_STORAGE_KEY, JSON.stringify(session));
        setSession(session);
    };

    return (
        <SessionContext.Provider
            value={{
                session,
                setSession: updateSession,
                clearSession,
                executeWithRefresh,
            }}
        >
            {children}
        </SessionContext.Provider>
    );
}

/**
 * React hook for using the session manager
 *
 * @returns The session manager
 */
export function useSessionManager(): SessionManager {
    return useContext(SessionContext);
}

/**
 * React hook for using the current session
 *
 * @returns The current session
 */
export function useSession(): Session | null {
    return useSessionManager().session;
}

/**
 * Checks if the user is logged in
 */
export function isLoggedIn(): boolean {
    const session = useSession();
    return !(!session || new Date(session.expiresAt) < new Date());
}
