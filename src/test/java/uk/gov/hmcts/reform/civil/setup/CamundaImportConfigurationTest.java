package uk.gov.hmcts.reform.civil.setup;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CamundaImportConfigurationTest {

    @TempDir
    Path workspace;

    @Test
    void shouldResolveCcdGatewayS2SCredentialsAndDefinitionFiles() throws IOException {
        Files.createDirectories(workspace.resolve("camunda"));
        Files.createFile(workspace.resolve("camunda").resolve("process.bpmn"));
        Files.createDirectories(workspace.resolve("wa-dmn").resolve("resources"));
        Files.createFile(workspace.resolve("wa-dmn").resolve("resources").resolve("decision.dmn"));

        CamundaImportConfiguration configuration = CamundaImportConfiguration.fromEnvironment(
            Map.of(
                "CAMUNDA_BASE_URL", "https://camunda.example.internal",
                "S2S_URL_BASE", "http://rpe-service-auth-provider-aat.service.core-compute-aat.internal",
                "CCD_API_GATEWAY_S2S_ID", "ccd_gw",
                "CCD_API_GATEWAY_S2S_KEY", "ccd-secret"
            ),
            workspace
        );

        assertThat(configuration.getServiceAuthClientId()).isEqualTo("ccd_gw");
        assertThat(configuration.getServiceAuthClientSecret()).isEqualTo("ccd-secret");
        assertThat(configuration.getDefinitionFiles())
            .extracting(file -> file.path().getFileName().toString(), CamundaImportConfiguration.CamundaDefinitionFile::type)
            .containsExactly(
                org.assertj.core.groups.Tuple.tuple("process.bpmn", CamundaImportConfiguration.DefinitionType.BPMN),
                org.assertj.core.groups.Tuple.tuple("decision.dmn", CamundaImportConfiguration.DefinitionType.DMN)
            );
    }

    @Test
    void shouldPreferBeftaS2SCredentialsWhenPresent() {
        CamundaImportConfiguration.ServiceAuthCredentials credentials = CamundaImportConfiguration.resolveServiceAuthCredentials(
            Map.of(
                "BEFTA_S2S_CLIENT_ID", "ccd_data",
                "BEFTA_S2S_CLIENT_SECRET", "befta-secret",
                "CCD_API_GATEWAY_S2S_ID", "ccd_gw",
                "CCD_API_GATEWAY_S2S_KEY", "ccd-secret"
            )
        );

        assertThat(credentials.clientId()).isEqualTo("ccd_data");
        assertThat(credentials.clientSecret()).isEqualTo("befta-secret");
    }

    @Test
    void shouldFailWhenNoS2SCredentialsAreAvailable() {
        assertThatThrownBy(() -> CamundaImportConfiguration.resolveServiceAuthCredentials(Map.of()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Missing S2S credentials");
    }
}
