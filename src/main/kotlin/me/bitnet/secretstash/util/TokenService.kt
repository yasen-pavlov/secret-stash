package me.bitnet.secretstash.util

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class TokenService {
    fun getCurrentUserId(): UUID = UUID.fromString(getJwt().subject)

    private fun getJwt(): Jwt {
        val authentication = SecurityContextHolder.getContext().authentication
        return authentication.principal as Jwt
    }
}
