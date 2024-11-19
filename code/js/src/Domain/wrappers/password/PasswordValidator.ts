import { PasswordValidationError } from './PasswordValidationError';

export class PasswordValidator {
    private static readonly MIN_LENGTH = 8;
    private static readonly MAX_LENGTH = 80;
    private static readonly MIN_LOWERCASE = 1;
    private static readonly MIN_UPPERCASE = 1;
    private static readonly MIN_DIGITS = 1;

    constructor(
        readonly minLength: number = PasswordValidator.MIN_LENGTH,
        readonly maxLength: number = PasswordValidator.MAX_LENGTH,
        readonly minLowercase: number = PasswordValidator.MIN_LOWERCASE,
        readonly minUppercase: number = PasswordValidator.MIN_UPPERCASE,
        readonly minDigits: number = PasswordValidator.MIN_DIGITS,
    ) {}

    validate(value: string): PasswordValidationError[] {
        const errors: PasswordValidationError[] = [];

        if (value.trim() === '') {
            errors.push(PasswordValidationError.Blank);
        }

        if (/\s/.test(value)) {
            errors.push(PasswordValidationError.CannotContainWhitespace);
        }

        if (value.length < this.minLength || value.length > this.maxLength) {
            errors.push(
                PasswordValidationError.InvalidLength(
                    this.minLength,
                    this.maxLength,
                ),
            );
        }

        if ((value.match(/[a-z]/g) || []).length < this.minLowercase) {
            errors.push(
                PasswordValidationError.NotEnoughLowercaseLetters(
                    this.minLowercase,
                ),
            );
        }

        if ((value.match(/[A-Z]/g) || []).length < this.minUppercase) {
            errors.push(
                PasswordValidationError.NotEnoughUppercaseLetters(
                    this.minUppercase,
                ),
            );
        }

        if ((value.match(/\d/g) || []).length < this.minDigits) {
            errors.push(
                PasswordValidationError.NotEnoughDigits(this.minDigits),
            );
        }

        if (errors.length > 0) {
            return errors;
        }

        return [];
    }
}

export async function checkPwned(password: string): Promise<number> {
    const hash = await sha1(password);
    const prefix = hash.substring(0, 5);
    const suffix = hash.substring(5);
    const response = await fetch(
        `https://api.pwnedpasswords.com/range/${prefix}`,
    );
    const body = await response.text();
    const lines = body.split('\n');
    const found = lines.find((line) => line.startsWith(suffix));
    return found ? parseInt(found.split(':')[1]) : 0;
}

async function sha1(data: string): Promise<string> {
    const buffer = new TextEncoder().encode(data);
    const hashBuffer = await crypto.subtle.digest('SHA-1', buffer);
    const hashArray = Array.from(new Uint8Array(hashBuffer));
    return hashArray
        .map((b) => b.toString(16).padStart(2, '0'))
        .join('')
        .toUpperCase();
}
