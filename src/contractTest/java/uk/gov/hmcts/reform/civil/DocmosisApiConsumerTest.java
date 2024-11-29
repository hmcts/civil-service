package uk.gov.hmcts.reform.civil;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit.MockServerConfig;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import uk.gov.hmcts.reform.civil.client.DocmosisApiClient;
import uk.gov.hmcts.reform.civil.helpers.ResourceReader;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisRequest;

import java.io.IOException;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@PactTestFor(providerName = "docmosis_render")
@MockServerConfig(hostInterface = "localhost", port = "6660")
public class DocmosisApiConsumerTest extends BaseContractTest {

    public static final String ENDPOINT = "/rs/render";

    @Autowired
    private DocmosisApiClient docmosisApiClient;

    @Pact(consumer = "civil_service")
    public RequestResponsePact postCreateDocumentRequest(PactDslWithProvider builder)
        throws JSONException, IOException {
        return buildCreateDocumentResponsePact(builder);
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
        return DocmosisRequest.builder()
            .accessKey("accessKey")
            .templateName("templateName")
            .outputFormat("outputFormat")
            .outputName("outputName")
            .data(Map.of("data", "dataV"))
            .build();
    }

}
