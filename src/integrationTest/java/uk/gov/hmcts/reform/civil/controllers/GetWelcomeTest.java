package uk.gov.hmcts.reform.civil.controllers;

import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(properties = {
    "spring.datasource.driverClassName=org.h2.Driver",
    "spring.datasource.url=jdbc:h2:mem:getwelcome;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
    "spring.datasource.jdbcUrl=jdbc:h2:mem:getwelcome;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.flyway.enabled=false"
})
public class GetWelcomeTest extends BaseIntegrationTest {

    @DisplayName("Should welcome upon root request with 200 response code")
    @Test
    @SneakyThrows
    public void welcomeRootEndpoint() {
        MvcResult response = mockMvc.perform(get("/")).andExpect(status().isOk()).andReturn();

        assertThat(response.getResponse().getContentAsString()).startsWith("Welcome");
    }
}
