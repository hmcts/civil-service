package uk.gov.hmcts.reform.civil.config;

import feign.Request;
import feign.RequestTemplate;
import feign.RetryableException;
import feign.Retryer;
import feign.Target;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Guards the read-only POST allowlist in {@link HttpClientFeignConfiguration}.
 *
 * <p>{@link ReadTimeoutAwareRetryer} stops replaying a POST, PUT or PATCH after a read timeout
 * unless its path is on the allowlist, so a new read-only POST endpoint silently loses its replay
 * until someone adds it. This test finds every write endpoint declared on a {@code @FeignClient}
 * interface (this service's own clients and the library clients on the classpath) and fails when
 * one has not been classified here, so the decision is made when the endpoint is added rather
 * than discovered in production. It also checks that each classification agrees with the allowlist
 * set in {@code application.yaml}, the only place it is configured and the value the application
 * loads. Each endpoint is checked as a request from its own {@code @FeignClient} name, because
 * the allowlist matches on client name and path suffix.</p>
 *
 * <p>Only endpoints declared with Spring MVC mapping annotations on a {@code @FeignClient}
 * interface, including methods it inherits, are found. Feign-native {@code @RequestLine}
 * endpoints, clients built with {@code Feign.builder()} and calls made with {@code RestTemplate}
 * or another HTTP client are not covered. {@code src/main} has none of the first two today.
 * An environment override of the property, for example in the Helm values, is not checked
 * either.</p>
 */
class FeignWriteEndpointReplayPolicyTest {

    private static final String BASE_PACKAGE = "uk.gov.hmcts.reform";
    private static final String REPLAY_PROPERTY = "http.client.retry.replay-on-read-timeout";

    /**
     * A POST, PUT or PATCH endpoint, keyed as {@code <interface> <method> <path>}.
     */
    private record Endpoint(String key, String feignClientName, Request.HttpMethod method, String path) {
    }

    /**
     * {@code true}: read-only, must stay on the allowlist so a read timeout is still replayed.
     * {@code false}: a write the server may already have acted on, never replayed after a read timeout.
     */
    private static final Map<String, Boolean> REPLAY_ON_READ_TIMEOUT = Map.ofEntries(
        // read-only POSTs: queries, searches, evaluations and token or lease requests
        Map.entry("CamundaRuntimeApi POST /decision-definition/key/{decisionKey}/tenant-id/{tenantId}/evaluate", true),
        Map.entry("CamundaRuntimeApi POST /history/process-instance", true),
        Map.entry("CamundaTestingSupportApi POST /history/process-instance", true),
        Map.entry("CaseAssignmentApi POST /noc/check-noc-approval", true),
        Map.entry("CoreCaseDataApi POST /searchCases?ctid={caseType}", true),
        Map.entry("DocmosisApiClient POST /rs/render", true),
        Map.entry("IdamApi POST /o/token", true),
        Map.entry("OidcApi POST /o/token", true),
        Map.entry("RoleAssignmentsApi POST /am/role-assignments/query", true),
        Map.entry("ServiceAuthorisationApi POST /lease", true),
        Map.entry("WaTaskManagementApiClient POST /task", true),
        // writes: the server may already have acted on the first attempt
        Map.entry("CamundaRuntimeApi POST /message", false),
        Map.entry("CamundaRuntimeApi POST /process-instance/{processInstanceId}/modification", false),
        Map.entry("CamundaRuntimeApi POST /process-instance/{processInstanceId}/variables", false),
        Map.entry("CamundaRuntimeApi PUT /job/{jobId}/retries", false),
        Map.entry("CamundaRuntimeApi PUT /process-instance/{processInstanceId}/variables/{variableName}", false),
        Map.entry("CamundaTestingSupportApi POST /process-definition/key/{definitionKey}/tenant-id/{tenantId}/start", false),
        Map.entry("CaseAccessApi POST /caseworkers/{uid}/jurisdictions/{jid}/case-types/{ctid}/cases/{cid}/users", false),
        Map.entry("CaseAccessDataStoreApi POST /case-users", false),
        Map.entry("CaseAssignmentApi POST /case-users", false),
        Map.entry("CaseAssignmentApi POST /noc/apply-decision", false),
        Map.entry("CaseDocumentClientApi PATCH /attachToCase", false),
        Map.entry("CaseDocumentClientApi PATCH /{documentId}", false),
        Map.entry("CaseDocumentClientApi POST ", false),
        Map.entry("CaseUserApi PUT /cases/{caseReference}/users/{userId}", false),
        Map.entry("CjesApiClient POST /judgment", false),
        Map.entry("CoreCaseDataApi POST /case-types/{caseTypeId}/cases", false),
        Map.entry("CoreCaseDataApi POST /cases/{caseId}/events", false),
        Map.entry("CoreCaseDataApi POST /cases/{caseId}/supplementary-data", false),
        Map.entry("CoreCaseDataApi POST /caseworkers/{uid}/jurisdictions/{jid}/case-types/{ctid}/cases/{cid}/events", false),
        Map.entry("CoreCaseDataApi POST /caseworkers/{userId}/jurisdictions/{jurisdictionId}/case-types/{caseType}/cases", false),
        Map.entry("CoreCaseDataApi POST /citizens/{uid}/jurisdictions/{jid}/case-types/{ctid}/cases/{cid}/events", false),
        Map.entry("CoreCaseDataApi POST /citizens/{userId}/jurisdictions/{jurisdictionId}/case-types/{caseType}/cases", false),
        Map.entry("DashboardApiClient POST /scenarios/{scenario_ref}/{unique_case_identifier}", false),
        Map.entry("EvidenceManagementApiClient POST /api/new-bundle", false),
        Map.entry("EvidenceManagementApiClient POST /api/stitch-ccd-bundles", false),
        Map.entry("HearingsApi PUT /partiesNotified/{id}", false),
        // single-use authorisation code and account or pin creation
        Map.entry("IdamApi POST /oauth2/authorize", false),
        Map.entry("IdamApi POST /oauth2/token", false),
        Map.entry("IdamApi POST /pin", false),
        Map.entry("IdamTestApi POST /accounts", false),
        Map.entry("PaymentsApi POST /card-payments", false),
        Map.entry("PaymentsApi POST /card-payments/{paymentReference}/cancel", false),
        Map.entry("PaymentsApi POST /credit-account-payments", false),
        Map.entry("PaymentsApi POST /service-request", false),
        Map.entry("PaymentsApi POST /service-request/{service-request-reference}/card-payments", false),
        Map.entry("PaymentsApi POST /service-request/{service-request-reference}/pba-payments", false),
        Map.entry("RoleAssignmentsApi POST /am/role-assignments", false),
        Map.entry("SendLetterApiProxy POST /letters", false),
        Map.entry("WaTaskManagementApiClient POST /task/{task-id}/claim", false)
    );

    private static Map<String, Endpoint> discoverWriteEndpoints() {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false) {
            @Override
            protected boolean isCandidateComponent(AnnotatedBeanDefinition definition) {
                return definition.getMetadata().isInterface();
            }
        };
        scanner.addIncludeFilter(new AnnotationTypeFilter(FeignClient.class));

        Map<String, Endpoint> endpoints = new TreeMap<>();
        scanner.findCandidateComponents(BASE_PACKAGE).forEach(definition -> {
            Class<?> client = load(definition.getBeanClassName());
            RequestMapping classMapping = AnnotatedElementUtils.findMergedAnnotation(client, RequestMapping.class);
            String prefix = classMapping == null || classMapping.path().length == 0 ? "" : classMapping.path()[0];
            // merged so that name and its alias value resolve to the same client name
            FeignClient feignClient = AnnotatedElementUtils.findMergedAnnotation(client, FeignClient.class);
            String clientPath = feignClient.path();
            // getMethods, not getDeclaredMethods, so endpoints inherited from a parent interface count too
            for (Method method : client.getMethods()) {
                RequestMapping mapping = AnnotatedElementUtils.findMergedAnnotation(method, RequestMapping.class);
                if (mapping == null || mapping.method().length == 0) {
                    continue;
                }
                for (RequestMethod requestMethod : mapping.method()) {
                    if (requestMethod == RequestMethod.GET || requestMethod == RequestMethod.DELETE
                        || requestMethod == RequestMethod.HEAD || requestMethod == RequestMethod.OPTIONS) {
                        continue;
                    }
                    String path = clientPath + prefix + (mapping.path().length > 0 ? mapping.path()[0] : "");
                    String key = client.getSimpleName() + " " + requestMethod + " " + path;
                    endpoints.put(key, new Endpoint(key, feignClient.name(), Request.HttpMethod.valueOf(requestMethod.name()), path));
                }
            }
        });
        return endpoints;
    }

    private static Class<?> load(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Sends a read timeout for the endpoint through the retryer as a request from its own Feign
     * client, the way {@code SynchronousMethodHandler} does: the request template carries the
     * client's target, whose name is the {@code @FeignClient} name.
     */
    private static boolean isReplayedAfterReadTimeout(Retryer retryer, Endpoint endpoint) {
        String url = "http://host" + endpoint.path().replaceAll("\\{[^}]*}", "1");
        RequestTemplate template = new RequestTemplate()
            .feignTarget(new Target.HardCodedTarget<>(Object.class, endpoint.feignClientName(), "http://host"));
        Request request = Request.create(endpoint.method(), url, Collections.emptyMap(), null, StandardCharsets.UTF_8, template);
        RetryableException readTimeout = new RetryableException(
            -1, "Read timed out", endpoint.method(), new SocketTimeoutException("Read timed out"), (Long) null, request);
        try {
            // a fresh clone per endpoint, as Feign does per call, so earlier attempts do not count
            retryer.clone().continueOrPropagate(readTimeout);
            return true;
        } catch (RetryableException propagated) {
            return false;
        }
    }

    private static Retryer retryerWithAllowlist(String allowlist) {
        HttpClientFeignConfiguration configuration = new HttpClientFeignConfiguration();
        ReflectionTestUtils.setField(configuration, "replayOnReadTimeoutPaths", allowlist);
        return configuration.feignRetryer(null);
    }

    private static String applicationYamlAllowlist() throws IOException {
        List<PropertySource<?>> documents = new YamlPropertySourceLoader()
            .load("application.yaml", new ClassPathResource("application.yaml"));
        StandardEnvironment environment = new StandardEnvironment();
        // the first document is the one with no profile, which every environment loads
        environment.getPropertySources().addLast(documents.get(0));
        return environment.getProperty(REPLAY_PROPERTY);
    }

    private static Map<String, Boolean> mismatchesWithClassification(String allowlist) {
        Retryer retryer = retryerWithAllowlist(allowlist);
        Map<String, Endpoint> endpoints = discoverWriteEndpoints();
        Map<String, Boolean> mismatches = new TreeMap<>();
        REPLAY_ON_READ_TIMEOUT.forEach((key, expected) -> {
            Endpoint endpoint = endpoints.get(key);
            if (endpoint != null && isReplayedAfterReadTimeout(retryer, endpoint) != expected) {
                mismatches.put(key, !expected);
            }
        });
        return mismatches;
    }

    @Test
    void everyFeignWriteEndpointIsClassified() {
        Set<String> unclassified = new TreeSet<>(discoverWriteEndpoints().keySet());
        unclassified.removeAll(REPLAY_ON_READ_TIMEOUT.keySet());

        assertThat(unclassified)
            .as("New POST/PUT/PATCH Feign endpoint(s). Decide whether each is read-only (add true here AND "
                    + "add <feign-client-name>:<path-suffix> to http.client.retry.replay-on-read-timeout in application.yaml "
                    + "so a read timeout is still replayed) or a write "
                    + "(add false here: it is never replayed after a read timeout).")
            .isEmpty();
    }

    @Test
    void everyClassifiedEndpointStillExists() {
        Set<String> stale = new TreeSet<>(REPLAY_ON_READ_TIMEOUT.keySet());
        stale.removeAll(discoverWriteEndpoints().keySet());

        assertThat(stale)
            .as("classified endpoint(s) no longer declared on a @FeignClient; remove them here")
            .isEmpty();
    }

    @Test
    void applicationYamlAllowlistMatchesTheClassification() throws IOException {
        String allowlist = applicationYamlAllowlist();
        assertThat(allowlist).as(REPLAY_PROPERTY + " in application.yaml").isNotBlank();

        assertThat(mismatchesWithClassification(allowlist))
            .as("endpoints whose replay after a read timeout under application.yaml differs from the classification"
                    + " (value shown is whether it was replayed)")
            .isEmpty();
    }

    @Test
    void allowlistEntriesAreScopedToAFeignClient() throws IOException {
        Set<String> clientNames = new TreeSet<>();
        discoverWriteEndpoints().values().forEach(endpoint -> clientNames.add(endpoint.feignClientName()));

        for (String entry : applicationYamlAllowlist().split(",")) {
            ReadTimeoutAwareRetryer.AllowlistEntry parsed = ReadTimeoutAwareRetryer.AllowlistEntry.parse(entry);
            assertThat(parsed.clientName())
                .as("allowlist entry '%s' in application.yaml must name the @FeignClient it applies to", entry.trim())
                .isIn(clientNames);
        }
    }
}
