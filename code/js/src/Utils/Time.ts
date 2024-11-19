/**
 * Returns a promise that executes the given function after the given delay.
 *
 * @param delay The delay in milliseconds.
 * @param func The function to execute.
 */
export function doAfterDelayWithResult<T>(
    delay: number,
    func: () => Promise<T>,
): Promise<T> {
    return new Promise<T>((resolve) => {
        setTimeout(() => {
            resolve(func());
        }, delay);
    });
}

export function doAfterDelay(delay: number, func: () => void): NodeJS.Timeout {
    return setTimeout(func, delay);
}
