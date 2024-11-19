export class IdentifierValidationError {
    private readonly defaultMessage: string;

    constructor(defaultMessage: string = 'Identifier is invalid') {
        this.defaultMessage = defaultMessage;
    }

    static NegativeValue = new (class extends IdentifierValidationError {
        constructor() {
            super('Identifier cannot be negative');
        }
    })();

    toErrorMessage(): string {
        return this.defaultMessage;
    }
}
