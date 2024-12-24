package im.api.middlewares.authentication

/**
 * Annotation that marks a Controller or Method as requiring authentication.
 *
 * If a Controller has the Authenticated annotation, all methods in that Controller will require authentication.
 *
 * If a Method has the Authenticated annotation, only that method will require authentication.
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
annotation class Authenticated
