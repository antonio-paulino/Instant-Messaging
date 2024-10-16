package im.domain

/**
 * Represents a value of one of two possible types (a disjoint union).
 * Instances of [Either] are either an instance of [Left] or [Right].
 *
 * Either is used to represent the result of a computation that may fail with a known error type
 * or succeed with a correct value.
 */
sealed class Either<out L, out R> {
    data class Left<out L>(
        val value: L,
    ) : Either<L, Nothing>()

    data class Right<out R>(
        val value: R,
    ) : Either<Nothing, R>()
}

/**
 * Returns the value from this [Either.Right]
 */
fun <R> success(value: R) = Either.Right(value)

/**
 * Returns the value from this [Either.Left]
 */
fun <L> failure(error: L) = Either.Left(error)

typealias Success<S> = Either.Right<S>
typealias Failure<F> = Either.Left<F>
