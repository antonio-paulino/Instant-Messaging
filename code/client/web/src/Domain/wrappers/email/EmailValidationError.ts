export abstract class EmailValidationError {
    private readonly defaultMessage: string;

    protected constructor(defaultMessage: string = 'Email is invalid') {
        this.defaultMessage = defaultMessage;
    }

    static Blank = new (class extends EmailValidationError {
        constructor() {
            super('Email cannot be blank');
        }
    })();

    static InvalidFormat = new (class extends EmailValidationError {
        constructor() {
            super('Email has an invalid format');
        }
    })();

    static InvalidLength = class extends EmailValidationError {
        min: number;
        max: number;

        constructor(min: number, max: number) {
            super(`Email must be between ${min} and ${max} characters`);
            this.min = min;
            this.max = max;
        }
    };

    toErrorMessage(): string {
        return this.defaultMessage;
    }
}
