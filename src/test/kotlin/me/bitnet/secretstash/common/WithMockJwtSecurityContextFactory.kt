package me.bitnet.secretstash.common

import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.security.test.context.support.WithSecurityContextFactory
import java.time.Instant

class WithMockJwtSecurityContextFactory : WithSecurityContextFactory<WithMockJwt> {
    override fun createSecurityContext(annotation: WithMockJwt): SecurityContext {
        val authorities = annotation.roles.map { SimpleGrantedAuthority("ROLE_$it") }

        // Build the claims map
        val claims =
            mutableMapOf<String, Any>(
                "sub" to annotation.subject,
                "preferred_username" to annotation.username,
                "email" to annotation.email,
            )

        // Add realm_access with roles
        claims["realm_access"] = mapOf("roles" to annotation.roles.toList())

        // Add additional custom claims
        annotation.claims.forEach { claim ->
            val parts = claim.split(":", limit = 2)
            if (parts.size == 2) {
                claims[parts[0]] = parts[1]
            }
        }

        // Create a JWT token
        val jwt =
            Jwt
                .withTokenValue("token")
                .header("alg", "RS256")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .claims { c -> c.putAll(claims) }
                .build()

        // Create the authentication token
        val authentication = JwtAuthenticationToken(jwt, authorities)

        // Create and populate the security context
        val context = SecurityContextHolder.createEmptyContext()
        context.authentication = authentication

        return context
    }
}
