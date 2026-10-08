package uk.gov.hmcts.reform.civil.documentmanagement;

import feign.FeignException;
import feign.Request;
import feign.Response;
import org.apache.tika.Tika;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.util.unit.DataSize;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClientApi;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.civil.client.CaseDocumentBinaryApiClient;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.document.DocumentDownloadClientApi;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * DTSCCI-5627 (EXC-CS-022) / DTSCCI-6312 (EXC-CS-029): classified download failures must
 * honour the {@code @Retryable} policy. Bad-input and access errors are attempted once;
 * transient CDAM 5xx is retried up to {@code maxAttempts = 3}.
 *
 * <p>This lives in a separate class from {@link SecuredDocumentManagementServiceTest} because it
 * needs {@link EnableRetry} to activate the retry proxy. Without it the retry interceptor is not
 * applied, so the sibling test can only assert at the method level and cannot prove the
 * {@code noRetryFor} classification actually short-circuits the three attempts.
 */
@SpringBootTest(classes = {
    SecuredDocumentManagementService.class,
    SecuredDocumentManagementServiceRetryTest.TestRetryConfig.class,
    JacksonAutoConfiguration.class,
    DocumentManagementConfiguration.class, Tika.class})
class SecuredDocumentManagementServiceRetryTest {

    public static final String BEARER_TOKEN = "Bearer Token";
    private static final String DOCUMENT_PATH = "/documents/85d97996-22a5-40d7-882e-3a382c8ae1b7";

    // proxyTargetClass = true so the retry proxy is a CGLIB subclass of the concrete service,
    // letting the test autowire SecuredDocumentManagementService directly (a JDK interface
    // proxy would only satisfy the DocumentManagementService interface type).
    @EnableRetry(proxyTargetClass = true)
    @Configuration
    static class TestRetryConfig {
    }

    @MockBean
    private CaseDocumentClientApi caseDocumentClientApi;
    @MockBean
    private CaseDocumentBinaryApiClient caseDocumentBinaryApiClient;
    @MockBean
    private DocumentDownloadClientApi documentDownloadClient;
    @MockBean
    private AuthTokenGenerator authTokenGenerator;
    @MockBean
    private UserService userService;
    @Autowired
    private SecuredDocumentManagementService documentManagementService;

    private final UserInfo userInfo = UserInfo.builder()
        .roles(List.of("role"))
        .uid("id")
        .build();

    @BeforeEach
    public void setUp() {
        when(authTokenGenerator.generate()).thenReturn(BEARER_TOKEN);
        when(userService.getUserInfo(anyString())).thenReturn(userInfo);
    }

    @Test
    void downloadDocumentWithMetaData_shortSelfHref_isNotRetried() {
        // 14 chars: shorter than DOC_UUID_LENGTH (36), so it cannot carry a trailing document UUID.
        String shortSelfHref = "documents/null";

        assertThrows(
            InvalidDocumentLinkException.class,
            () -> documentManagementService.downloadDocumentWithMetaData(BEARER_TOKEN, shortSelfHref));

        // the guard trips before any CDAM call, so no document client is ever touched.
        verifyNoInteractions(caseDocumentClientApi, caseDocumentBinaryApiClient, documentDownloadClient);
    }

    @Test
    void downloadDocumentWithMetaData_streamedBinary500_isRetriedThreeTimes() {
        when(caseDocumentClientApi.getMetadataForDocument(anyString(), anyString(), any(UUID.class)))
            .thenReturn(document(1024));
        when(caseDocumentBinaryApiClient.getDocumentBinary(anyString(), anyString(), any(UUID.class)))
            .thenAnswer(invocation -> binaryResponse(500));

        assertThrows(
            DocumentDownloadException.class,
            () -> documentManagementService.downloadDocumentWithMetaData(BEARER_TOKEN, DOCUMENT_PATH));

        verify(caseDocumentBinaryApiClient, times(3)).getDocumentBinary(anyString(), anyString(), any(UUID.class));
    }

    @Test
    void downloadDocumentWithMetaData_tooLarge_isNotRetried() {
        when(caseDocumentClientApi.getMetadataForDocument(anyString(), anyString(), any(UUID.class)))
            .thenReturn(document(DataSize.ofMegabytes(100).toBytes() + 1));

        assertThrows(
            DocumentTooLargeException.class,
            () -> documentManagementService.downloadDocumentWithMetaData(BEARER_TOKEN, DOCUMENT_PATH));

        verify(caseDocumentClientApi, times(1)).getMetadataForDocument(anyString(), anyString(), any(UUID.class));
        verifyNoInteractions(caseDocumentBinaryApiClient);
    }

    @Test
    void downloadDocument_cdam500_isRetriedThreeTimes() {
        String documentPath = "/documents/85d97996-22a5-40d7-882e-3a382c8ae1b7";
        when(caseDocumentClientApi.getDocumentBinary(anyString(), anyString(), any(UUID.class)))
            .thenAnswer(invocation -> {
                throw buildFeignException(500);
            });

        assertThrows(
            DocumentDownloadException.class,
            () -> documentManagementService.downloadDocument(BEARER_TOKEN, documentPath));

        verify(userService, times(3)).getUserInfo(BEARER_TOKEN);
    }

    @Test
    void downloadDocument_cdam401_isNotRetried() {
        String documentPath = "/documents/85d97996-22a5-40d7-882e-3a382c8ae1b7";
        when(caseDocumentClientApi.getDocumentBinary(anyString(), anyString(), any(UUID.class)))
            .thenThrow(buildFeignException(401));

        assertThrows(
            DocumentAccessException.class,
            () -> documentManagementService.downloadDocument(BEARER_TOKEN, documentPath));

        verify(userService, times(1)).getUserInfo(BEARER_TOKEN);
    }

    private static Document document(long size) {
        return Document.builder().size(size).originalDocumentName("TEST_DOCUMENT_1.pdf").build();
    }

    private static Response binaryResponse(int status) {
        Request request = Request.create(
            Request.HttpMethod.GET, "/cases/documents/x/binary", Map.of(), new byte[]{}, StandardCharsets.UTF_8, null);
        return Response.builder().status(status).request(request).headers(Map.of()).body(new byte[]{}).build();
    }

    private static FeignException buildFeignException(int status) {
        Request request = Request.create(
            Request.HttpMethod.GET, "/cases/documents/x", Map.of(), new byte[]{}, StandardCharsets.UTF_8, null);
        return switch (status) {
            case 401 -> new FeignException.Unauthorized("unauthorized", request, new byte[]{}, Map.of());
            case 500 -> new FeignException.InternalServerError("server error", request, new byte[]{}, Map.of());
            default -> new FeignException.GatewayTimeout("timeout", request, new byte[]{}, Map.of());
        };
    }
}
