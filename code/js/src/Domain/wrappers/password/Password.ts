import { PasswordValidator } from './PasswordValidator';

export class Password {
    private static validator = new PasswordValidator();

    constructor(public readonly value: string) {
        const validation = Password.validator.validate(value);
        if (validation.length > 0) {
            throw new Error(validation.join('\n'));
        }
    }

    static fromString(value: string): Password {
        return new Password(value);
    }

    toString(): string {
        return this.value;
    }
}
