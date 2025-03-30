package me.bitnet.secretstash.util.auth

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class TokenService {
    /**
     * Retrieves the current authenticated user's ID from the JWT token.
     * Extracts the subject claim from the JWT and converts it to UUID.
     *
     * @return UUID representation of the current user's ID
     */
    fun getCurrentUserId(): UUID = UUID.fromString(getJwt().subject)

    private fun getJwt(): Jwt {
        val authentication = SecurityContextHolder.getContext().authentication
        return authentication.principal as Jwt
    }
}
