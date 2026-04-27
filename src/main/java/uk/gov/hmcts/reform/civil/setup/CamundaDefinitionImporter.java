package uk.gov.hmcts.reform.civil.setup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGeneratorFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class CamundaDefinitionImporter {

    private static final Logger logger = LoggerFactory.getLogger(CamundaDefinitionImporter.class);
    private static final DateTimeFormatter BPMN_DEPLOYMENT_TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final CamundaImportConfiguration configuration;
    private final RestTemplate restTemplate;

    public CamundaDefinitionImporter(CamundaImportConfiguration configuration) {
        this(configuration, new RestTemplate());
    }

    CamundaDefinitionImporter(CamundaImportConfiguration configuration, RestTemplate restTemplate) {
        this.configuration = configuration;
        this.restTemplate = restTemplate;
    }

    public void importDefinitions() throws IOException {
        List<CamundaImportConfiguration.CamundaDefinitionFile> definitionFiles = configuration.getDefinitionFiles();
        if (definitionFiles.isEmpty()) {
            logger.info("No Camunda BPMN or DMN files found to import");
            return;
        }

        logger.info("Importing {} Camunda BPMN/DMN definitions into {}", definitionFiles.size(), configuration.getCamundaBaseUrl());

        AuthTokenGenerator serviceTokenGenerator = AuthTokenGeneratorFactory.createDefaultGenerator(
            configuration.getServiceAuthClientSecret(),
            configuration.getServiceAuthClientId(),
            new ServiceAuthorisationRestApi(configuration.getServiceAuthBaseUrl(), restTemplate)
        );
        String serviceAuthorization = serviceTokenGenerator.generate();

        List<String> failures = new ArrayList<>();
        for (CamundaImportConfiguration.CamundaDefinitionFile definitionFile : definitionFiles) {
            try {
                uploadDefinition(serviceAuthorization, definitionFile);
            } catch (RestClientResponseException exception) {
                String message = String.format(
                    "%s upload failed with status %s and response %s",
                    definitionFile.path().getFileName(),
                    exception.getStatusCode(),
                    exception.getResponseBodyAsString()
                );
                logger.error(message, exception);
                failures.add(message);
            } catch (Exception exception) {
                String message = String.format("%s upload failed: %s", definitionFile.path().getFileName(), exception.getMessage());
                logger.error(message, exception);
                failures.add(message);
            }
        }

        if (!failures.isEmpty()) {
            throw new IllegalStateException("Camunda definition import failed for: " + String.join(", ", failures));
        }
    }

    private void uploadDefinition(String serviceAuthorization,
                                  CamundaImportConfiguration.CamundaDefinitionFile definitionFile) {
        MultiValueMap<String, Object> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("deployment-name", deploymentNameFor(definitionFile));
        requestBody.add("tenant-id", configuration.getTenantId());
        requestBody.add("file", new FileSystemResource(definitionFile.path()));

        if (definitionFile.type() == CamundaImportConfiguration.DefinitionType.DMN) {
            requestBody.add("deploy-changed-only", "true");
            requestBody.add("deployment-source", configuration.getDeploymentSource());
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.add("ServiceAuthorization", serviceAuthorization);

        ResponseEntity<String> response = restTemplate.postForEntity(
            configuration.getCamundaBaseUrl() + "/engine-rest/deployment/create",
            new HttpEntity<>(requestBody, headers),
            String.class
        );

        logger.info(
            "{} uploaded successfully with status {}",
            definitionFile.path().getFileName(),
            response.getStatusCode()
        );
    }

    private String deploymentNameFor(CamundaImportConfiguration.CamundaDefinitionFile definitionFile) {
        if (definitionFile.type() == CamundaImportConfiguration.DefinitionType.BPMN) {
            return String.format(
                "%s-%s",
                LocalDateTime.now().format(BPMN_DEPLOYMENT_TIMESTAMP),
                definitionFile.path().getFileName()
            );
        }

        return definitionFile.path().getFileName().toString();
    }
}
