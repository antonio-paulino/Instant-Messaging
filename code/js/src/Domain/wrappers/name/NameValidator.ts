import { NameValidationError } from './NameValidationError';

export class NameValidator {
    private static readonly MAX_NAME_LENGTH = 30;
    private static readonly MIN_NAME_LENGTH = 3;

    constructor(
        readonly maxNameLength: number = NameValidator.MAX_NAME_LENGTH,
        readonly minNameLength: number = NameValidator.MIN_NAME_LENGTH,
    ) {}

    validate(value: string): NameValidationError[] {
        const errors: NameValidationError[] = [];

        if (value.trim() === '') {
            errors.push(NameValidationError.Blank);
        }

        if (
            value.length < this.minNameLength ||
            value.length > this.maxNameLength
        ) {
            errors.push(
                NameValidationError.InvalidLength(
                    this.minNameLength,
                    this.maxNameLength,
                ),
            );
        }

        return errors.length > 0 ? errors : [];
    }
}
