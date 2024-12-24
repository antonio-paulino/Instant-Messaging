export abstract class NameValidationError {
    private readonly defaultMessage: string;

    protected constructor(defaultMessage: string = 'Name is invalid') {
        this.defaultMessage = defaultMessage;
    }

    static Blank = new (class extends NameValidationError {
        constructor() {
            super('Name cannot be blank');
        }
    })();

    static InvalidLength(min: number, max: number) {
        return new (class extends NameValidationError {
            constructor() {
                super(`Name must be between ${min} and ${max} characters`);
            }
        })();
    }

    toErrorMessage(): string {
        return this.defaultMessage;
    }
}
