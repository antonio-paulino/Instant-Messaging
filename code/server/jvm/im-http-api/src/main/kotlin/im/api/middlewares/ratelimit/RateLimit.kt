package im.api.middlewares.ratelimit

/**
 * Annotation that marks a Controller or Method as requiring rate limiting.
 *
 * If a Controller has the RateLimit annotation, all methods in that controller will require rate limiting.
 *
 * If a Method has the RateLimit annotation, only that method will require rate limiting.
 *
 * Used to limit the number of requests a client can make in a given time window.
 *
 * @param limitSeconds The number of seconds to limit requests.
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
annotation class RateLimit(val limitSeconds: Int)
