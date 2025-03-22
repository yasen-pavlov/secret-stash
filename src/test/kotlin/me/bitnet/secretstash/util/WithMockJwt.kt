package me.bitnet.secretstash.util

import org.springframework.security.test.context.support.WithSecurityContext

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
@WithSecurityContext(factory = WithMockJwtSecurityContextFactory::class)
annotation class WithMockJwt(
    val subject: String = "0c47a356-edb2-47ae-923c-9f2902c622be",
    val username: String = "test-user",
    val email: String = "test@example.com",
    val roles: Array<String> = ["USER"],
    val claims: Array<String> = [], // Format: "key:value"
)
