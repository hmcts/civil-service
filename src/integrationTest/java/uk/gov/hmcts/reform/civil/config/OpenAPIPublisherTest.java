package uk.gov.hmcts.reform.civil.config;

import com.microsoft.applicationinsights.TelemetryClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.civil.callback.CallbackHandlerFactory;
import uk.gov.hmcts.reform.civil.controllers.RootController;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.dashboard.repositories.DashboardNotificationsRepository;
import uk.gov.hmcts.reform.dashboard.repositories.NotificationActionRepository;
import uk.gov.hmcts.reform.dashboard.repositories.ScenarioRepository;
import uk.gov.hmcts.reform.dashboard.repositories.TaskItemTemplateRepository;
import uk.gov.hmcts.reform.dashboard.repositories.TaskListRepository;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Built-in feature which saves service's Open Api specs in temporary directory.
 * Each travis run on master should automatically save and upload (if updated) documentation.
 */

@WebMvcTest(controllers = RootController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("integration-test")
@Disabled("Temporarily disabled during Spring Boot 4 migration: OpenAPI full-context startup requires legacy Feign clients")
class OpenAPIPublisherTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private TelemetryClient telemetryClient;

    @MockitoBean
    private ScenarioRepository scenarioRepository;

    @MockitoBean
    private NotificationActionRepository notificationActionRepository;

    @MockitoBean
    private TaskListRepository taskListRepository;

    @MockitoBean
    private DashboardNotificationsRepository dashboardNotificationsRepository;

    @MockitoBean
    private TaskItemTemplateRepository taskItemTemplateRepository;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private CoreCaseDataApi coreCaseDataApi;

    @MockitoBean
    private CallbackHandlerFactory callbackHandlerFactory;

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
