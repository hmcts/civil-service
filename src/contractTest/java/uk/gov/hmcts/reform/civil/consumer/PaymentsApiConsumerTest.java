package uk.gov.hmcts.reform.civil.consumer;

import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit.MockServerConfig;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.civil.config.PaymentsConfiguration;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.prd.model.ContactInformation;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.PaymentsService;
import uk.gov.hmcts.reform.payments.client.PaymentsClient;
import uk.gov.hmcts.reform.payments.client.models.PaymentDto;
import uk.gov.hmcts.reform.payments.request.CardPaymentRequest;
import uk.gov.hmcts.reform.payments.request.CardPaymentServiceRequestDTO;
import uk.gov.hmcts.reform.payments.request.CreateServiceRequestDTO;
import uk.gov.hmcts.reform.payments.request.PBAServiceRequestDTO;
import uk.gov.hmcts.reform.payments.response.CardPaymentServiceRequestResponse;
import uk.gov.hmcts.reform.payments.response.PBAServiceRequestResponse;
import uk.gov.hmcts.reform.payments.response.PaymentServiceResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@PactTestFor(providerName = "payment_creditAccountPayment")
@MockServerConfig(hostInterface = "localhost", port = "6670")
@TestPropertySource(properties = "payments.api.url=http://localhost:6670")
public class PaymentsApiConsumerTest extends BaseContractTest {

    private static final String SERVICE = "CIVIL";
    private static final String SITE_ID = "site_id";
    private static final String SPEC_SITE_ID = "spec_site_id";
    private static final Organisation ORGANISATION = new Organisation()
        .setName("test org")
        .setContactInformation(List.of(new ContactInformation()));
    private static final String PAYMENT_REFERENCE = "RC-1519-9028-2432-0001";
    private static final String PAYMENT_REFERENCE_PATH = "${paymentReference}";
    private static final String PAYMENT_REFERENCE_REGEX = "^RC-\\d{4}-\\d{4}-\\d{4}-\\d{4}$";
    private static final String SERVICE_REQUEST_REFERENCE = "2026-1700000000000001";
    private static final String SERVICE_REQUEST_REFERENCE_PATH = "${serviceRequestReference}";
    private static final String RETURN_URL = "https://civil-service/return";
    private static final String CALLBACK_URL = "https://civil-service/callback";
    protected static final String ACCOUNT_NUMBER = "PBA0077597";

    @Autowired
    private PaymentsService paymentsService;

    @Autowired
    private PaymentsClient paymentsClient;

    @MockBean
    AuthTokenGenerator authTokenGenerator;

    @MockBean
    private OrganisationService organisationService;

    @MockBean
    private PaymentsConfiguration paymentsConfiguration;

    private CaseData caseData = CaseDataBuilder.builder().buildClaimIssuedPaymentCaseDataWithPba(ACCOUNT_NUMBER);

    @Pact(consumer = "civil_service")
    public RequestResponsePact doCardPaymentRequest(PactDslWithProvider builder)
        throws JSONException, IOException {
        return buildCardPaymentRequestPact(builder);
    }

    @Pact(consumer = "civil_service")
    public RequestResponsePact createCardPayment(PactDslWithProvider builder) throws IOException {
        return builder
            .uponReceiving("a request to create a card payment")
            .path("/card-payments")
            .headers(
                AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN,
                SERVICE_AUTHORIZATION_HEADER, SERVICE_AUTH_TOKEN,
                "return-url", RETURN_URL,
                "service-callback-url", CALLBACK_URL
            )
            .method(HttpMethod.POST.toString())
            .body(createJsonObject(buildCardPaymentRequest()))
            .willRespondWith()
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(buildPaymentResponseDsl("Initiated"))
            .status(HttpStatus.SC_CREATED)
            .toPact();
    }

    @Pact(consumer = "civil_service")
    public RequestResponsePact retrieveCardPayment(PactDslWithProvider builder) {
        return builder
            .given("A card payment exists", Map.of("paymentReference", PAYMENT_REFERENCE))
            .uponReceiving("a request to retrieve a card payment")
            .pathFromProviderState(
                "/card-payments/" + PAYMENT_REFERENCE_PATH,
                "/card-payments/" + PAYMENT_REFERENCE
            )
            .headers(AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN, SERVICE_AUTHORIZATION_HEADER, SERVICE_AUTH_TOKEN)
            .method(HttpMethod.GET.toString())
            .willRespondWith()
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(buildPaymentResponseDsl("Success"))
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    @Pact(consumer = "civil_service")
    public RequestResponsePact retrieveCardPaymentStatus(PactDslWithProvider builder) {
        return builder
            .given("A card payment exists", Map.of("paymentReference", PAYMENT_REFERENCE))
            .uponReceiving("a request to retrieve a card payment status")
            .pathFromProviderState(
                "/card-payments/" + PAYMENT_REFERENCE_PATH + "/statuses",
                "/card-payments/" + PAYMENT_REFERENCE + "/statuses"
            )
            .headers(AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN, SERVICE_AUTHORIZATION_HEADER, SERVICE_AUTH_TOKEN)
            .method(HttpMethod.GET.toString())
            .willRespondWith()
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(buildPaymentResponseDsl("Success"))
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    @Pact(consumer = "civil_service")
    public RequestResponsePact createServiceRequest(PactDslWithProvider builder) throws IOException {
        return builder
            .uponReceiving("a request to create a payments service request")
            .path("/service-request")
            .headers(AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN, SERVICE_AUTHORIZATION_HEADER, SERVICE_AUTH_TOKEN)
            .method(HttpMethod.POST.toString())
            .body(createJsonObject(buildCreateServiceRequest()))
            .willRespondWith()
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(newJsonBody(root -> root.stringType("service_request_reference", SERVICE_REQUEST_REFERENCE)).build())
            .status(HttpStatus.SC_CREATED)
            .toPact();
    }

    @Pact(consumer = "civil_service")
    public RequestResponsePact createPbaPayment(PactDslWithProvider builder) throws IOException {
        return builder
            .given("A service request exists", Map.of("serviceRequestReference", SERVICE_REQUEST_REFERENCE))
            .uponReceiving("a request to create a PBA payment for a service request")
            .pathFromProviderState(
                "/service-request/" + SERVICE_REQUEST_REFERENCE_PATH + "/pba-payments",
                "/service-request/" + SERVICE_REQUEST_REFERENCE + "/pba-payments"
            )
            .headers(AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN, SERVICE_AUTHORIZATION_HEADER, SERVICE_AUTH_TOKEN)
            .method(HttpMethod.POST.toString())
            .body(createJsonObject(buildPbaServiceRequest()))
            .willRespondWith()
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(newJsonBody(root -> root
                .stringMatcher("payment_reference", PAYMENT_REFERENCE_REGEX, PAYMENT_REFERENCE)
                .stringType("status", "Success")
                .stringType("date_created", "2026-08-14T10:15:30.000Z")).build())
            .status(HttpStatus.SC_CREATED)
            .toPact();
    }

    @Pact(consumer = "civil_service")
    public RequestResponsePact createGovPayCardPaymentRequest(PactDslWithProvider builder) throws IOException {
        return builder
            .given("A service request exists", Map.of("serviceRequestReference", SERVICE_REQUEST_REFERENCE))
            .uponReceiving("a request to create a service request card payment")
            .pathFromProviderState(
                "/service-request/" + SERVICE_REQUEST_REFERENCE_PATH + "/card-payments",
                "/service-request/" + SERVICE_REQUEST_REFERENCE + "/card-payments"
            )
            .headers(AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN, SERVICE_AUTHORIZATION_HEADER, SERVICE_AUTH_TOKEN)
            .method(HttpMethod.POST.toString())
            .body(createJsonObject(buildCardPaymentServiceRequest()))
            .willRespondWith()
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(newJsonBody(root -> root
                .stringType("external_reference", "external-reference")
                .stringMatcher("payment_reference", PAYMENT_REFERENCE_REGEX, PAYMENT_REFERENCE)
                .stringType("status", "Initiated")
                .stringType("next_url", "https://payments/next")
                .stringType("date_created", "2026-08-14T10:15:30Z")).build())
            .status(HttpStatus.SC_CREATED)
            .toPact();
    }

    @BeforeEach
    void setUp() {
        when(paymentsConfiguration.getService()).thenReturn(SERVICE);
        when(paymentsConfiguration.getSiteId()).thenReturn(SITE_ID);
        when(paymentsConfiguration.getSpecSiteId()).thenReturn(SPEC_SITE_ID);
        when(organisationService.findOrganisationById(any())).thenReturn(Optional.of(ORGANISATION));
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN);
    }

    @Test
    @PactTestFor(pactMethod = "doCardPaymentRequest")
    public void verifyCreditCardPaymentRequest() {
        PaymentDto response = paymentsService.createCreditAccountPayment(caseData, AUTHORIZATION_TOKEN);
        assertThat(response.getStatus(), is("Success"));

    }

    @Test
    @PactTestFor(pactMethod = "createCardPayment")
    public void verifyCreateCardPayment() {
        PaymentDto response = paymentsClient.createCardPayment(
            AUTHORIZATION_TOKEN,
            buildCardPaymentRequest(),
            RETURN_URL,
            CALLBACK_URL
        );

        assertThat(response.getStatus(), is("Initiated"));
    }

    @Test
    @PactTestFor(pactMethod = "retrieveCardPayment")
    public void verifyRetrieveCardPayment() {
        PaymentDto response = paymentsClient.retrieveCardPayment(AUTHORIZATION_TOKEN, PAYMENT_REFERENCE);

        assertThat(response.getPaymentReference(), is(PAYMENT_REFERENCE));
    }

    @Test
    @PactTestFor(pactMethod = "retrieveCardPaymentStatus")
    public void verifyRetrieveCardPaymentStatus() {
        PaymentDto response = paymentsClient.getGovPayCardPaymentStatus(PAYMENT_REFERENCE, AUTHORIZATION_TOKEN);

        assertThat(response.getStatus(), is("Success"));
    }

    @Test
    @PactTestFor(pactMethod = "createServiceRequest")
    public void verifyCreateServiceRequest() {
        PaymentServiceResponse response = paymentsClient.createServiceRequest(
            AUTHORIZATION_TOKEN,
            buildCreateServiceRequest()
        );

        assertThat(response.getServiceRequestReference(), is(SERVICE_REQUEST_REFERENCE));
    }

    @Test
    @PactTestFor(pactMethod = "createPbaPayment")
    public void verifyCreatePbaPayment() {
        PBAServiceRequestResponse response = paymentsClient.createPbaPayment(
            SERVICE_REQUEST_REFERENCE,
            AUTHORIZATION_TOKEN,
            buildPbaServiceRequest()
        );

        assertThat(response.getPaymentReference(), is(PAYMENT_REFERENCE));
    }

    @Test
    @PactTestFor(pactMethod = "createGovPayCardPaymentRequest")
    public void verifyCreateGovPayCardPaymentRequest() {
        CardPaymentServiceRequestResponse response = paymentsClient.createGovPayCardPaymentRequest(
            SERVICE_REQUEST_REFERENCE,
            AUTHORIZATION_TOKEN,
            buildCardPaymentServiceRequest()
        );

        assertThat(response.getPaymentReference(), is(PAYMENT_REFERENCE));
    }

    private RequestResponsePact buildCardPaymentRequestPact(PactDslWithProvider builder) throws IOException {

        Map<String, Object> paymentMap = new HashMap<>();
        paymentMap.put("accountNumber", ACCOUNT_NUMBER);
        paymentMap.put("availableBalance", "1000.00");
        paymentMap.put("accountName", "test.account.name");

        return builder
            .given("An active account has sufficient funds for a payment", paymentMap)
            .uponReceiving("a request to create a payment in payments api with valid authorization")
            .path("/credit-account-payments")
            .headers(AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN)
            .method(HttpMethod.POST.toString())
            .body(createJsonObject(paymentsService.buildRequest(caseData)))
            .willRespondWith()
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(buildPBAPaymentResponseDsl("Success", "success", null, "Payment by account successful"))
            .status(HttpStatus.SC_CREATED)
            .toPact();
    }

    private DslPart buildPBAPaymentResponseDsl(String status, String paymentStatus, String errorCode, String errorMessage) {
        return getDslPart(status, paymentStatus, errorCode, errorMessage);
    }

    static DslPart getDslPart(String status, String paymentStatus, String errorCode, String errorMessage) {
        return newJsonBody((o) -> {
            o.stringMatcher("reference", PAYMENT_REFERENCE_REGEX, PAYMENT_REFERENCE)
                .stringType("status", status)
                .minArrayLike("status_histories", 1, 1,
                    (sh) -> {
                        sh.stringMatcher("date_updated",
                                "^(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{3}\\+\\d{4})$",
                                "2020-10-06T18:54:48.785+0000")
                            .stringMatcher("date_created",
                                "^(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{3}\\+\\d{4})$",
                                "2020-10-06T18:54:48.785+0000")
                            .stringValue("status", paymentStatus);
                        if (errorCode != null) {
                            sh.stringValue("error_code", errorCode);
                            sh.stringType("error_message",
                                errorMessage);
                        }
                    });
        }).build();
    }

    private DslPart buildPaymentResponseDsl(String status) {
        return newJsonBody(root -> root
            .stringType("reference", "reference")
            .stringMatcher("payment_reference", PAYMENT_REFERENCE_REGEX, PAYMENT_REFERENCE)
            .stringValue("status", status)
            .stringValue("currency", "GBP")
            .numberValue("amount", 100.00)).build();
    }

    private CardPaymentRequest buildCardPaymentRequest() {
        return CardPaymentRequest.builder()
            .amount(new BigDecimal("100.00"))
            .caseReference("000MC001")
            .ccdCaseNumber("1712345678901234")
            .channel("online")
            .description("Claim issue payment")
            .provider("govpay")
            .caseType("CIVIL")
            .fees(new uk.gov.hmcts.reform.payments.client.models.FeeDto[] {buildFee()})
            .build();
    }

    private CreateServiceRequestDTO buildCreateServiceRequest() {
        return CreateServiceRequestDTO.builder()
            .callBackUrl(CALLBACK_URL)
            .caseReference("000MC001")
            .ccdCaseNumber("1712345678901234")
            .hmctsOrgId(SITE_ID)
            .fees(new uk.gov.hmcts.reform.payments.client.models.FeeDto[] {buildFee()})
            .build();
    }

    private PBAServiceRequestDTO buildPbaServiceRequest() {
        return PBAServiceRequestDTO.builder()
            .accountNumber(ACCOUNT_NUMBER)
            .amount(new BigDecimal("100.00"))
            .customerReference("customer-reference")
            .idempotencyKey("idempotency-key")
            .organisationName("test org")
            .build();
    }

    private CardPaymentServiceRequestDTO buildCardPaymentServiceRequest() {
        return CardPaymentServiceRequestDTO.builder()
            .amount(new BigDecimal("100.00"))
            .language("en")
            .returnUrl(RETURN_URL)
            .build();
    }

    private uk.gov.hmcts.reform.payments.client.models.FeeDto buildFee() {
        return uk.gov.hmcts.reform.payments.client.models.FeeDto.builder()
            .calculatedAmount(new BigDecimal("100.00"))
            .code("FEE0204")
            .version("1")
            .volume(1)
            .build();
    }
}
