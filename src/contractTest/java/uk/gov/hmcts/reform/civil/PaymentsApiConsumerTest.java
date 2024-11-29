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
import uk.gov.hmcts.reform.payments.client.models.FeeDto;
import uk.gov.hmcts.reform.payments.client.models.PaymentDto;
import uk.gov.hmcts.reform.payments.request.CreditAccountPaymentRequest;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
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

    public static final String PAYMENT_REQUEST_ENDPOINT_SUFFIX = "/credit-account-payments";
    private static final String SERVICE = "service";
    private static final String SITE_ID = "site_id";
    private static final String SPEC_SITE_ID = "spec_site_id";
    private static final Organisation ORGANISATION = Organisation.builder()
        .name("test org")
        .contactInformation(List.of(ContactInformation.builder().build()))
        .build();

    @Autowired
    private PaymentsService paymentsService;

    @MockBean
    AuthTokenGenerator authTokenGenerator;

    @MockBean
    private OrganisationService organisationService;

    @MockBean
    private PaymentsConfiguration paymentsConfiguration;

    @Pact(consumer = "civil_service")
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
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN);
    }

    @Test
    @PactTestFor(pactMethod = "doCardPaymentRequest")
    public void verifyCreditCardPaymentRequest() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build();
        PaymentDto response = paymentsService.createCreditAccountPayment(caseData, AUTHORIZATION_TOKEN);
        assertThat(response.getStatus(), is("Success"));

    }

    private RequestResponsePact buildCardPaymentRequestPact(PactDslWithProvider builder) throws IOException {
        return builder
            .given("a request to create a payment in payments api")
            .uponReceiving("a request to create a payment in payments api with valid authorization")
            .pathFromProviderState(
                PAYMENT_REQUEST_ENDPOINT_SUFFIX,
                PAYMENT_REQUEST_ENDPOINT_SUFFIX
            )
            .headers(AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN, SERVICE_AUTHORIZATION_HEADER, SERVICE_AUTH_TOKEN)
            .method(HttpMethod.POST.toString())
            .body(createJsonObject(buildCreditAccountPaymentRequest()))
            .willRespondWith()
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(buildPBAPaymentResponseDsl("Success", "success", null, "Payment by account successful"))
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    private CreditAccountPaymentRequest buildCreditAccountPaymentRequest() {
        return CreditAccountPaymentRequest.builder()
            .accountNumber("PBA0077597")
            .amount(BigDecimal.valueOf(1.00).setScale(2))
            .caseReference("000DC001")
            .ccdCaseNumber("1594901956117591")
            .currency("GBP")
            .customerReference("12345")
            .description("Claim issue payment")
            .organisationName("test org")
            .service("service")
            .siteId("site_id")
            .fees(new FeeDto[] {
                FeeDto.builder()
                    .calculatedAmount(BigDecimal.valueOf(1.00).setScale(2))
                    .code("CODE")
                    .version("1")
                    .build()
            })
            .build();
    }

    private DslPart buildPBAPaymentResponseDsl(String status, String paymentStatus, String errorCode, String errorMessage) {
        return getDslPart(status, paymentStatus, errorCode, errorMessage);
    }

    static DslPart getDslPart(String status, String paymentStatus, String errorCode, String errorMessage) {
        return newJsonBody((o) -> {
            o.stringType("reference", "reference")
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
}
