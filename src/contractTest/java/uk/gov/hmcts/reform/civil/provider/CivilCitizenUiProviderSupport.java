package uk.gov.hmcts.reform.civil.provider;

import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.spring.junit5.MockMvcTestTarget;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.core.type.TypeReference;
import feign.FeignException;
import uk.gov.hmcts.reform.civil.filters.RequestFilter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.StringHttpMessageConverter;
import uk.gov.hmcts.reform.civil.config.JacksonConfiguration;
import uk.gov.hmcts.reform.civil.advice.ControllerExceptionHandler;
import uk.gov.hmcts.reform.civil.advice.ResourceExceptionHandler;
import uk.gov.hmcts.reform.civil.advice.UncaughtExceptionHandler;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.controllers.cases.CasesController;
import uk.gov.hmcts.reform.civil.controllers.fees.FeesController;
import uk.gov.hmcts.reform.civil.controllers.fees.FeesPaymentController;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.ga.service.GaFeesPaymentService;
import uk.gov.hmcts.reform.civil.model.CardPaymentStatusResponse;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.FeesPaymentService;
import uk.gov.hmcts.reform.civil.service.FeesService;
import uk.gov.hmcts.reform.civil.service.GeneralAppFeesService;
import uk.gov.hmcts.reform.civil.service.citizen.events.CaseEventService;
import uk.gov.hmcts.reform.civil.service.citizen.events.EventSubmissionParams;
import uk.gov.hmcts.reform.civil.service.user.UserInformationService;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.time.OffsetDateTime;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

abstract class CivilCitizenUiProviderSupport {

    private static final String AUTH_HEADER = "Bearer some-access-token";
    private static final String CASE_REFERENCE = "1234567890123456";
    private static final String PAYMENT_REFERENCE = "RC-1701-0909-0602-0418";
    private static final String CUI_CASE_REFERENCE = "1111222233334444";

    MockMvc mockMvc;

    @Mock
    private FeesPaymentService feesPaymentService;
    @Mock
    private GaFeesPaymentService gaFeesPaymentService;
    @Mock
    private FeesService feesService;
    @Mock
    private GeneralAppFeesService generalAppFeesService;
    @Mock
    private CoreCaseDataService coreCaseDataService;
    @Mock
    private CaseEventService caseEventService;
    @Mock
    private UserInformationService userInformationService;
    private AutoCloseable mocks;

    @BeforeEach
    void beforeEach(PactVerificationContext context) {
        String brokerUrl = System.getenv("PACT_BROKER_FULL_URL");
        if (brokerUrl != null && !brokerUrl.isBlank()) {
            System.setProperty("pactbroker.url", brokerUrl);
        }
        mocks = MockitoAnnotations.openMocks(this);
        FeesPaymentController paymentController = new FeesPaymentController(feesPaymentService, gaFeesPaymentService);
        FeesController feesController = new FeesController(
            feesService, generalAppFeesService, mock(uk.gov.hmcts.reform.civil.utils.InterestCalculator.class));
        CasesController casesController = new CasesController(
            mock(uk.gov.hmcts.reform.civil.service.RoleAssignmentsService.class), coreCaseDataService,
            mock(uk.gov.hmcts.reform.civil.ga.service.GaCoreCaseDataService.class),
            mock(uk.gov.hmcts.reform.civil.service.citizenui.DashboardClaimInfoService.class), caseEventService,
            mock(uk.gov.hmcts.reform.civil.ga.service.events.GaCaseEventService.class),
            mock(uk.gov.hmcts.reform.civil.service.search.CaseSdtRequestSearchService.class),
            mock(uk.gov.hmcts.reform.civil.service.bulkclaims.CaseworkerCaseEventService.class),
            mock(uk.gov.hmcts.reform.civil.service.citizenui.responsedeadline.DeadlineExtensionCalculatorService.class),
            mock(uk.gov.hmcts.reform.civil.validation.PostcodeValidator.class), userInformationService,
            mock(uk.gov.hmcts.reform.civil.service.citizen.repaymentplan.RepaymentPlanDecisionService.class));
        ObjectMapper mapper = buildObjectMapper();
        MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter(mapper);
        mockMvc = MockMvcBuilders.standaloneSetup(paymentController, feesController, casesController)
            .addFilters(new RequestFilter())
            .setMessageConverters(new StringHttpMessageConverter(), messageConverter)
            .setControllerAdvice(new ControllerExceptionHandler(), new ResourceExceptionHandler(mapper),
                                 new UncaughtExceptionHandler())
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
        java.util.Objects.requireNonNull(context, "No CUI Pact interaction loaded").verifyInteraction();
    }

    @State("Claim issue payment can be initiated for case 1234567890123456")
    void claimIssuePaymentExists() {
        when(feesPaymentService.createGovPaymentRequest(
            FeeType.CLAIMISSUED,
            CASE_REFERENCE,
            AUTH_HEADER
        )).thenReturn(
            new CardPaymentStatusResponse()
                .setExternalReference("2023-1701090705688")
                .setPaymentReference(PAYMENT_REFERENCE)
                .setStatus("Initiated")
                .setNextUrl("https://card.payments.service.gov.uk/secure/7b0716b2-40c4-413e-b62e-72c599c91960")
                .setDateCreated(OffsetDateTime.parse("2023-11-27T13:15:06.313+00:00"))
        );
    }

    @State("Payment status SUCCESS is available for payment RC-1701-0909-0602-0418")
    void paymentStatusSuccess() {
        when(feesPaymentService.getGovPaymentRequestStatus(
            FeeType.CLAIMISSUED,
            CASE_REFERENCE,
            PAYMENT_REFERENCE,
            AUTH_HEADER
        )).thenReturn(
            new CardPaymentStatusResponse()
                .setExternalReference("2023-1701090705688")
                .setPaymentReference(PAYMENT_REFERENCE)
                .setStatus("Success")
                .setPaymentFor("claimissued")
                .setPaymentAmount(new BigDecimal("200"))
        );
    }

    @State("Claim issue payment can be initiated for general application case 1234567890123456")
    void claimIssuePaymentExistsForGeneralApplication() {
        when(gaFeesPaymentService.createGovPaymentRequest(
            CASE_REFERENCE,
            AUTH_HEADER
        )).thenReturn(
            new CardPaymentStatusResponse()
                .setExternalReference("2023-1701090705688")
                .setPaymentReference(PAYMENT_REFERENCE)
                .setStatus("Initiated")
                .setNextUrl("https://card.payments.service.gov.uk/secure/7b0716b2-40c4-413e-b62e-72c599c91960")
                .setDateCreated(OffsetDateTime.parse("2023-11-27T13:15:06.313+00:00"))
        );
    }

    @State("Payment status SUCCESS is available for general application payment RC-1701-0909-0602-0418")
    void paymentStatusSuccessForGeneralApplication() {
        when(gaFeesPaymentService.getGovPaymentRequestStatus(
            CASE_REFERENCE,
            PAYMENT_REFERENCE,
            AUTH_HEADER
        )).thenReturn(
            new CardPaymentStatusResponse()
                .setExternalReference("2023-1701090705688")
                .setPaymentReference(PAYMENT_REFERENCE)
                .setStatus("Success")
                .setPaymentFor("claimissued")
                .setPaymentAmount(new BigDecimal("200"))
        );
    }

    @State("A claim issue fee is available for a claim amount of 1000")
    void claimIssueFeeExists() {
        when(feesService.getFeeDataByTotalClaimAmount(new BigDecimal("1000")))
            .thenReturn(new Fee(new BigDecimal("11500"), "FEE0209", "1"));
    }

    @State("Draft case 1111222233334444 can be submitted by the CUI user")
    void draftCaseCanBeSubmitted() {
        EventSubmissionParams expected = new EventSubmissionParams()
            .setAuthorisation(AUTH_HEADER)
            .setCaseId(CUI_CASE_REFERENCE)
            .setUserId("cui-user-id")
            .setEvent(CaseEvent.CREATE_LIP_CLAIM)
            .setUpdates(Map.of());
        when(caseEventService.submitEvent(expected)).thenReturn(contractCase());
    }

    @State("A draft individual-company claim can be submitted")
    void individualCompanyDraft() throws IOException {
        draftCanBeSubmitted("individual-company");
    }

    @State("A draft company-organisation claim can be submitted")
    void companyOrganisationDraft() throws IOException {
        draftCanBeSubmitted("company-organisation");
    }

    @State("A draft organisation-sole-trader claim can be submitted")
    void organisationSoleTraderDraft() throws IOException {
        draftCanBeSubmitted("organisation-sole-trader");
    }

    @State("A draft sole-trader-individual claim can be submitted")
    void soleTraderIndividualDraft() throws IOException {
        draftCanBeSubmitted("sole-trader-individual");
    }

    private void draftCanBeSubmitted(String variant) throws IOException {
        ObjectMapper mapper = buildObjectMapper();
        JsonNode examples;
        try (var stream = getClass().getResourceAsStream("/civil-cui-draft-claims.json")) {
            examples = mapper.readTree(java.util.Objects.requireNonNull(stream));
        }
        Map<String, Object> updates = mapper.convertValue(examples.get("common"), new TypeReference<>() { });
        updates.putAll(mapper.convertValue(examples.get("variants").get(variant), new TypeReference<>() { }));
        Map<String, Object> data = new HashMap<>(updates);
        data.put("legacyCaseReference", "000MC001");
        data.put("applicant1", mapper.convertValue(updates.get("applicant1"), uk.gov.hmcts.reform.civil.model.Party.class));
        data.put("respondent1", mapper.convertValue(updates.get("respondent1"), uk.gov.hmcts.reform.civil.model.Party.class));
        when(caseEventService.submitEvent(eventParams("draft", updates))).thenReturn(CaseDetails.builder()
            .id(Long.valueOf(CUI_CASE_REFERENCE))
            .state("PENDING_CASE_ISSUED")
            .lastModified(LocalDateTime.of(2025, 2, 3, 10, 15, 30))
            .data(data)
            .build());
    }

    @State("Citizen event submission returns callback errors and warnings")
    void callbackErrorsAndWarnings() throws IOException {
        rejectedEvent(Map.of("callbackErrors", List.of("Claim cannot be submitted"),
                             "callbackWarnings", List.of("Check the claim details")));
    }

    @State("Citizen event submission returns field validation errors")
    void fieldValidationErrors() throws IOException {
        rejectedEvent(Map.of("details", Map.of("field_errors", List.of(
            Map.of("id", "applicant1.partyEmail", "message", "Enter a valid email address")))));
    }

    @State("Citizen event submission returns no actionable validation fields")
    void noActionableValidationFields() throws IOException {
        rejectedEvent(Map.of("message", "Submission rejected"));
    }

    private void rejectedEvent(Map<String, Object> upstreamBody) throws IOException {
        // The real ResourceExceptionHandler must translate this downstream exception.
        when(caseEventService.submitEvent(eventParams(CUI_CASE_REFERENCE, Map.of())))
            .thenThrow(new FeignException.UnprocessableEntity("Submission rejected", mock(feign.Request.class),
                                                            buildObjectMapper().writeValueAsBytes(upstreamBody), Map.of()));
    }

    private EventSubmissionParams eventParams(String caseId, Map<String, Object> updates) {
        return new EventSubmissionParams()
            .setAuthorisation(AUTH_HEADER)
            .setCaseId(caseId)
            .setUserId("cui-user-id")
            .setEvent(CaseEvent.CREATE_LIP_CLAIM)
            .setUpdates(updates);
    }

    private CaseDetails contractCase() {
        return CaseDetails.builder()
            .id(Long.valueOf(CUI_CASE_REFERENCE))
            .state("PENDING_CASE_ISSUED")
            .data(Map.of("legacyCaseReference", "000MC001"))
            .build();
    }

    private ObjectMapper buildObjectMapper() {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        new JacksonConfiguration().jsonDateTimeFormatCustomizer().customize(builder);
        ObjectMapper mapper = builder.build();
        JacksonConfiguration.objectMapperJavaTimePostProcessor().postProcessAfterInitialization(mapper, "objectMapper");
        return mapper;
    }
}
