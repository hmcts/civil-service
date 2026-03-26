package uk.gov.hmcts.reform.civil.consumer;

import au.com.dius.pact.consumer.dsl.DslPart;
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
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.CaseDetails;
import uk.gov.hmcts.reform.civil.client.EvidenceManagementApiClient;
import uk.gov.hmcts.reform.civil.model.BundleRequest;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.io.IOException;
import java.util.HashMap;

import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@PactTestFor(providerName = "em_stitchBundle")
@MockServerConfig(hostInterface = "localhost", port = "6664")
@TestPropertySource(properties = "bundle.api.url=http://localhost:6664")
public class StitchBundleApiConsumerTest extends BaseContractTest {

    public static final String ENDPOINT = "/api/stitch-ccd-bundles";

    @Autowired
    private EvidenceManagementApiClient evidenceManagementApiClient;

    @Pact(consumer = "civil_service")
    public RequestResponsePact postStitchBundleServiceRequest(PactDslWithProvider builder)
        throws JSONException, IOException {
        return buildStitchBundleResponsePact(builder);
    }

    @Test
    @PactTestFor(pactMethod = "postStitchBundleServiceRequest")
    public void verifyStitchBundle() {
        ResponseEntity<uk.gov.hmcts.reform.ccd.client.model.CaseDetails> response = evidenceManagementApiClient.stitchBundle(
            AUTHORIZATION_TOKEN,
            SERVICE_AUTH_TOKEN,
            getBundleRequest()
        );

        CaseData caseData = objectMapper.convertValue(response.getBody().getData(), CaseData.class);

        assertThat(
            caseData.getCaseBundles().get(0).getValue().getStitchedDocument().get().getDocumentUrl(),
            is(equalTo("documentStitchedUrl"))
        );
    }

    private RequestResponsePact buildStitchBundleResponsePact(PactDslWithProvider builder) throws IOException {
        return builder
            .uponReceiving("a stitch bundle request")
            .path(ENDPOINT)
            .method(HttpMethod.POST.toString())
            .headers(SERVICE_AUTHORIZATION_HEADER, SERVICE_AUTH_TOKEN, AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN)
            .body(createJsonObject(getBundleRequest()))
            .willRespondWith()
            .body(buildStitchBundleResponseDsl())
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    private DslPart buildStitchBundleResponseDsl() {
        return newJsonBody(response ->
                               response
                                   .numberType("documentTaskId", 123)
                                   .object("data", bundleData ->
                                       bundleData
                                           .minArrayLike("caseBundles", 1, caseBundle ->
                                               caseBundle
                                                   .object("value", bundleDetails ->
                                                       bundleDetails
                                                           .stringType("id", "id")
                                                           .stringType("title", "title")
                                                           .stringType("description", "description")
                                                           .stringType("stitchStatus", "stitchStatus")
                                                           .stringType("fileName", "fileName")
                                                           .stringMatcher("createdOn",
                                                                          "^(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{1,6})$",
                                                                          "2020-10-06T18:54:48.785000")
                                                           .date("bundleHearingDate", "yyyy-MM-dd")
                                                           .object("stitchedDocument", stitchedDocument ->
                                                               stitchedDocument
                                                                   .stringType("document_url", "documentStitchedUrl")
                                                                   .stringType(
                                                                       "document_binary_url",
                                                                       "documentBinaryUrl"
                                                                   )
                                                                   .stringType("document_filename", "documentFileName")
                                                                   .stringType("document_hash", "documentHash")
                                                                   .stringType("category_id", "categoryID")
                                                           )
                                                   )
                                           )
                                   )
        ).build();
    }

    private BundleRequest getBundleRequest() {
        return new BundleRequest(new CaseDetails()
                                     .setId(666L)
                                     .setData(new HashMap<>()));
    }
}
