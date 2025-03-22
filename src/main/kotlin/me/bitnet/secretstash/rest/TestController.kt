package me.bitnet.secretstash.rest

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TestController {
    @GetMapping("/secured/test")
    @PreAuthorize("hasRole('USER')")
    fun securedEndpoint(
        @AuthenticationPrincipal jwt: Jwt,
    ): String = "user id: ${jwt.subject ?: "Unknown User"}"
}
