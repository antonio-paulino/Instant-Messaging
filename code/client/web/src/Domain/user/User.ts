import { Identifier } from '../wrappers/identifier/Identifier';
import { Name } from '../wrappers/name/Name';
import { Email } from '../wrappers/email/Email';
import { UserOutputModel } from '../../Dto/output/users/UserOutputModel';
import { UserCreationOutputModel } from '../../Dto/output/users/UserCreationOutputModel';

/**
 * Represents a user.
 */
export interface User {
    id: Identifier;
    name: Name;
    email: Email;
}

export function userFromDto(dto: UserOutputModel): User {
    return {
        id: new Identifier(dto.id),
        name: new Name(dto.name),
        email: new Email(dto.email),
    };
}

export function userFromCreation(dto: UserCreationOutputModel, name: Name, email: Email): User {
    return {
        id: new Identifier(dto.id),
        name,
        email,
    };
}
