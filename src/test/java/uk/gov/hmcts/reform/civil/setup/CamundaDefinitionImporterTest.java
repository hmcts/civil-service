package uk.gov.hmcts.reform.civil.setup;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGeneratorFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CamundaDefinitionImporterTest {

    private static final String DEPLOYMENT_URL = "http://camunda/engine-rest/deployment/create";

    @TempDir
    private Path workspace;

    @Test
    void retriesRetryableCamundaDeploymentFailures() throws Exception {
        CamundaImportConfiguration configuration = configurationWithDefinition("acknowledge_claim.bpmn");
        RestTemplate restTemplate = mock(RestTemplate.class);
        HttpServerErrorException camundaNotReady = serverError();

        when(restTemplate.postForEntity(eq(DEPLOYMENT_URL), any(HttpEntity.class), eq(String.class)))
            .thenThrow(camundaNotReady)
            .thenThrow(camundaNotReady)
            .thenReturn(ResponseEntity.ok("{}"));

        withServiceToken(() ->
            importer(configuration, restTemplate, 3).importDefinitions()
        );

        verify(restTemplate, times(3)).postForEntity(eq(DEPLOYMENT_URL), any(HttpEntity.class), eq(String.class));
    }

    @Test
    void doesNotRetryNonRetryableCamundaDeploymentFailures() throws Exception {
        CamundaImportConfiguration configuration = configurationWithDefinition("acknowledge_claim.bpmn");
        RestTemplate restTemplate = mock(RestTemplate.class);

        when(restTemplate.postForEntity(eq(DEPLOYMENT_URL), any(HttpEntity.class), eq(String.class)))
            .thenThrow(HttpClientErrorException.create(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                null,
                "invalid definition".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8
            ));

        assertThatThrownBy(() -> withServiceToken(() ->
            importer(configuration, restTemplate, 3).importDefinitions()
        ))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Camunda definition import failed for")
            .hasMessageContaining("acknowledge_claim.bpmn");

        verify(restTemplate).postForEntity(eq(DEPLOYMENT_URL), any(HttpEntity.class), eq(String.class));
    }

    @Test
    void failsFastAfterRetryableCamundaDeploymentFailuresAreExhausted() throws Exception {
        configurationWithDefinition("acknowledge_claim.bpmn");
        CamundaImportConfiguration configuration = configurationWithDefinition("add_case_note.bpmn");
        RestTemplate restTemplate = mock(RestTemplate.class);

        when(restTemplate.postForEntity(eq(DEPLOYMENT_URL), any(HttpEntity.class), eq(String.class)))
            .thenThrow(serverError());

        assertThatThrownBy(() -> withServiceToken(() ->
            importer(configuration, restTemplate, 2).importDefinitions()
        ))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("failed after retrying transient deployment response")
            .hasMessageContaining("acknowledge_claim.bpmn");

        verify(restTemplate, times(2)).postForEntity(eq(DEPLOYMENT_URL), any(HttpEntity.class), eq(String.class));
    }

    private CamundaDefinitionImporter importer(CamundaImportConfiguration configuration,
                                               RestTemplate restTemplate,
                                               int maxUploadAttempts) {
        return new CamundaDefinitionImporter(
            configuration,
            restTemplate,
            maxUploadAttempts,
            Duration.ZERO,
            duration -> { }
        );
    }

    private CamundaImportConfiguration configurationWithDefinition(String fileName) throws IOException {
        Path camundaDirectory = workspace.resolve("camunda");
        Files.createDirectories(camundaDirectory);
        Files.writeString(camundaDirectory.resolve(fileName), "definition");

        return CamundaImportConfiguration.fromEnvironment(
            Map.of(
                "CAMUNDA_BASE_URL", "http://camunda",
                "S2S_URL_BASE", "http://service-auth",
                "S2S_SECRET", "secret"
            ),
            workspace
        );
    }

    private HttpServerErrorException serverError() {
        return HttpServerErrorException.create(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Internal Server Error",
            null,
            "persistence error".getBytes(StandardCharsets.UTF_8),
            StandardCharsets.UTF_8
        );
    }

    private void withServiceToken(ThrowingRunnable runnable) throws Exception {
        AuthTokenGenerator tokenGenerator = mock(AuthTokenGenerator.class);
        when(tokenGenerator.generate()).thenReturn("service-token");

        try (MockedStatic<AuthTokenGeneratorFactory> tokenGeneratorFactory = mockStatic(AuthTokenGeneratorFactory.class)) {
            tokenGeneratorFactory
                .when(() -> AuthTokenGeneratorFactory.createDefaultGenerator(eq("secret"), eq("civil_service"), any()))
                .thenReturn(tokenGenerator);

            runnable.run();
        }
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }
}
