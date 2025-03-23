package me.bitnet.secretstash.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.OAuthFlow
import io.swagger.v3.oas.models.security.OAuthFlows
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig(
    @Value("\${keycloak.auth-server-url}")
    private val authServerUrl: String,
    @Value("\${keycloak.realm}")
    private val realm: String,
) {
    @Bean
    fun customOpenAPI(): OpenAPI =
        OpenAPI()
            .info(
                Info()
                    .title("Secret Stash Service API Documentation")
                    .version("1.0")
                    .description(
                        """
                        The Secret Stash Service API enables secure storage and management of confidential notes. \
                        Users can create, retrieve, update, and delete personal notes through a simple interface. \
                        This service is designed for storing sensitive information with proper authentication, \
                        ensuring that your private content remains protected and accessible only to you.
                        """.trimIndent(),
                    ),
            ).components(
                Components()
                    .addSecuritySchemes("keycloak-oauth", createKeycloakPasswordFlowScheme()),
            ).addSecurityItem(SecurityRequirement().addList("keycloak-oauth"))

    private fun createKeycloakPasswordFlowScheme(): SecurityScheme {
        val tokenUrl = "$authServerUrl/realms/$realm/protocol/openid-connect/token"

        return SecurityScheme()
            .type(SecurityScheme.Type.OAUTH2)
            .flows(
                OAuthFlows()
                    .password(
                        OAuthFlow()
                            .tokenUrl(tokenUrl),
                    ),
            )
    }
}
