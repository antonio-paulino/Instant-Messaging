/**
 * Input model for creating a user
 *
 * @param username The username of the user.
 * @param email The email of the user.
 * @param password The password of the user.
 * @param invitation The invitation code.
 */
export interface UserCreationInputModel {
    username: string;
    email: string;
    password: string;
    invitation: string;
}
