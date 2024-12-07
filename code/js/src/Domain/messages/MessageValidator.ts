import { MessageValidationError } from './MessageValidationError';

export class MessageValidator {
    private static readonly MIN_LENGTH = 1;
    private static readonly MAX_LENGTH = 300;

    constructor(
        private readonly minLength: number = MessageValidator.MIN_LENGTH,
        private readonly maxLength: number = MessageValidator.MAX_LENGTH,
    ) {}

    validate(message: string): MessageValidationError[] {
        const errors: MessageValidationError[] = [];

        if (message.trim().length === 0) {
            errors.push(MessageValidationError.ContentBlank);
        }

        if (message.length < this.minLength || message.length > this.maxLength) {
            errors.push(new MessageValidationError.ContentLength(this.minLength, this.maxLength));
        }

        if (errors.length > 0) {
            return errors;
        }

        return [];
    }
}
