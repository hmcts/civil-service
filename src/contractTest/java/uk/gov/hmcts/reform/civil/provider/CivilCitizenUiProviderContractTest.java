package uk.gov.hmcts.reform.civil.provider;

import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.IgnoreNoPactsToVerify;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.junitsupport.loader.PactBrokerConsumerVersionSelectors;
import au.com.dius.pact.provider.junitsupport.loader.PactFolder;
import au.com.dius.pact.provider.junitsupport.loader.SelectorBuilder;
import au.com.dius.pact.provider.spring.junit5.MockMvcTestTarget;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.civil.controllers.fees.FeesPaymentController;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.ga.service.GaFeesPaymentService;
import uk.gov.hmcts.reform.civil.model.CardPaymentStatusResponse;
import uk.gov.hmcts.reform.civil.service.FeesPaymentService;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Provider("civil_service")
@PactBroker(
    url = "${PACT_BROKER_FULL_URL:http://localhost:80}",
    providerBranch = "${pact.provider.branch}"
)
@PactFolder("src/contractTest/resources/pacts")
@IgnoreNoPactsToVerify
class CivilCitizenUiProviderContractTest {

    private static final String AUTH_HEADER = "Bearer some-access-token";
    private static final String CASE_REFERENCE = "1234567890123456";
    private static final String PAYMENT_REFERENCE = "RC-1701-0909-0602-0418";

    private MockMvc mockMvc;

    @Mock
    private FeesPaymentService feesPaymentService;
    @Mock
    private GaFeesPaymentService gaFeesPaymentService;
    private AutoCloseable mocks;

    @PactBrokerConsumerVersionSelectors
    public static SelectorBuilder consumerVersionSelectors() {
        return new SelectorBuilder()
            .matchingBranch()
            .mainBranch()
            .deployedOrReleased();
    }

    @BeforeAll
    static void requirePactBroker() {
        Assumptions.assumeTrue(
            StringUtils.hasText(System.getenv("PACT_BROKER_FULL_URL")),
            "PACT_BROKER_FULL_URL must be set to run provider verification tests"
        );
    }

    @BeforeEach
    void beforeEach(PactVerificationContext context) {
        String brokerUrl = System.getenv("PACT_BROKER_FULL_URL");
        if (brokerUrl != null && !brokerUrl.isBlank()) {
            System.setProperty("pactbroker.url", brokerUrl);
        } else {
            System.clearProperty("pactbroker.url");
        }
        mocks = MockitoAnnotations.openMocks(this);
        FeesPaymentController controller = new FeesPaymentController(feesPaymentService, gaFeesPaymentService);
        MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter(buildObjectMapper());
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setMessageConverters(messageConverter)
            .alwaysDo(result -> result.getResponse().setContentType(APPLICATION_JSON_VALUE))
            .build();
        MockMvcTestTarget target = new MockMvcTestTarget();
        target.setMockMvc(mockMvc);
        if (context != null) {
            context.setTarget(target);
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        if (mocks != null) {
            mocks.close();
        }
    }

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void verifyPactInteractions(PactVerificationContext context) {
        if (context != null) {
            context.verifyInteraction();
        }
    }

    @State("Claim issue payment can be initiated for case 1234567890123456")
    void claimIssuePaymentExists() {
        when(feesPaymentService.createGovPaymentRequest(
            eq(FeeType.CLAIMISSUED),
            eq(CASE_REFERENCE),
            anyString()
        )).thenReturn(
            CardPaymentStatusResponse.builder()
                .externalReference("2023-1701090705688")
                .paymentReference(PAYMENT_REFERENCE)
                .status("Initiated")
                .nextUrl("https://card.payments.service.gov.uk/secure/7b0716b2-40c4-413e-b62e-72c599c91960")
                .dateCreated(OffsetDateTime.parse("2023-11-27T13:15:06.313+00:00"))
                .build()
        );
    }

    @State("Payment status SUCCESS is available for payment RC-1701-0909-0602-0418")
    void paymentStatusSuccess() {
        when(feesPaymentService.getGovPaymentRequestStatus(
            eq(FeeType.CLAIMISSUED),
            eq(CASE_REFERENCE),
            eq(PAYMENT_REFERENCE),
            anyString()
        )).thenReturn(
            CardPaymentStatusResponse.builder()
                .externalReference("2023-1701090705688")
                .paymentReference(PAYMENT_REFERENCE)
                .status("Success")
                .paymentFor("claimissued")
                .paymentAmount(new BigDecimal("200"))
                .build()
        );
    }

    @State("Claim issue payment can be initiated for general application case 1234567890123456")
    void claimIssuePaymentExistsForGeneralApplication() {
        when(gaFeesPaymentService.createGovPaymentRequest(
            CASE_REFERENCE,
            AUTH_HEADER
        )).thenReturn(
            CardPaymentStatusResponse.builder()
                .externalReference("2023-1701090705688")
                .paymentReference(PAYMENT_REFERENCE)
                .status("Initiated")
                .nextUrl("https://card.payments.service.gov.uk/secure/7b0716b2-40c4-413e-b62e-72c599c91960")
                .dateCreated(OffsetDateTime.parse("2023-11-27T13:15:06.313+00:00"))
                .build()
        );
    }

    @State("Payment status SUCCESS is available for general application payment RC-1701-0909-0602-0418")
    void paymentStatusSuccessForGeneralApplication() {
        when(gaFeesPaymentService.getGovPaymentRequestStatus(
            CASE_REFERENCE,
            PAYMENT_REFERENCE,
            AUTH_HEADER
        )).thenReturn(
            CardPaymentStatusResponse.builder()
                .externalReference("2023-1701090705688")
                .paymentReference(PAYMENT_REFERENCE)
                .status("Success")
                .paymentFor("claimissued")
                .paymentAmount(new BigDecimal("200"))
                .build()
        );
    }

    private ObjectMapper buildObjectMapper() {
        ObjectMapper mapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .build();
        SimpleModule module = new SimpleModule();
        module.addSerializer(OffsetDateTime.class, new OffsetDateTimeEpochSecondsSerializer());
        mapper.registerModule(module);
        return mapper;
    }

    private static class OffsetDateTimeEpochSecondsSerializer extends com.fasterxml.jackson.databind.JsonSerializer<OffsetDateTime> {

        @Override
        public void serialize(OffsetDateTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (value == null) {
                gen.writeNull();
                return;
            }
            long epochMillis = value.toInstant().toEpochMilli();
            BigDecimal epochSeconds = BigDecimal.valueOf(epochMillis, 3);
            gen.writeNumber(epochSeconds);
        }
    }
}
