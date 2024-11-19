import { IdentifierValidator } from './IdentifierValidator';
import { IdentifierOutputModel } from '../../../UI/Components/Providers/Events';

export class Identifier {
    private static validator = new IdentifierValidator();

    constructor(public readonly value: number) {
        const validation = Identifier.validator.validate(value);
        if (validation.length > 0) {
            throw new Error(validation.join('\n'));
        }
    }

    toString(): string {
        return this.value.toString();
    }

    static fromDto(dto: IdentifierOutputModel): Identifier {
        return new Identifier(dto.id);
    }
}
