package uk.gov.hmcts.reform.civil;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit.MockServerConfig;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.payments.client.PaymentsApi;
import uk.gov.hmcts.reform.payments.client.models.PaymentDto;
import uk.gov.hmcts.reform.payments.request.CardPaymentServiceRequestDTO;
import uk.gov.hmcts.reform.payments.response.CardPaymentServiceRequestResponse;

import java.io.IOException;
import java.math.BigDecimal;

import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@PactTestFor(providerName = "payments-api")
@TestPropertySource(properties = "payments.api.url=http://localhost:8765")
@MockServerConfig(hostInterface = "localhost", port = "6670")
public class PaymentsApiConsumerTest extends BaseContractTest {

    public static final String PAYMENT_REQUEST_ENDPOINT_PREFIX = "/service-request/";
    public static final String PAYMENT_REQUEST_ENDPOINT_SUFFIX = "/card-payments";
    private static final String SERVICE_REQUEST_ID_SUFFIX = "${service-request-reference}";

    public static final String STATUS_ENDPOINT_PREFIX = "/card-payments/";
    public static final String STATUS_ENDPOINT_SUFFIX = "/statuses";
    private static final String PAYMENT_REFERENCE_ID_SUFFIX = "${paymentReference}";
    private static final String REFERENCE = "123456789";

    @Autowired
    private PaymentsApi paymentsApi;

    @Nested
    class CardPayments {

        @Pact(consumer = "civil-service")
        public V4Pact getStatusOfPayment(PactDslWithProvider builder)
            throws JSONException, IOException {
            return buildStatusOfPaymentPact(builder);
        }

        @Test
        @PactTestFor(pactMethod = "getStatusOfPayment")
        public void verifyPaymentSuccess() {
            PaymentDto response = paymentsApi.retrieveCardPaymentStatus(REFERENCE, AUTHORIZATION_TOKEN, SERVICE_AUTH_TOKEN);
            assertThat(response.getStatus(), is("Success"));
        }

        public V4Pact buildStatusOfPaymentPact(PactDslWithProvider builder) throws IOException {
            return builder
                .given("The status of a payment request needs to be checked")
                .uponReceiving("a request for status for a payment reference")
                .pathFromProviderState(
                    STATUS_ENDPOINT_PREFIX + PAYMENT_REFERENCE_ID_SUFFIX + STATUS_ENDPOINT_SUFFIX,
                    STATUS_ENDPOINT_PREFIX + REFERENCE + STATUS_ENDPOINT_SUFFIX
                )
                .headers(AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN, SERVICE_AUTHORIZATION_HEADER, SERVICE_AUTH_TOKEN)
                .method(HttpMethod.GET.toString())
                .willRespondWith()
                .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(buildPaymentStatusResponseDsl())
                .status(HttpStatus.SC_OK)
                .toPact(V4Pact.class);
        }

        public static DslPart buildPaymentStatusResponseDsl() {
            return newJsonBody(response ->
                                   response
                                       .stringValue("externalReference", "DUMMY-EXT-REF")
                                       .stringValue("paymentReference", "DUMMY-PAYMENT-REF")
                                       .stringValue("status", "Success")
                                       .stringValue("dateCreated","2020-02-20T20:20:20.222+0000")
            ).build();
        }

        @Pact(consumer = "civil-service")
        public V4Pact doCardPaymentRequest(PactDslWithProvider builder)
            throws JSONException, IOException {
            return buildCardPaymentRequestPact(builder);
        }

        @Test
        @PactTestFor(pactMethod = "doCardPaymentRequest")
        public void verifyPostOfPaymentRequest() {
            CardPaymentServiceRequestResponse response = paymentsApi.createGovPayCardPaymentRequest(REFERENCE, AUTHORIZATION_TOKEN, SERVICE_AUTH_TOKEN, buildPaymentRequest());
            assertThat(response.getExternalReference(), is("DUMMY-EXT-REF"));
            assertThat(response.getStatus(), is("Initiated"));
            assertThat(response.getNextUrl(), is("cui-page.hmcts.platform.net"));
        }

        private V4Pact buildCardPaymentRequestPact(PactDslWithProvider builder) throws IOException {
            return builder
                .given("Post a payment request to pay service request")
                .uponReceiving("a request payment")
                .pathFromProviderState(
                    PAYMENT_REQUEST_ENDPOINT_PREFIX + SERVICE_REQUEST_ID_SUFFIX + PAYMENT_REQUEST_ENDPOINT_SUFFIX,
                    PAYMENT_REQUEST_ENDPOINT_PREFIX + REFERENCE + PAYMENT_REQUEST_ENDPOINT_SUFFIX
                )
                .headers(AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN, SERVICE_AUTHORIZATION_HEADER, SERVICE_AUTH_TOKEN)
                .method(HttpMethod.POST.toString())
                .willRespondWith()
                .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(buildDoPaymentResponseDsl())
                .status(HttpStatus.SC_OK)
                .toPact(V4Pact.class);
        }

        public CardPaymentServiceRequestDTO buildPaymentRequest() {
            return CardPaymentServiceRequestDTO.builder()
                .language("En")
                .currency("GBP")
                .amount(new BigDecimal(5000))
                .returnUrl("cui-page.hmcts.platform.net")
                .build();

        }

        public static DslPart buildDoPaymentResponseDsl() {
            return newJsonBody(response ->
                                   response
                                       .stringValue("externalReference", "DUMMY-EXT-REF")
                                       .stringValue("paymentReference", "DUMMY-PAYMENT-REF")
                                       .stringValue("status", "Initiated")
                                       .stringValue("nextUrl", "cui-page.hmcts.platform.net")
                                       .stringValue("dateCreated","2020-02-20T20:20:20.222+0000")
            ).build();
        }
    }
}
