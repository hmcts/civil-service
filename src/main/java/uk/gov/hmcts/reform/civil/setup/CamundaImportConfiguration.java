package uk.gov.hmcts.reform.civil.setup;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public final class CamundaImportConfiguration {

    static final String DEFAULT_DEPLOYMENT_SOURCE = "civil";
    static final String DEFAULT_TENANT_ID = "civil";

    private final String camundaBaseUrl;
    private final String serviceAuthBaseUrl;
    private final String serviceAuthClientId;
    private final String serviceAuthClientSecret;
    private final String deploymentSource;
    private final String tenantId;
    private final List<CamundaDefinitionFile> definitionFiles;

    private CamundaImportConfiguration(String camundaBaseUrl,
                                       String serviceAuthBaseUrl,
                                       String serviceAuthClientId,
                                       String serviceAuthClientSecret,
                                       String deploymentSource,
                                       String tenantId,
                                       List<CamundaDefinitionFile> definitionFiles) {
        this.camundaBaseUrl = camundaBaseUrl;
        this.serviceAuthBaseUrl = serviceAuthBaseUrl;
        this.serviceAuthClientId = serviceAuthClientId;
        this.serviceAuthClientSecret = serviceAuthClientSecret;
        this.deploymentSource = deploymentSource;
        this.tenantId = tenantId;
        this.definitionFiles = List.copyOf(definitionFiles);
    }

    public static CamundaImportConfiguration fromEnvironment(Map<String, String> env, Path workspace) throws IOException {
        Objects.requireNonNull(env, "env must not be null");
        Objects.requireNonNull(workspace, "workspace must not be null");

        ServiceAuthCredentials credentials = resolveServiceAuthCredentials(env);

        return new CamundaImportConfiguration(
            requireValue(env, "CAMUNDA_BASE_URL"),
            requireValue(env, "S2S_URL_BASE"),
            credentials.clientId(),
            credentials.clientSecret(),
            env.getOrDefault("CAMUNDA_DEPLOYMENT_SOURCE", DEFAULT_DEPLOYMENT_SOURCE),
            env.getOrDefault("CAMUNDA_TENANT_ID", DEFAULT_TENANT_ID),
            findDefinitionFiles(workspace)
        );
    }

    static ServiceAuthCredentials resolveServiceAuthCredentials(Map<String, String> env) {
        if (hasValues(env, "BEFTA_S2S_CLIENT_ID", "BEFTA_S2S_CLIENT_SECRET")) {
            return new ServiceAuthCredentials(env.get("BEFTA_S2S_CLIENT_ID"), env.get("BEFTA_S2S_CLIENT_SECRET"));
        }

        if (hasValues(env, "CCD_API_GATEWAY_S2S_ID", "CCD_API_GATEWAY_S2S_KEY")) {
            return new ServiceAuthCredentials(env.get("CCD_API_GATEWAY_S2S_ID"), env.get("CCD_API_GATEWAY_S2S_KEY"));
        }

        String defaultSecret = env.get("S2S_SECRET");
        if (isBlank(defaultSecret)) {
            throw new IllegalArgumentException("Missing S2S credentials for Camunda import");
        }

        return new ServiceAuthCredentials("civil_service", defaultSecret);
    }

    static List<CamundaDefinitionFile> findDefinitionFiles(Path workspace) throws IOException {
        List<CamundaDefinitionFile> definitions = new ArrayList<>();
        definitions.addAll(findFiles(workspace.resolve("camunda"), ".bpmn", DefinitionType.BPMN));
        definitions.addAll(findFiles(workspace.resolve("wa-dmn").resolve("resources"), ".dmn", DefinitionType.DMN));
        definitions.sort(Comparator.comparing(file -> file.path().toString()));
        return Collections.unmodifiableList(definitions);
    }

    private static List<CamundaDefinitionFile> findFiles(Path directory,
                                                         String extension,
                                                         DefinitionType definitionType) throws IOException {
        if (!Files.isDirectory(directory)) {
            return List.of();
        }

        try (Stream<Path> paths = Files.walk(directory)) {
            return paths
                .filter(Files::isRegularFile)
                .filter(path -> path.getFileName().toString().endsWith(extension))
                .sorted()
                .map(path -> new CamundaDefinitionFile(path, definitionType))
                .toList();
        }
    }

    private static boolean hasValues(Map<String, String> env, String clientIdKey, String secretKey) {
        return !isBlank(env.get(clientIdKey)) && !isBlank(env.get(secretKey));
    }

    private static String requireValue(Map<String, String> env, String key) {
        String value = env.get(key);
        if (isBlank(value)) {
            throw new IllegalArgumentException(String.format("Missing required environment variable '%s'", key));
        }
        return value;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public String getCamundaBaseUrl() {
        return camundaBaseUrl;
    }

    public String getServiceAuthBaseUrl() {
        return serviceAuthBaseUrl;
    }

    public String getServiceAuthClientId() {
        return serviceAuthClientId;
    }

    public String getServiceAuthClientSecret() {
        return serviceAuthClientSecret;
    }

    public String getDeploymentSource() {
        return deploymentSource;
    }

    public String getTenantId() {
        return tenantId;
    }

    public List<CamundaDefinitionFile> getDefinitionFiles() {
        return definitionFiles;
    }

    public record ServiceAuthCredentials(String clientId, String clientSecret) {
    }

    public record CamundaDefinitionFile(Path path, DefinitionType type) {
    }

    public enum DefinitionType {
        BPMN,
        DMN
    }
}
