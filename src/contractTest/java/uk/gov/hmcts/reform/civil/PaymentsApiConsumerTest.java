package uk.gov.hmcts.reform.civil;

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
import uk.gov.hmcts.reform.hmc.service.HearingsService;
import uk.gov.hmcts.reform.payments.client.PaymentsApi;
import uk.gov.hmcts.reform.payments.client.models.PaymentDto;
import uk.gov.hmcts.reform.payments.request.CardPaymentServiceRequestDTO;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@PactTestFor(providerName = "payment_api")
@MockServerConfig(hostInterface = "localhost", port = "6670")
@TestPropertySource(properties = "payments.api.url=http://localhost:6670")
public class PaymentsApiConsumerTest extends BaseContractTest {

    public static final String PAYMENT_REQUEST_ENDPOINT_PREFIX = "/service-request/";
    public static final String PAYMENT_REQUEST_ENDPOINT_SUFFIX = "/card-payments";
    private static final String SERVICE_REQUEST_ID_SUFFIX = "${service-request-reference}";
    private static final String REFERENCE = "123456789";
    private static final String SERVICE = "service";
    private static final String SITE_ID = "site_id";
    private static final String SPEC_SITE_ID = "spec_site_id";
    private static final Organisation ORGANISATION = Organisation.builder()
        .name("test org")
        .contactInformation(List.of(ContactInformation.builder().build()))
        .build();

    @Autowired
    private PaymentsService paymentsService;

    @Autowired
    private PaymentsApi paymentsApi;

    @MockBean
    AuthTokenGenerator authTokenGenerator;

    @MockBean
    private HearingsService hearingsService;

    @MockBean
    private OrganisationService organisationService;

    @MockBean
    private PaymentsConfiguration paymentsConfiguration;

    @Pact(consumer = "civil-service")
    public RequestResponsePact doCardPaymentRequest(PactDslWithProvider builder)
        throws JSONException, IOException {
        return buildCardPaymentRequestPact(builder);
    }

    @BeforeEach
    void setUp() {
        when(paymentsConfiguration.getService()).thenReturn(SERVICE);
        when(paymentsConfiguration.getSiteId()).thenReturn(SITE_ID);
        when(paymentsConfiguration.getSpecSiteId()).thenReturn(SPEC_SITE_ID);
        when(organisationService.findOrganisationById(any())).thenReturn(Optional.of(ORGANISATION));
        when(authTokenGenerator.generate()).thenReturn(AUTHORIZATION_TOKEN);
    }

    @Test
    @PactTestFor(pactMethod = "doCardPaymentRequest")
    public void verifyCreditCardPaymentRequest() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build();
        PaymentDto response = paymentsService.createCreditAccountPayment(caseData, AUTHORIZATION_TOKEN);
        assertThat(response.getExternalReference(), is("DUMMY-EXT-REF"));
        assertThat(response.getStatus(), is("Initiated"));

    }

    private RequestResponsePact buildCardPaymentRequestPact(PactDslWithProvider builder) throws IOException {
        return builder
            .given("a request to create a payment in payments api")
            .uponReceiving("a request to create a payment in payments api with valid authorization")
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
            .toPact();
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
                .stringValue("external_reference", "DUMMY-EXT-REF")
                .stringValue("payment_reference", "DUMMY-PAYMENT-REF")
                .stringValue("status", "Initiated")
                .stringValue("next_url", "cui-page.hmcts.platform.net")
                .stringValue("date_created", "2020-02-20T20:20:20.222+0000")
        ).build();
    }

}
