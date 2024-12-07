import { ApiResult, handle } from '../media/Problem';
import { BaseHTTPService } from '../BaseHTTPService';
import { AuthenticationInputModel } from '../../Dto/input/AuthenticationInputModel';
import { CredentialsOutputModel } from '../../Dto/output/credentials/CredentialsOutputModel';
import { Session } from '../../Domain/sessions/Session';
import { Name } from '../../Domain/wrappers/name/Name';
import { Email } from '../../Domain/wrappers/email/Email';
import { Password } from '../../Domain/wrappers/password/Password';
import { ImInvitation, imInvitationFromDto } from '../../Domain/invitations/ImInvitation';
import { UserCreationInputModel } from '../../Dto/input/UserCreationInputModel';
import { UserCreationOutputModel } from '../../Dto/output/users/UserCreationOutputModel';
import { ImInvitationOutputModel } from '../../Dto/output/invitations/ImInvitationOutputModel';
import { User, userFromCreation } from '../../Domain/user/User';
import { Uri } from '../Uri';
import { ImInvitationCreationInputModel } from '../../Dto/input/ImInvitationCreationInputModel';

export namespace AuthService {
    import post = BaseHTTPService.post;

    import LOGIN_ROUTE = Uri.LOGIN_ROUTE;
    import REGISTER_ROUTE = Uri.REGISTER_ROUTE;
    import LOGOUT_ROUTE = Uri.LOGOUT_ROUTE;
    import REFRESH_ROUTE = Uri.REFRESH_ROUTE;
    import CREATE_INVITATION_ROUTE = Uri.CREATE_INVITATION_ROUTE;

    /**
     * Logs in a user.
     *
     * The user can log in using either their username or email.
     *
     * @param username The username of the user.
     * @param email The email of the user.
     * @param password The password of the user.
     * @param abortSignal The signal to abort the request.
     *
     * @returns The session that was created.
     */
    export async function login(
        password: string,
        username?: string,
        email?: string,
        abortSignal?: AbortSignal,
    ): ApiResult<Session> {
        return await handle(
            post<AuthenticationInputModel, CredentialsOutputModel>({
                uri: LOGIN_ROUTE,
                requestBody: {
                    username: username,
                    email: email,
                    password: password,
                },
                abortSignal: abortSignal,
            }),
            (credentials) => Session.fromDto(credentials),
        );
    }

    /**
     * Registers a user.
     *
     * @param username The username of the user.
     * @param email The email of the user.
     * @param password The password of the user.
     * @param token The invitation token.
     * @param abortSignal The signal to abort the request.
     *
     * @returns The user that was created.
     */
    export async function register(
        username: Name,
        email: Email,
        password: Password,
        token: string,
        abortSignal?: AbortSignal,
    ): ApiResult<User> {
        return await handle(
            post<UserCreationInputModel, UserCreationOutputModel>({
                uri: REGISTER_ROUTE,
                requestBody: {
                    username: username.value,
                    email: email.value,
                    password: password.value,
                    invitation: token,
                },
                abortSignal: abortSignal,
            }),
            (userCreationOutputModel) => userFromCreation(userCreationOutputModel, username, email),
        );
    }

    /**
     * Logs out the current user, using the current session cookies.
     *
     * @returns The result of the request.
     */
    export async function logout(abortSignal?: AbortSignal): ApiResult<void> {
        return await post<void, void>({
            uri: LOGOUT_ROUTE,
            abortSignal: abortSignal,
            fetchResBody: false,
        });
    }

    /**
     * Refreshes the current session.
     *
     * @returns The session that was created.
     */
    export async function refresh(abortSignal?: AbortSignal): ApiResult<Session> {
        return await handle(
            post<void, CredentialsOutputModel>({
                uri: REFRESH_ROUTE,
                abortSignal: abortSignal,
            }),
            (credentials) => Session.fromDto(credentials),
        );
    }

    /**
     * Creates an invitation.
     *
     * @returns The invitation that was created.
     */
    export async function createInvitation(expiresAt: Date, abortSignal?: AbortSignal): ApiResult<ImInvitation> {
        return await handle(
            post<ImInvitationCreationInputModel, ImInvitationOutputModel>({
                uri: CREATE_INVITATION_ROUTE,
                abortSignal: abortSignal,
                requestBody: {
                    expiresAt: expiresAt.toISOString(),
                },
            }),
            imInvitationFromDto,
        );
    }
}
