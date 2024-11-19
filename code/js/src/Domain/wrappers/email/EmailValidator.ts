import { EmailValidationError } from './EmailValidationError';

export class EmailValidator {
    private static readonly MAX_EMAIL_LENGTH = 50;
    private static readonly MIN_EMAIL_LENGTH = 8;
    private static readonly EMAIL_REGEX =
        /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Z|a-z]{2,}$/;

    public constructor(
        readonly maxEmailLength: number = EmailValidator.MAX_EMAIL_LENGTH,
        readonly minEmailLength: number = EmailValidator.MIN_EMAIL_LENGTH,
        readonly emailRegex: RegExp = EmailValidator.EMAIL_REGEX,
    ) {}

    validate(email: string): EmailValidationError[] {
        const errors: EmailValidationError[] = [];

        if (email.trim() === '') {
            errors.push(EmailValidationError.Blank);
        }

        if (!email.match(this.emailRegex)) {
            errors.push(EmailValidationError.InvalidFormat);
        }

        if (
            email.length < this.minEmailLength ||
            email.length > this.maxEmailLength
        ) {
            errors.push(
                new EmailValidationError.InvalidLength(
                    this.minEmailLength,
                    this.maxEmailLength,
                ),
            );
        }

        if (errors.length > 0) {
            return errors;
        }

        return [];
    }
}
