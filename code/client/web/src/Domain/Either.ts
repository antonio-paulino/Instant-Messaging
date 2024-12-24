/**
 * Represents a value of one of two possible types (a disjoint union).
 * Instances of Either are either an instance of Left or Right.
 *
 * Either is used to represent the result of a computation that may fail with a known error type
 * or succeed with a correct value.
 */
export class Either<L, R> {
    private constructor(
        private readonly left?: L,
        private readonly right?: R,
    ) {}

    static Left<L>(value: L): Either<L, never> {
        return new Either<L, never>(value);
    }

    static Right<R>(value: R): Either<never, R> {
        // @ts-ignore
        return new Either<never, R>(undefined, value);
    }

    isFailure(): this is Failure<L> {
        return this.left !== undefined;
    }

    isSuccess(): this is Success<R> {
        return this.right !== undefined;
    }

    getLeft(): L | undefined {
        return this.left;
    }

    getRight(): R | undefined {
        return this.right;
    }
}

export const success = <R>(value: R): Either<never, R> => Either.Right(value);
export const failure = <L>(error: L): Either<L, never> => Either.Left(error);

export type Success<S> = Either<never, S>;
export type Failure<F> = Either<F, never>;
