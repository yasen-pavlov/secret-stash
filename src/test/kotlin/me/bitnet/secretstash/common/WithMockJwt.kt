package me.bitnet.secretstash.common

import org.springframework.security.test.context.support.WithSecurityContext

/**
 * Custom test annotation for creating a mock JWT authentication context.
 * This annotation simplifies testing of secured endpoints by providing
 * a pre-configured JWT token with customizable claims.
 *
 * Usage:
 * @WithMockJwt
 * fun testSecuredEndpoint() { ... }
 *
 * @see WithSecurityContext
 * @see WithMockJwtSecurityContextFactory
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
@WithSecurityContext(factory = WithMockJwtSecurityContextFactory::class)
annotation class WithMockJwt(
    /**
     * The subject claim (sub) for the JWT token.
     * Defaults to a pre-defined UUID string.
     */
    val subject: String = "0c47a356-edb2-47ae-923c-9f2902c622be",
    /**
     * The username claim for the JWT token.
     * Defaults to "test-user".
     */
    val username: String = "test-user",
    /**
     * The email claim for the JWT token.
     * Defaults to "test@example.com".
     */
    val email: String = "test@example.com",
    /**
     * The roles to be included in the JWT token.
     * Defaults to a single role: "USER".
     */
    val roles: Array<String> = ["USER"],
    /**
     * Additional custom claims to include in the JWT token.
     * Should be provided in "key:value" format.
     * Example: ["tenant_id:123", "feature_flags:premium"]
     */
    val claims: Array<String> = [], // Format: "key:value"
)
