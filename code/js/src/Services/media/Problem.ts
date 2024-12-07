import { Either, failure, success } from '../../Domain/Either';

/**
 * Represents a problem response from the API.
 *
 * A problem response is returned when an error occurs during the processing of a request.
 *
 * @param type The type of the problem.
 * @param title The title of the problem.
 * @param status The HTTP status code of the problem.
 * @param detail The detailed description of the problem.
 */
interface Problem {
    type: string;
    title: string;
    status: number;
    detail: string;
}

export type ProblemResponse = Problem | UnexpectedProblem | InputValidationProblem;

export type ApiResult<T> = Promise<Either<ProblemResponse, T>>;

/**
 * Handles the response from the API, transforming the response into a value of type R or propagating the problem.
 *
 * @param response The response from the API.
 * @param transform The transformation function.
 */
export async function handle<T, R>(
    response: ApiResult<T>,
    transform: (t: T) => R,
): Promise<Either<Problem | UnexpectedProblem | InputValidationProblem, R>> {
    const res = await response;
    if (res.isFailure()) {
        return failure(res.getLeft()!);
    } else {
        return success(transform(res.getRight()!));
    }
}

/**
 * Represents a problem response from the API.
 */
export interface ServiceProblem extends Problem {}

function isServiceProblem(obj: any): obj is ServiceProblem {
    return obj.type !== undefined && obj.title !== undefined && obj.status !== undefined && obj.detail !== undefined;
}

export interface UnexpectedProblem {
    status: number;
    detail: string;
}

function isUnexpectedProblem(obj: any): obj is UnexpectedProblem {
    return obj.status !== undefined && obj.detail !== undefined;
}

/**
 * Represents an input validation problem response from the API.
 */
export interface InputValidationProblem extends Problem {
    errors: string[];
}

function isInputValidationProblem(obj: any): obj is InputValidationProblem {
    return (
        obj.type !== undefined &&
        obj.title !== undefined &&
        obj.status !== undefined &&
        obj.detail !== undefined &&
        obj.errors !== undefined
    );
}
