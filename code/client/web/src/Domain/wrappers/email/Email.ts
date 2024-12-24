import { EmailValidator } from './EmailValidator';

export class Email {
    private static validator = new EmailValidator();

    constructor(public readonly value: string) {
        const validation = Email.validator.validate(value);
        if (validation.length > 0) {
            throw new Error(validation.join('\n'));
        }
    }

    static fromString(value: string): Email {
        return new Email(value);
    }

    toString(): string {
        return this.value;
    }
}
