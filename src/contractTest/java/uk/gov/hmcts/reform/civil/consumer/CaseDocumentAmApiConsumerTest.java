package uk.gov.hmcts.reform.civil.consumer;

import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.LambdaDsl;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit.MockServerConfig;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.apache.hc.client5.http.entity.mime.HttpMultipartMode;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.core5.http.ContentType;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.SEALED_CLAIM;

@PactTestFor(providerName = "ccd_case_document_am_api")
@MockServerConfig(hostInterface = "localhost", port = "6680")
@TestPropertySource(properties = {
    "case_document_am.url=http://localhost:6680",
    "document_management.url=http://localhost:6680",
    "document_management.userRoles=caseworker-civil,caseworker-civil-solicitor"
})
public class CaseDocumentAmApiConsumerTest extends BaseContractTest {

    private static final String DOCUMENT_ID = "24828900-4706-4f88-9fa4-0d8a4e047dc2";
    private static final String FILE_NAME = "0000-claim.pdf";
    private static final byte[] FILE_CONTENT = "test document content".getBytes(StandardCharsets.UTF_8);
    private static final String DOCUMENT_SELF_URL = "http://localhost:6680/cases/documents/" + DOCUMENT_ID;
    private static final String DOCUMENT_BINARY_URL = DOCUMENT_SELF_URL + "/binary";
    private static final String HASH_TOKEN = "hash-token";
    private static final String USER_ID = "user-id";

    @Autowired
    private DocumentManagementService documentManagementService;

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @MockBean(name = "userService")
    private UserService userService;

    @BeforeEach
    void setUp() {
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN);
        when(userService.getUserInfo(AUTHORIZATION_TOKEN)).thenReturn(UserInfo.builder().uid(USER_ID).build());
    }

    @Pact(consumer = "civil_service")
    public RequestResponsePact uploadDocument(PactDslWithProvider builder) {
        return builder
            .uponReceiving("a case document upload request")
            .path("/cases/documents")
            .method(HttpMethod.POST.toString())
            .headers(AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN, SERVICE_AUTHORIZATION_HEADER, SERVICE_AUTH_TOKEN)
            .body(buildUploadDocumentRequest())
            .willRespondWith()
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(buildUploadDocumentResponse())
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    @Pact(consumer = "civil_service")
    public RequestResponsePact downloadDocumentBinary(PactDslWithProvider builder) {
        return builder
            .uponReceiving("a case document binary download request")
            .path("/cases/documents/" + DOCUMENT_ID + "/binary")
            .headers(AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN, SERVICE_AUTHORIZATION_HEADER, SERVICE_AUTH_TOKEN)
            .method(HttpMethod.GET.toString())
            .willRespondWith()
            .withBinaryData(FILE_CONTENT, MediaType.APPLICATION_PDF_VALUE)
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    @Pact(consumer = "civil_service")
    public RequestResponsePact deleteDocument(PactDslWithProvider builder) {
        return builder
            .uponReceiving("a permanent case document delete request")
            .path("/cases/documents/" + DOCUMENT_ID)
            .headers(AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN, SERVICE_AUTHORIZATION_HEADER, SERVICE_AUTH_TOKEN)
            .method(HttpMethod.DELETE.toString())
            .matchQuery("permanent", "true|false", "true")
            .willRespondWith()
            .status(HttpStatus.SC_NO_CONTENT)
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "uploadDocument")
    public void verifyUploadDocument() {
        CaseDocument response = documentManagementService.uploadDocument(
            AUTHORIZATION_TOKEN,
            new PDF(FILE_NAME, FILE_CONTENT, SEALED_CLAIM)
        );

        assertThat(response.getDocumentName(), is(FILE_NAME));
        assertThat(response.getDocumentLink().getDocumentUrl(), is(DOCUMENT_SELF_URL));
        assertThat(response.getDocumentLink().getDocumentBinaryUrl(), is(DOCUMENT_BINARY_URL));
        assertThat(response.getDocumentLink().getDocumentHash(), is(HASH_TOKEN));
        assertThat(response.getDocumentSize(), is(equalTo((long) FILE_CONTENT.length)));
    }

    @Test
    @PactTestFor(pactMethod = "downloadDocumentBinary")
    public void verifyDownloadDocumentBinary() {
        byte[] response = documentManagementService.downloadDocument(AUTHORIZATION_TOKEN, DOCUMENT_SELF_URL);

        assertThat(response, is(equalTo(FILE_CONTENT)));
    }

    @Test
    @PactTestFor(pactMethod = "deleteDocument")
    public void verifyDeleteDocument() {
        documentManagementService.deleteDocument(AUTHORIZATION_TOKEN, DOCUMENT_SELF_URL);
    }

    private MultipartEntityBuilder buildUploadDocumentRequest() {
        return MultipartEntityBuilder.create()
            .setMode(HttpMultipartMode.EXTENDED)
            .addTextBody("caseTypeId", "CIVIL", ContentType.create(MediaType.TEXT_PLAIN_VALUE, StandardCharsets.UTF_8))
            .addTextBody("jurisdictionId", "CIVIL", ContentType.create(MediaType.TEXT_PLAIN_VALUE, StandardCharsets.UTF_8))
            .addBinaryBody("files", FILE_CONTENT, ContentType.create(MediaType.APPLICATION_PDF_VALUE), FILE_NAME)
            .addTextBody("classification", "RESTRICTED", ContentType.create(MediaType.TEXT_PLAIN_VALUE, StandardCharsets.UTF_8));
    }

    private DslPart buildUploadDocumentResponse() {
        return LambdaDsl.newJsonBody(root -> root
            .minArrayLike("documents", 1, 1, document -> document
                .stringValue("classification", "RESTRICTED")
                .numberValue("size", FILE_CONTENT.length)
                .stringValue("mimeType", MediaType.APPLICATION_PDF_VALUE)
                .stringValue("originalDocumentName", FILE_NAME)
                .stringValue("createdOn", "2026-08-13T10:15:30.000Z")
                .stringValue("hashToken", HASH_TOKEN)
                .object("_links", links -> links
                    .object("self", self -> self.stringValue("href", DOCUMENT_SELF_URL))
                    .object("binary", binary -> binary.stringValue("href", DOCUMENT_BINARY_URL))))
        ).build();
    }
}
