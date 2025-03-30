package me.bitnet.secretstash.util.ratelimiter

/**
 * Annotation to apply rate limiting to REST endpoints or controller classes.
 *
 * Rate limiting restricts the number of requests a user can make within a specified time window.
 * When applied to a class, all methods in that class will use the same rate limit settings.
 * When applied to a method, those settings override any class-level settings.
 *
 * Both parameters must be positive (greater than zero) to take effect.
 * Any zero or negative value will cause the system to use the configured defaults
 * from application properties (rate.limit.default-limit and rate.limit.window-seconds).
 *
 * @property value The maximum number of requests allowed within the time window.
 *                 Must be positive to override the default configuration value.
 * @property windowSeconds The time window in seconds during which the requests are counted.
 *                         Must be positive to override the default configuration value.
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class RateLimit(
    val value: Int = -1,
    val windowSeconds: Int = -1,
)
