export namespace Routes {
    export const HOME = '/home';
    export const CREATE_CHANNEL = `${HOME}/create-channel`;
    export const CREATE_IM_INVITATION = `${HOME}/create-im-invitation`;
    export const SEARCH_CHANNELS = `${HOME}/search-channels`;
    export const CHANNEL_SETTINGS = `${HOME}/channel-settings`;

    export const SIGN_IN = '/sign-in';
    export const SIGN_UP = '/sign-up';
    export const REFERRAL = '/referral';

    export const CHANNEL_INVITATIONS = `${CHANNEL_SETTINGS}/invitations`;
    export const INVITE_CHANNEL_MEMBER = `${CHANNEL_SETTINGS}/invite-member`;
    export const CHANNEL_MEMBERS = `${CHANNEL_SETTINGS}/members`;
    export const DELETE_CHANNEL = `${CHANNEL_SETTINGS}/delete-channel`;
    export const EDIT_CHANNEL = `${CHANNEL_SETTINGS}/edit-channel`;
    export const LEAVE_CHANNEL = `${CHANNEL_SETTINGS}/leave-channel`;

    export const TOKEN_PARAM = 'token';
}
