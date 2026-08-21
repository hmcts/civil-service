package uk.gov.hmcts.reform.civil.documentmanagement;

import org.apache.tika.Tika;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClientApi;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.document.DocumentDownloadClientApi;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * DTSCCI-5627 (EXC-CS-022): a short / malformed document self-href is a bad-input error, not a
 * transient download failure, so it must NOT be retried by the {@code @Retryable} download policy.
 *
 * <p>This lives in a separate class from {@link SecuredDocumentManagementServiceTest} because it
 * needs {@link EnableRetry} to activate the retry proxy. Without it the retry interceptor is not
 * applied, so the sibling test can only assert at the method level and cannot prove the
 * {@code noRetryFor} classification actually short-circuits the five attempts.
 *
 * <p>Pre-fix this fails (the wrapped {@link DocumentDownloadException} is retried 5x, so
 * {@code getUserInfo} is invoked five times and the test also incurs the full ~15s backoff);
 * post-fix the {@link InvalidDocumentLinkException} is excluded from retry and it is attempted once.
 */
@SpringBootTest(classes = {
    SecuredDocumentManagementService.class,
    SecuredDocumentManagementServiceRetryTest.TestRetryConfig.class,
    JacksonAutoConfiguration.class,
    DocumentManagementConfiguration.class, Tika.class})
class SecuredDocumentManagementServiceRetryTest {

    public static final String BEARER_TOKEN = "Bearer Token";

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

        // getUserInfo runs once at the top of every attempt, so it is a reliable per-attempt counter:
        // exactly one invocation proves the bad-input error short-circuited the retry (not 5 attempts).
        verify(userService, times(1)).getUserInfo(BEARER_TOKEN);
        // the guard trips before any CDAM call, so neither document client is ever touched.
        verifyNoInteractions(caseDocumentClientApi, documentDownloadClient);
    }
}
