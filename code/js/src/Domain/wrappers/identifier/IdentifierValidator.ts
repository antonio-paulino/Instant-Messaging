import { IdentifierValidationError } from './IdentifierValidationError';

export class IdentifierValidator {
    validate(value: number): IdentifierValidationError[] {
        const errors: IdentifierValidationError[] = [];

        if (value < 0) {
            errors.push(IdentifierValidationError.NegativeValue);
        }

        if (errors.length > 0) {
            return errors;
        }

        return [];
    }
}
