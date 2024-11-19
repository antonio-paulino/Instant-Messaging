import { NameValidator } from './NameValidator';

export class Name {
    private static readonly validator = new NameValidator();

    constructor(public readonly value: string) {
        const validation = Name.validator.validate(value);
        if (validation.length > 0) {
            throw new Error(validation.join('\n'));
        }
    }

    static fromString(value: string): Name {
        return new Name(value);
    }

    toString(): string {
        return this.value;
    }
}
