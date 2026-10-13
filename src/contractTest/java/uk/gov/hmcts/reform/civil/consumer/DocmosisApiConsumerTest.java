package uk.gov.hmcts.reform.civil.consumer;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit.MockServerConfig;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.client.DocmosisApiClient;
import uk.gov.hmcts.reform.civil.helpers.ResourceReader;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisRequest;
import uk.gov.hmcts.reform.civil.service.DocumentConversionService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@PactTestFor(providerName = "docmosis_render")
@MockServerConfig(hostInterface = "localhost", port = "6660")
@TestPropertySource(properties = "docmosis.tornado.key=accessKey")
public class DocmosisApiConsumerTest extends BaseContractTest {

    public static final String ENDPOINT = "/rs/render";
    public static final String CONVERT_ENDPOINT = "/rs/convert";
    private static final String SOURCE_FILE_NAME = "source-document.docx";
    private static final String CONVERTED_FILE_NAME = "source-document.pdf";
    private static final String DOCUMENT_URL = "http://dm-store/documents/document-id";

    @Autowired
    private DocmosisApiClient docmosisApiClient;

    @Autowired
    private DocumentConversionService documentConversionService;

    @MockBean
    private DocumentManagementService documentManagementService;

    @Pact(consumer = "civil_service")
    public RequestResponsePact postCreateDocumentRequest(PactDslWithProvider builder)
        throws JSONException, IOException {
        return buildCreateDocumentResponsePact(builder);
    }

    @Pact(consumer = "civil_service")
    public RequestResponsePact postConvertDocumentRequest(PactDslWithProvider builder) throws IOException {
        return builder
            .uponReceiving("a convert document request")
            .path(CONVERT_ENDPOINT)
            .method(HttpMethod.POST.toString())
            .matchHeader(HttpHeaders.CONTENT_TYPE, "multipart/form-data.*", "multipart/form-data")
            .willRespondWith()
            .withBinaryData(getResponse(), "application/pdf")
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    @AfterEach
    void cleanUpConvertedSourceFile() throws IOException {
        Files.deleteIfExists(Path.of(CONVERTED_FILE_NAME));
    }

    @Test
    @PactTestFor(pactMethod = "postCreateDocumentRequest")
    public void verifyCreateDocumentRequest() throws IOException {
        byte[] response = docmosisApiClient.createDocument(getDocmosisRequest());

        assertThat(
            response,
            is(equalTo(getResponse()))
        );
    }

    @Test
    @PactTestFor(pactMethod = "postConvertDocumentRequest")
    public void verifyConvertDocumentRequest() throws IOException {
        when(documentManagementService.downloadDocument(AUTHORIZATION_TOKEN, DOCUMENT_URL))
            .thenReturn("source document".getBytes());

        byte[] response = documentConversionService.convert(getSourceDocument(), 1712345678901234L, AUTHORIZATION_TOKEN);

        assertThat(
            response,
            is(equalTo(getResponse()))
        );
    }

    private RequestResponsePact buildCreateDocumentResponsePact(PactDslWithProvider builder) throws IOException {
        return builder
            .uponReceiving("a render document request")
            .path(ENDPOINT)
            .method(HttpMethod.POST.toString())
            .body(createJsonObject(getDocmosisRequest()))
            .willRespondWith()
            .withBinaryData(getResponse(), "application/pdf")
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    private static byte[] getResponse() throws IOException {
        return ResourceReader.readBytes("/TEST_DOCUMENT_1.pdf");
    }

    private DocmosisRequest getDocmosisRequest() {
        return new DocmosisRequest()
            .setAccessKey("accessKey")
            .setTemplateName("templateName")
            .setOutputFormat("outputFormat")
            .setOutputName("outputName")
            .setData(Map.of("data", "dataV"));
    }

    private Document getSourceDocument() {
        return new Document()
            .setDocumentUrl(DOCUMENT_URL)
            .setDocumentFileName(SOURCE_FILE_NAME);
    }

}
