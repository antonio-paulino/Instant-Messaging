export abstract class PasswordValidationError {
    private readonly defaultMessage: string;

    protected constructor(defaultMessage: string = 'Password is invalid') {
        this.defaultMessage = defaultMessage;
    }

    static Blank = new (class extends PasswordValidationError {
        constructor() {
            super('Password cannot be blank');
        }
    })();

    static InvalidLength(min: number, max: number) {
        return new (class extends PasswordValidationError {
            constructor() {
                super(`Password must be between ${min} and ${max} characters`);
            }
        })();
    }

    static NotEnoughUppercaseLetters(min: number) {
        return new (class extends PasswordValidationError {
            constructor() {
                super(`Password must contain at least ${min} uppercase letter(s)`);
            }
        })();
    }

    static NotEnoughLowercaseLetters(min: number) {
        return new (class extends PasswordValidationError {
            constructor() {
                super(`Password must contain at least ${min} lowercase letter(s)`);
            }
        })();
    }

    static NotEnoughDigits(min: number) {
        return new (class extends PasswordValidationError {
            constructor() {
                super(`Password must contain at least ${min} digit(s)`);
            }
        })();
    }

    static Insecure = new (class extends PasswordValidationError {
        constructor() {
            super('This password is too insecure, choose a stronger one');
        }
    })();

    static CannotContainWhitespace = new (class extends PasswordValidationError {
        constructor() {
            super('Password cannot contain whitespace');
        }
    })();

    toErrorMessage(): string {
        return this.defaultMessage;
    }
}
