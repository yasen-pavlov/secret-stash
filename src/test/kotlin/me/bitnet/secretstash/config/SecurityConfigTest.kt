package me.bitnet.secretstash.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.boot.test.system.OutputCaptureExtension
import org.springframework.security.oauth2.jwt.Jwt

@ExtendWith(MockitoExtension::class, OutputCaptureExtension::class)
class SecurityConfigTest {
    private val securityConfig = SecurityConfig()

    @Test
    fun `should convert JWT with roles to authorities`() {
        // Arrange
        val jwt = createJwtWithRoles(listOf("admin", "user"))
        val converter = securityConfig.jwtAuthenticationConverter()

        // Act
        val authentication = converter.convert(jwt)

        // Assert
        assertThat(authentication).isNotNull
        assertThat(authentication!!.authorities).hasSize(2)
        assertThat(authentication.authorities)
            .extracting("authority")
            .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_USER")
    }

    @Test
    fun `should convert JWT with empty roles to empty authorities list`() {
        // Arrange
        val jwt = createJwtWithRoles(emptyList())
        val converter = securityConfig.jwtAuthenticationConverter()

        // Act
        val authentication = converter.convert(jwt)

        // Assert
        assertThat(authentication).isNotNull
        assertThat(authentication!!.authorities).isEmpty()
    }

    @Test
    fun `should convert JWT with missing realm_access to empty authorities list`() {
        // Arrange
        val jwt = createJwtWithoutRealmAccess()
        val converter = securityConfig.jwtAuthenticationConverter()

        // Act
        val authentication = converter.convert(jwt)

        // Assert
        assertThat(authentication).isNotNull
        assertThat(authentication!!.authorities).isEmpty()
    }

    @Test
    fun `should handle null roles in realm_access`() {
        // Arrange
        val jwt = createJwtWithNullRoles()
        val converter = securityConfig.jwtAuthenticationConverter()

        // Act
        val authentication = converter.convert(jwt)

        // Assert
        assertThat(authentication).isNotNull
        assertThat(authentication!!.authorities).isEmpty()
    }

    @Test
    fun `should uppercase role names when converting to authorities`() {
        // Arrange
        val jwt = createJwtWithRoles(listOf("admin", "super-user", "reader"))
        val converter = securityConfig.jwtAuthenticationConverter()

        // Act
        val authentication = converter.convert(jwt)

        // Assert
        assertThat(authentication).isNotNull
        assertThat(authentication!!.authorities)
            .extracting("authority")
            .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_SUPER-USER", "ROLE_READER")
    }

    @Test
    fun `should handle type mismatch in realm_access roles`() {
        // Arrange
        val jwt = createJwtWithInvalidRolesType()
        val converter = securityConfig.jwtAuthenticationConverter()

        // Act
        val authentication = converter.convert(jwt)

        // Assert
        assertThat(authentication).isNotNull
        assertThat(authentication!!.authorities).isEmpty()
    }

    @Test
    fun `should handle type mismatch in realm_access itself`() {
        // Arrange
        val jwt = mock(Jwt::class.java)
        `when`(jwt.claims).thenReturn(mapOf<String, Any>("realm_access" to "not-a-map"))
        val converter = securityConfig.jwtAuthenticationConverter()

        // Act
        val authentication = converter.convert(jwt)

        // Assert
        assertThat(authentication).isNotNull
        assertThat(authentication!!.authorities).isEmpty()
    }

    private fun createJwtWithRoles(roles: List<String>): Jwt {
        val jwt = mock(Jwt::class.java)
        val claims =
            mapOf<String, Any>(
                "realm_access" to
                    mapOf<String, Any>(
                        "roles" to roles,
                    ),
            )
        `when`(jwt.claims).thenReturn(claims)
        return jwt
    }

    private fun createJwtWithoutRealmAccess(): Jwt {
        val jwt = mock(Jwt::class.java)
        `when`(jwt.claims).thenReturn(emptyMap<String, Any>())
        return jwt
    }

    private fun createJwtWithNullRoles(): Jwt {
        val jwt = mock(Jwt::class.java)
        val claims =
            mapOf<String, Any>(
                "realm_access" to
                    mapOf<String, Any?>(
                        "roles" to null,
                    ),
            )
        `when`(jwt.claims).thenReturn(claims)
        return jwt
    }

    private fun createJwtWithInvalidRolesType(): Jwt {
        val jwt = mock(Jwt::class.java)
        val claims =
            mapOf<String, Any>(
                "realm_access" to
                    mapOf<String, Any>(
                        "roles" to "not-a-list",
                    ),
            )
        `when`(jwt.claims).thenReturn(claims)
        return jwt
    }
}
