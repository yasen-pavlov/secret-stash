package me.bitnet.secretstash.rest

import me.bitnet.secretstash.util.TestcontainersConfiguration
import me.bitnet.secretstash.util.WithMockJwt
import org.hamcrest.CoreMatchers.containsString
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Import(TestcontainersConfiguration::class)
@SpringBootTest
@AutoConfigureMockMvc
class TestControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    @WithMockJwt(roles = ["USER"])
    fun `should access test endpoint when user has USER role`() {
        mockMvc
            .perform(get("/secured/test"))
            .andExpect(status().isOk)
            .andExpect(content().string(containsString("user id: 0c47a356-edb2-47ae-923c-9f2902c622be")))
    }

    @Test
    @WithMockJwt(roles = ["OTHER"])
    fun `should not access test endpoint when user lacks USER role`() {
        mockMvc
            .perform(get("/secured/test"))
            .andExpect(status().isForbidden)
    }

    @Test
    fun `should not access test endpoint when not authenticated`() {
        mockMvc
            .perform(get("/secured/test"))
            .andExpect(status().isUnauthorized)
    }
}
