package uk.gov.hmcts.reform.civil.config;

import com.microsoft.applicationinsights.TelemetryClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

/**
 * Built-in feature which saves service's Open Api specs in temporary directory.
 * Each travis run on master should automatically save and upload (if updated) documentation.
 */

@WebMvcTest
@ActiveProfiles("integration-test")
class OpenAPIPublisherTest {

    private MockMvc mvc;
    private final WebApplicationContext wac;

    @MockBean
    private TelemetryClient telemetryClient;

    OpenAPIPublisherTest(WebApplicationContext wac) {
        this.wac = wac;
    }

    @BeforeEach
    void setUp() {
        mvc = webAppContextSetup(wac).build();
    }

    @DisplayName("Generate Open API documentation")
    @Test
    @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
    void generateDocs() throws Exception {
        byte[] specs = mvc.perform(get("/v3/api-docs"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsByteArray();

        if (!Files.exists(Paths.get("/tmp/"))) {
            Files.createDirectories(Paths.get("/tmp/"));    // needed on Windows systems as the directory won't exist by default
        }
        try (OutputStream outputStream = Files.newOutputStream(Paths.get("/tmp/openapi-specs.json"))) {
            outputStream.write(specs);
        }
    }
}
