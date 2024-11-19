import { UserOutputModel } from '../users/UserOutputModel';
import { AccessTokenOutputModel } from './AccessTokenOutputModel';
import { RefreshTokenOutputModel } from './RefreshTokenOutputModel';

export interface CredentialsOutputModel {
    sessionID: number;
    user: UserOutputModel;
    accessToken: AccessTokenOutputModel;
    refreshToken: RefreshTokenOutputModel;
}
