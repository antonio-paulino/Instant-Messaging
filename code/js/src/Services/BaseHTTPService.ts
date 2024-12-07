import { ApiResult } from './media/Problem';
import { doAfterDelayWithResult } from '../Utils/Time';
import { failure, success } from '../Domain/Either';
import { delay } from './Utils';

type RequestBody<B> = B | null;

const JSON_CONTENT_TYPE = 'application/json';
const PROBLEM_CONTENT_TYPE = 'application/problem+json';
export const ABORT_STATUS = 0;

export namespace BaseHTTPService {
    enum HTTPMethod {
        GET = 'GET',
        POST = 'POST',
        PUT = 'PUT',
        DELETE = 'DELETE',
        PATCH = 'PATCH',
    }

    /**
     * The options for a request.
     *
     * [T] - The type of the request body.
     * [B] - The type of the response body.
     *
     * @param uri - The URI of the request.
     * @param requestBody - The body of the request.
     * @param fetchResBody - Whether to fetch the body of the response.
     * @param abortSignal - The signal to abort the request.
     */
    interface RequestOptions<T> {
        uri: string;
        requestBody?: RequestBody<T>;
        fetchResBody?: boolean;
        abortSignal?: AbortSignal;
    }

    /**
     * Fetches a response from the server.
     *
     * Cookies are included in the request.
     *
     * @param url - The URL of the request.
     * @param method - The HTTP method of the request.
     * @param body - The body of the request.
     * @param fetchResBody - Whether to fetch the body of the response.
     * @param abortSignal - The signal to abort the request.
     *
     */
    async function fetchResponse<T>(
        url: string,
        method: HTTPMethod,
        body?: BodyInit,
        fetchResBody: boolean = true,
        abortSignal?: AbortSignal,
    ): ApiResult<T> {
        if (process.env.NODE_ENV === 'development') {
            await delay(100 + Math.random() * 800);
        }
        const request: RequestInit = {
            method: method,
            headers: {
                'Content-Type': JSON_CONTENT_TYPE,
                Accept: `${JSON_CONTENT_TYPE}, ${PROBLEM_CONTENT_TYPE}`,
            },
            signal: abortSignal,
            body: body,
            credentials: 'include' as RequestCredentials,
        };

        let response: Response;

        try {
            response = await fetch(url, request);
        } catch {
            if (abortSignal?.aborted) {
                return failure({
                    status: ABORT_STATUS,
                    detail: 'Request was aborted.',
                });
            }
            return failure({
                status: 500,
                detail: 'Failed to connect to the server.',
            });
        }

        if (!response.ok) {
            return handleErrorResponse(response, request, url);
        }

        if (fetchResBody) {
            return success(await response.json());
        }

        return success(null as T);
    }

    /**
     * Handles an error response from the server.
     *
     * If the response status is 429, the request is retried after the delay specified in the Retry-After header.
     *
     * Otherwise, the response is returned as a failure.
     *
     * @param response - The response from the server.
     * @param request - The request that was sent to the server.
     * @param url - The URL of the request.
     * @private
     */
    async function handleErrorResponse<T>(response: Response, request: RequestInit, url: string): ApiResult<T> {
        if (response.status === 429) {
            const retryAfter = response.headers.get('Retry-After');
            if (retryAfter) {
                return await doAfterDelayWithResult(parseInt(retryAfter) * 1000, () =>
                    fetchResponse<T>(url, request.method as HTTPMethod, request.body, true),
                );
            }
        }
        if (response.status === 504) {
            return failure({
                status: response.status,
                detail: 'Could not connect to the server.',
            });
        }
        try {
            return failure(await response.json());
        } catch {
            return failure({
                status: response.status,
                detail: 'There was an error processing the request.',
            });
        }
    }

    /**
     * Performs a GET request to the server.
     *
     * @param requestOptions
     *
     * @returns The result of the request.
     */
    export async function get<T>(requestOptions: RequestOptions<T>): ApiResult<T> {
        return await fetchResponse<T>(
            requestOptions.uri,
            HTTPMethod.GET,
            null,
            requestOptions.fetchResBody,
            requestOptions.abortSignal,
        );
    }

    /**
     * Performs a POST request to the server.
     *
     * [T] - The type of the request body.
     *
     * [B] - The type of the response body.
     *
     * @param requestOptions - The options for the request.
     *
     * @returns The result of the request.
     */
    export async function post<T, B>(requestOptions: RequestOptions<T>): ApiResult<B> {
        return await fetchResponse<B>(
            requestOptions.uri,
            HTTPMethod.POST,
            requestOptions.requestBody ? JSON.stringify(requestOptions.requestBody) : null,
            requestOptions.fetchResBody,
            requestOptions.abortSignal,
        );
    }

    /**
     * Performs a PUT request to the server without fetching the response body.
     *
     * [T] - The type of the request body.
     *
     * @param requestOptions - The options for the request.
     */
    export async function put<T>(requestOptions: RequestOptions<T>): ApiResult<void> {
        return await fetchResponse<void>(
            requestOptions.uri,
            HTTPMethod.PUT,
            requestOptions.requestBody ? JSON.stringify(requestOptions.requestBody) : null,
            false,
            requestOptions.abortSignal,
        );
    }

    /**
     * Performs a DELETE request to the server.
     *
     * @param requestOptions - The options for the request.
     *
     * @returns The result of the request.
     */
    export async function deleteRequest(requestOptions: RequestOptions<void>): ApiResult<void> {
        return await fetchResponse<void>(
            requestOptions.uri,
            HTTPMethod.DELETE,
            null,
            false,
            requestOptions.abortSignal,
        );
    }

    /**
     * Performs a PATCH request to the server.
     *
     * [T] - The type of the request body.
     *
     * @param requestOptions - The options for the request.
     *
     * @returns The result of the request.
     */
    export async function patch<T>(requestOptions: RequestOptions<T>): ApiResult<void> {
        return await fetchResponse<void>(
            requestOptions.uri,
            HTTPMethod.PATCH,
            requestOptions.requestBody ? JSON.stringify(requestOptions.requestBody) : null,
            false,
            requestOptions.abortSignal,
        );
    }
}
