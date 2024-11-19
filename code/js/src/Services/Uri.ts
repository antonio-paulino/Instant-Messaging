export namespace Uri {
    export const LOGIN_ROUTE = '/api/auth/login';
    export const LOGOUT_ROUTE = '/api/auth/logout';
    export const REGISTER_ROUTE = '/api/auth/register';
    export const REFRESH_ROUTE = '/api/auth/refresh';
    export const CREATE_INVITATION_ROUTE = '/api/auth/invitations';

    export const USER_ID_PARAM = '{userId}';
    export const CHANNEL_ID_PARAM = '{channelId}';
    export const INVITATION_ID_PARAM = '{invitationId}';
    export const MESSAGE_ID_PARAM = '{messageId}';

    export const CHANNELS_ROUTE = '/api/channels';
    export const CHANNEL_ROUTE = `/api/channels/${CHANNEL_ID_PARAM}`;
    export const CHANNEL_MEMBERS_ROUTE = `/api/channels/${CHANNEL_ID_PARAM}/members`;
    export const CHANNEL_MEMBER_ROUTE = `/api/channels/${CHANNEL_ID_PARAM}/members/${USER_ID_PARAM}`;

    export const CHANNEL_INVITATIONS_ROUTE = `/api/channels/${CHANNEL_ID_PARAM}/invitations`;
    export const CHANNEL_INVITATION_ROUTE = `/api/channels/${CHANNEL_ID_PARAM}/invitations/${INVITATION_ID_PARAM}`;

    export const MESSAGES_ROUTE = `/api/channels/${CHANNEL_ID_PARAM}/messages`;
    export const MESSAGE_ROUTE = `/api/channels/${CHANNEL_ID_PARAM}/messages/${MESSAGE_ID_PARAM}`;

    export const USER_ROUTE = `/api/users/${USER_ID_PARAM}`;
    export const USER_CHANNELS_ROUTE = `/api/users/${USER_ID_PARAM}/channels`;
    export const USERS_ROUTE = '/api/users';
}
