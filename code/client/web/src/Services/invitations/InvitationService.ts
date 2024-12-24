import {
    ChannelInvitation,
    channelInvitationFromCreation,
    channelInvitationFromDto,
} from '../../Domain/invitations/ChannelInvitation';
import { Channel } from '../../Domain/channel/Channel';
import { User } from '../../Domain/user/User';
import { ChannelRole } from '../../Domain/channel/ChannelRole';
import { ApiResult, handle } from '../media/Problem';
import { BaseHTTPService } from '../BaseHTTPService';
import { ChannelInvitationCreationInputModel } from '../../Dto/input/ChannelInvitationCreationInputModel';
import { ChannelInvitationCreationOutputModel } from '../../Dto/output/invitations/ChannelInvitationCreationOutputModel';
import { Uri } from '../Uri';
import { Identifier } from '../../Domain/wrappers/identifier/Identifier';
import { ChannelInvitationOutputModel } from '../../Dto/output/invitations/ChannelInvitationOutputModel';
import { PaginationRequest } from '../../Domain/pagination/PaginationRequest';
import { SortRequest } from '../../Domain/pagination/SortRequest';
import { ChannelInvitationsPaginatedOutputModel } from '../../Dto/output/invitations/ChannelInvitationsPaginatedOutputModel';
import { buildQuery } from '../Utils';
import { Pagination } from '../../Domain/pagination/Pagination';
import { ChannelInvitationUpdateInputModel } from '../../Dto/input/ChannelInvitationUpdateInputModel';
import { InvitationAcceptInputModel } from '../../Dto/input/InvitationAcceptInputModel';
import { ChannelInvitationStatus } from '../../Domain/invitations/ChannelInvitationStatus';

export namespace InvitationService {
    import post = BaseHTTPService.post;
    import get = BaseHTTPService.get;
    import patch = BaseHTTPService.patch;
    import deleteRequest = BaseHTTPService.deleteRequest;

    import CHANNEL_INVITATIONS_ROUTE = Uri.CHANNEL_INVITATIONS_ROUTE;
    import CHANNEL_ID_PARAM = Uri.CHANNEL_ID_PARAM;
    import INVITATION_ID_PARAM = Uri.INVITATION_ID_PARAM;
    import CHANNEL_INVITATION_ROUTE = Uri.CHANNEL_INVITATION_ROUTE;
    import USER_INVITATIONS_ROUTE = Uri.USER_INVITATIONS_ROUTE;
    import USER_ID_PARAM = Uri.USER_ID_PARAM;
    import USER_INVITATION_ROUTE = Uri.USER_INVITATION_ROUTE;

    /**
     * Creates a channel invitation.
     *
     * @param channel - The channel to create the invitation for.
     * @param invitee - The user to invite.
     * @param expiresAt - The date the invitation expires.
     * @param role - The role of the user in the channel.
     * @param abortSignal - The signal to abort the request.
     *
     * @returns The channel invitation that was created.
     */
    export async function createChannelInvitation(
        channel: Channel,
        invitee: User,
        expiresAt: Date,
        role: ChannelRole,
        abortSignal?: AbortSignal,
    ): ApiResult<ChannelInvitation> {
        return await handle(
            post<ChannelInvitationCreationInputModel, ChannelInvitationCreationOutputModel>({
                uri: CHANNEL_INVITATIONS_ROUTE.replace(CHANNEL_ID_PARAM, channel.id.value.toString()),
                requestBody: {
                    invitee: invitee.id.value,
                    expiresAt: expiresAt,
                    role: role,
                },
                abortSignal: abortSignal,
            }),
            (outputModel) => channelInvitationFromCreation(outputModel, channel, invitee, expiresAt, role),
        );
    }

    /**
     * Gets a channel invitation by its identifier.
     *
     * @param channel - The channel the invitation belongs to.
     * @param inviteId - The identifier of the invitation.
     * @param abortSignal - The signal to abort the request.
     *
     * @returns The channel invitation.
     */
    export async function getInvitation(
        channel: Channel,
        inviteId: Identifier,
        abortSignal?: AbortSignal,
    ): ApiResult<ChannelInvitation> {
        return await handle(
            get<ChannelInvitationOutputModel>({
                uri: CHANNEL_INVITATION_ROUTE.replace(CHANNEL_ID_PARAM, channel.id.value.toString()).replace(
                    INVITATION_ID_PARAM,
                    inviteId.value.toString(),
                ),
                abortSignal: abortSignal,
            }),
            channelInvitationFromDto,
        );
    }

    /**
     * Gets a list of channel invitations.
     *
     * @param channel - The channel the invitations belong to.
     * @param pagination - The pagination request.
     * @param sort - The sort request.
     * @param after - The identifier of the last item in the previous page.
     * @param abortSignal - The signal to abort the request.
     *
     * @returns The list of channel invitations.
     */
    export async function getChannelInvitations(
        channel: Channel,
        pagination?: PaginationRequest,
        sort?: SortRequest,
        after?: Identifier,
        abortSignal?: AbortSignal,
    ): ApiResult<Pagination<ChannelInvitation>> {
        return await handle(
            get<ChannelInvitationsPaginatedOutputModel>({
                uri: buildQuery(CHANNEL_INVITATIONS_ROUTE, null, pagination, sort, false, after).replace(
                    CHANNEL_ID_PARAM,
                    channel.id.value.toString(),
                ),
                abortSignal: abortSignal,
            }),
            (outputModel) => ({
                items: outputModel.invitations.map(channelInvitationFromDto),
                info: outputModel.pagination,
            }),
        );
    }

    /**
     * Updates a channel invitation.
     *
     * @param invitation - The invitation to update.
     * @param role - The new role of the user in the channel.
     * @param expiresAt - The new expiration date of the invitation.
     * @param abortSignal - The signal to abort the request.
     *
     */
    export async function updateInvitation(
        invitation: ChannelInvitation,
        role: ChannelRole,
        expiresAt: Date,
        abortSignal?: AbortSignal,
    ): ApiResult<void> {
        return patch<ChannelInvitationUpdateInputModel>({
            uri: CHANNEL_INVITATION_ROUTE.replace(CHANNEL_ID_PARAM, invitation.channel.id.value.toString()).replace(
                INVITATION_ID_PARAM,
                invitation.id.value.toString(),
            ),
            requestBody: {
                role: role,
                expiresAt: expiresAt,
            },
            abortSignal: abortSignal,
        });
    }

    /**
     * Deletes a channel invitation.
     *
     * @param invitation - The invitation to delete.
     * @param abortSignal - The signal to abort the request.
     */
    export async function deleteInvitation(invitation: ChannelInvitation, abortSignal?: AbortSignal): ApiResult<void> {
        return await deleteRequest({
            uri: CHANNEL_INVITATION_ROUTE.replace(CHANNEL_ID_PARAM, invitation.channel.id.value.toString()).replace(
                INVITATION_ID_PARAM,
                invitation.id.value.toString(),
            ),
            abortSignal: abortSignal,
        });
    }

    /**
     * Gets a list of invitations for a user.
     *
     * @param user - The user to get the invitations for.
     * @param pagination - The pagination request.
     * @param sort - The sort request.
     * @param after - The identifier of the last item in the previous page.
     * @param abortSignal - The signal to abort the request.
     *
     * @returns The list of invitations.
     */
    export async function getUserInvitations(
        user: User,
        pagination?: PaginationRequest,
        sort?: SortRequest,
        after?: Identifier,
        abortSignal?: AbortSignal,
    ): ApiResult<Pagination<ChannelInvitation>> {
        return await handle(
            get<ChannelInvitationsPaginatedOutputModel>({
                uri: buildQuery(USER_INVITATIONS_ROUTE, null, pagination, sort, false, after)
                    .replace(CHANNEL_ID_PARAM, user.id.value.toString())
                    .replace(USER_ID_PARAM, user.id.value.toString()),
                abortSignal: abortSignal,
            }),
            (outputModel) => ({
                items: outputModel.invitations.map(channelInvitationFromDto),
                info: outputModel.pagination,
            }),
        );
    }

    /**
     * Accepts a channel invitation.
     *
     * @param invitation - The invitation to accept.
     * @param abortSignal - The signal to abort the request.
     *
     */
    export async function acceptInvitation(invitation: ChannelInvitation, abortSignal?: AbortSignal): ApiResult<void> {
        return await patch<InvitationAcceptInputModel>({
            uri: USER_INVITATION_ROUTE.replace(CHANNEL_ID_PARAM, invitation.channel.id.value.toString())
                .replace(INVITATION_ID_PARAM, invitation.id.value.toString())
                .replace(USER_ID_PARAM, invitation.invitee.id.value.toString()),
            requestBody: {
                status: ChannelInvitationStatus.ACCEPTED,
            },
            abortSignal: abortSignal,
        });
    }

    /**
     * Declines a channel invitation.
     *
     * @param invitation - The invitation to decline.
     * @param abortSignal - The signal to abort the request.
     */
    export async function declineInvitation(invitation: ChannelInvitation, abortSignal?: AbortSignal): ApiResult<void> {
        return await patch<InvitationAcceptInputModel>({
            uri: USER_INVITATION_ROUTE.replace(CHANNEL_ID_PARAM, invitation.channel.id.value.toString())
                .replace(INVITATION_ID_PARAM, invitation.id.value.toString())
                .replace(USER_ID_PARAM, invitation.invitee.id.value.toString()),
            requestBody: {
                status: ChannelInvitationStatus.REJECTED,
            },
            abortSignal: abortSignal,
        });
    }
}
