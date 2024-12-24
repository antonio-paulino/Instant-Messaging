export abstract class MessageValidationError {
    protected constructor(public readonly defaultMessage: string = 'Invalid message') {}

    static ContentBlank = new (class extends MessageValidationError {
        constructor() {
            super('Message content cannot be blank');
        }
    })();

    static ContentLength = class extends MessageValidationError {
        constructor(
            public readonly min: number,
            public readonly max: number,
        ) {
            super(`Message content must be between ${min} and ${max} characters`);
        }
    };

    toErrorMessage(): string {
        return this.defaultMessage;
    }
}
