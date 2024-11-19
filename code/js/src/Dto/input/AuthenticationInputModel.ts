/**
 * Input model for login
 *
 * The user can log in using either their username or email.
 *
 * @param username The username of the user.
 * @param email The email of the user.
 * @param password The password of the user.
 *
 */
export interface AuthenticationInputModel {
    username: string | null;
    email: string | null;
    password: string;
}
