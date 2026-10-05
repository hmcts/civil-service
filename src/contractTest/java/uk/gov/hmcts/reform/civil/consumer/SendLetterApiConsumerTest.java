package uk.gov.hmcts.reform.civil.consumer;

import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit.MockServerConfig;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
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
import uk.gov.hmcts.reform.civil.service.BulkPrintService;
import uk.gov.hmcts.reform.sendletter.api.SendLetterResponse;

import java.util.List;

import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@PactTestFor(providerName = "send_letter")
@MockServerConfig(hostInterface = "localhost", port = "6684")
@TestPropertySource(properties = "send-letter.url=http://localhost:6684")
public class SendLetterApiConsumerTest extends BaseContractTest {

    private static final String LETTER_ID = "11111111-2222-3333-4444-555555555555";
    private static final String CASE_ID = "1712345678901234";
    private static final String CASE_REFERENCE = "000MC001";
    private static final String LETTER_TYPE = "final-order";
    private static final String LETTER_V2_MEDIA_TYPE = "application/vnd.uk.gov.hmcts.letter-service.in.letter.v2+json";

    @Autowired
    private BulkPrintService bulkPrintService;

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @BeforeEach
    void setUp() {
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN);
    }

    @Pact(consumer = "civil_service")
    public RequestResponsePact sendLetter(PactDslWithProvider builder) {
        return builder
            .uponReceiving("a bulk print send letter request")
            .path("/letters")
            .matchQuery("isAsync", "true", "true")
            .headers(SERVICE_AUTHORIZATION_HEADER, SERVICE_AUTH_TOKEN, HttpHeaders.CONTENT_TYPE, LETTER_V2_MEDIA_TYPE)
            .method(HttpMethod.POST.toString())
            .body(buildSendLetterRequest(), LETTER_V2_MEDIA_TYPE)
            .willRespondWith()
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(buildSendLetterResponse())
            .status(HttpStatus.SC_OK)
            .uponReceiving("a bulk print letter status request")
            .path("/letters/" + LETTER_ID)
            .matchQuery("include-additional-info", "false", "false")
            .matchQuery("check-duplicate", "true", "true")
            .method(HttpMethod.GET.toString())
            .willRespondWith()
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(buildLetterStatusResponse())
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "sendLetter")
    public void verifySendLetter() {
        SendLetterResponse response = bulkPrintService.printLetter(
            "test letter".getBytes(),
            CASE_ID,
            CASE_REFERENCE,
            LETTER_TYPE,
            List.of("Jane Smith"),
            List.of("final-order.pdf")
        );

        assertThat(response.letterId.toString(), is(equalTo(LETTER_ID)));
    }

    private String buildSendLetterRequest() {
        return """
            {
              "type": "CMC001",
              "documents": [
                "dGVzdCBsZXR0ZXI="
              ],
              "additional_data": {
                "letterType": "%s",
                "caseIdentifier": "%s",
                "caseReferenceNumber": "%s",
                "recipients": [
                  "Jane Smith"
                ],
                "fileNames": [
                  "final-order.pdf"
                ]
              }
            }
            """.formatted(LETTER_TYPE, CASE_ID, CASE_REFERENCE);
    }

    private DslPart buildSendLetterResponse() {
        return newJsonBody(root -> root.stringValue("letter_id", LETTER_ID)).build();
    }

    private DslPart buildLetterStatusResponse() {
        return newJsonBody(root -> root
            .stringValue("id", LETTER_ID)
            .stringValue("status", "Created")
            .stringValue("message_id", "checksum")
            .stringValue("checksum", "checksum")
            .stringValue("created_at", "2026-08-14T10:15:30Z")
            .numberValue("copies", 1)).build();
    }
}
