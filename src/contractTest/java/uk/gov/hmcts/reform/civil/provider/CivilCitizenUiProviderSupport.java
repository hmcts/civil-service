package uk.gov.hmcts.reform.civil.provider;

import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.spring.junit5.MockMvcTestTarget;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.advice.ControllerExceptionHandler;
import uk.gov.hmcts.reform.civil.advice.ResourceExceptionHandler;
import uk.gov.hmcts.reform.civil.advice.UncaughtExceptionHandler;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.config.JacksonConfiguration;
import uk.gov.hmcts.reform.civil.controllers.cases.CaseAssignmentController;
import uk.gov.hmcts.reform.civil.controllers.cases.CasesController;
import uk.gov.hmcts.reform.civil.controllers.fees.FeesController;
import uk.gov.hmcts.reform.civil.controllers.fees.FeesPaymentController;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.filters.RequestFilter;
import uk.gov.hmcts.reform.civil.ga.service.GaFeesPaymentService;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CardPaymentStatusResponse;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee2Dto;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.citizenui.DashboardClaimInfo;
import uk.gov.hmcts.reform.civil.model.citizenui.DashboardClaimStatus;
import uk.gov.hmcts.reform.civil.model.citizenui.DashboardResponse;
import uk.gov.hmcts.reform.civil.model.citizenui.GeneralApplicationFeeRequest;
import uk.gov.hmcts.reform.civil.model.citizenui.dto.RepaymentDecisionType;
import uk.gov.hmcts.reform.civil.model.repaymentplan.ClaimantProposedPlan;
import uk.gov.hmcts.reform.civil.service.AssignCaseService;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.FeesPaymentService;
import uk.gov.hmcts.reform.civil.service.FeesService;
import uk.gov.hmcts.reform.civil.service.GeneralAppFeesService;
import uk.gov.hmcts.reform.civil.service.citizen.defendant.LipDefendantCaseAssignmentService;
import uk.gov.hmcts.reform.civil.service.citizen.events.CaseEventService;
import uk.gov.hmcts.reform.civil.service.citizen.events.EventSubmissionParams;
import uk.gov.hmcts.reform.civil.service.citizen.repaymentplan.RepaymentPlanDecisionService;
import uk.gov.hmcts.reform.civil.service.citizenui.DashboardClaimInfoService;
import uk.gov.hmcts.reform.civil.service.citizenui.responsedeadline.DeadlineExtensionCalculatorService;
import uk.gov.hmcts.reform.civil.service.pininpost.DefendantPinToPostLRspecService;
import uk.gov.hmcts.reform.civil.service.pininpost.exception.PinNotMatchException;
import uk.gov.hmcts.reform.civil.service.search.CaseLegacyReferenceSearchService;
import uk.gov.hmcts.reform.civil.service.search.exceptions.SearchServiceCaseNotFoundException;
import uk.gov.hmcts.reform.civil.service.user.UserInformationService;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

// The legacy OCMC response needs a scoped Pact content-type override. Prevent
// concurrent tests from observing that temporary JVM property.
@org.junit.jupiter.api.parallel.Isolated
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
    @Mock
    private DashboardClaimInfoService dashboardClaimInfoService;
    @Mock
    private CaseLegacyReferenceSearchService referenceSearchService;
    @Mock
    private DefendantPinToPostLRspecService pinService;
    @Mock
    private AssignCaseService assignCaseService;
    @Mock
    private LipDefendantCaseAssignmentService lipAssignmentService;
    private Runnable stateVerification = () -> { };
    @Mock
    private InterestCalculator interestCalculator;
    @Mock
    private DeadlineExtensionCalculatorService deadlineCalculator;
    @Mock
    private RepaymentPlanDecisionService repaymentDecisionService;
    private boolean rawOcmcResponse;
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
            feesService, generalAppFeesService, interestCalculator);
        CasesController casesController = new CasesController(
            mock(uk.gov.hmcts.reform.civil.service.RoleAssignmentsService.class), coreCaseDataService,
            mock(uk.gov.hmcts.reform.civil.ga.service.GaCoreCaseDataService.class),
            dashboardClaimInfoService, caseEventService,
            mock(uk.gov.hmcts.reform.civil.ga.service.events.GaCaseEventService.class),
            mock(uk.gov.hmcts.reform.civil.service.search.CaseSdtRequestSearchService.class),
            mock(uk.gov.hmcts.reform.civil.service.bulkclaims.CaseworkerCaseEventService.class),
            deadlineCalculator,
            mock(uk.gov.hmcts.reform.civil.validation.PostcodeValidator.class), userInformationService,
            repaymentDecisionService);
        CaseAssignmentController assignmentController = new CaseAssignmentController(
            referenceSearchService, pinService, assignCaseService, lipAssignmentService, coreCaseDataService);
        stateVerification = () -> { };
        rawOcmcResponse = false;
        ObjectMapper mapper = buildObjectMapper();
        MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter(mapper);
        mockMvc = MockMvcBuilders.standaloneSetup(paymentController, feesController, casesController, assignmentController)
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
        try {
            stateVerification.run();
        } finally {
            if (mocks != null) {
                mocks.close();
            }
        }
    }

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void verifyPactInteractions(PactVerificationContext context) {
        java.util.Objects.requireNonNull(context, "No CUI Pact interaction loaded");
        if (!rawOcmcResponse) {
            context.verifyInteraction();
            return;
        }
        // This endpoint returns an unquoted URL with application/json. Compare its
        // exact text and actual header, without rewriting either. Diff generation
        // otherwise tries to pretty-print the raw URL as JSON, even on a match.
        String contentTypeKey = "pact.content_type.override.application/json";
        String diffKey = "pact.verifier.generateDiff";
        String previousContentType = System.getProperty(contentTypeKey);
        String previousDiff = System.getProperty(diffKey);
        try {
            System.setProperty(contentTypeKey, "text");
            System.setProperty(diffKey, "false");
            context.verifyInteraction();
        } finally {
            restoreProperty(contentTypeKey, previousContentType);
            restoreProperty(diffKey, previousDiff);
        }
    }

    private static void restoreProperty(String key, String value) {
        if (value == null) {
            System.clearProperty(key);
        } else {
            System.setProperty(key, value);
        }
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

    @State("Case details and claimant access are available")
    void claimantCaseAccess() {
        caseAccess(false, List.of("[CLAIMANT]"));
    }

    @State("Case details and defendant access are available")
    void defendantCaseAccess() {
        caseAccess(true, List.of("[DEFENDANT]"));
    }

    @State("Case details and no roles access are available")
    void emptyCaseAccess() {
        caseAccess(false, List.of());
    }

    private void caseAccess(boolean defendant, List<String> roles) {
        when(coreCaseDataService.getCase(Long.valueOf(CUI_CASE_REFERENCE), AUTH_HEADER))
            .thenReturn(readableCase(defendant));
        when(userInformationService.getUserCaseRoles(CUI_CASE_REFERENCE, AUTH_HEADER)).thenReturn(roles);
    }

    @State("The requested case does not exist")
    void missingCase() {
        when(coreCaseDataService.getCase(Long.valueOf(CUI_CASE_REFERENCE), AUTH_HEADER)).thenThrow(notFound());
    }

    private FeignException.NotFound notFound() {
        return new FeignException.NotFound("Case not found", mock(feign.Request.class), new byte[0], Map.of());
    }

    private CaseDetails readableCase(boolean defendant) {
        return CaseDetails.builder().id(Long.valueOf(CUI_CASE_REFERENCE)).caseTypeId("CIVIL")
            .state(defendant ? "AWAITING_RESPONDENT_ACKNOWLEDGEMENT" : "CASE_ISSUED")
            .lastModified(LocalDateTime.of(2025, 2, 3, 10, 15, 30))
            .data(Map.of(
                "legacyCaseReference", "000MC001", "totalClaimAmount", new BigDecimal("1000"),
                "applicant1", new Party().setType(Party.Type.INDIVIDUAL)
                    .setIndividualFirstName("Alex").setIndividualLastName("Example"),
                "respondent1", new Party().setType(Party.Type.COMPANY).setCompanyName("Example Services"),
                "businessProcess", new BusinessProcess().setCamundaEvent("CREATE_LIP_CLAIM")
                    .setStatus(defendant ? BusinessProcessStatus.STARTED : BusinessProcessStatus.FINISHED)))
            .build();
    }

    @State("The claimant dashboard page 1 is available")
    void claimantDashboard() {
        when(dashboardClaimInfoService.getDashboardClaimantResponse(AUTH_HEADER, "cui-user-id", 1))
            .thenReturn(dashboard());
    }

    @State("The defendant dashboard page 1 is available")
    void defendantDashboard() {
        when(dashboardClaimInfoService.getDashboardDefendantResponse(AUTH_HEADER, "cui-user-id", 1))
            .thenReturn(dashboard());
    }

    @State("The claimant dashboard page 2 is available")
    void emptyClaimantDashboard() {
        when(dashboardClaimInfoService.getDashboardClaimantResponse(AUTH_HEADER, "cui-user-id", 2))
            .thenReturn(new DashboardResponse(List.of(), 0));
    }

    @State("The defendant dashboard page 2 is available")
    void emptyDefendantDashboard() {
        when(dashboardClaimInfoService.getDashboardDefendantResponse(AUTH_HEADER, "cui-user-id", 2))
            .thenReturn(new DashboardResponse(List.of(), 0));
    }

    private DashboardResponse dashboard() {
        return new DashboardResponse(List.of(dashboardItem(false), dashboardItem(true)), 2);
    }

    private DashboardClaimInfo dashboardItem(boolean ocmc) {
        return new DashboardClaimInfo().setClaimId(ocmc ? "2222333344445555" : CUI_CASE_REFERENCE)
            .setClaimNumber(ocmc ? "000MC002" : "000MC001").setClaimantName("Alex Example")
            .setDefendantName("Example Services").setClaimAmount(new BigDecimal("1000.00"))
            .setStatus(DashboardClaimStatus.CASE_DISMISSED).setOcmc(ocmc);
    }

    @State("A Civil reference and PIN are valid")
    void validCivilPin() {
        CaseDetails details = readableCase(false);
        when(referenceSearchService.getCaseDataByLegacyReference("000MC001")).thenReturn(details);
        stateVerification = () -> verify(pinService).validatePin(details, "123456");
    }

    @State("Civil PIN validation fails for invalid PIN")
    void invalidCivilPin() {
        CaseDetails details = readableCase(false);
        when(referenceSearchService.getCaseDataByLegacyReference("000MC001")).thenReturn(details);
        doThrow(new PinNotMatchException()).when(pinService).validatePin(details, "123456");
    }

    @State("Civil PIN validation fails for missing claim")
    void missingCivilPinClaim() {
        when(referenceSearchService.getCaseDataByLegacyReference("000MC001"))
            .thenThrow(new SearchServiceCaseNotFoundException());
    }

    @State("An OCMC reference and PIN are valid")
    void validOcmcPin() {
        rawOcmcResponse = true;
        when(pinService.validateOcmcPin("12345678", "000MC001"))
            .thenReturn("https://moneyclaims.aat.platform.hmcts.net/claim/000MC001");
    }

    @State("An OCMC PIN is invalid")
    void invalidOcmcPin() {
        when(pinService.validateOcmcPin("12345678", "000MC001")).thenThrow(new PinNotMatchException());
    }

    @State("Defendant link status is linked")
    void linkedDefendant() {
        defendantLink(true);
    }

    @State("Defendant link status is unlinked")
    void unlinkedDefendant() {
        defendantLink(false);
    }

    private void defendantLink(boolean linked) {
        CaseDetails details = readableCase(false);
        when(referenceSearchService.getCivilOrOcmcCaseDataByCaseReference("000MC001")).thenReturn(details);
        when(pinService.isDefendantLinked(details)).thenReturn(linked);
    }

    @State("Defendant link status is no search result")
    void missingLinkSearchResult() {
        when(referenceSearchService.getCivilOrOcmcCaseDataByCaseReference("000MC001")).thenReturn(null);
    }

    @State("Defendant link status is upstream not found")
    void missingLinkUpstream() {
        when(referenceSearchService.getCivilOrOcmcCaseDataByCaseReference("000MC001")).thenThrow(notFound());
    }

    @State("Defendant link status is provider failure")
    void linkProviderFailure() {
        when(referenceSearchService.getCivilOrOcmcCaseDataByCaseReference("000MC001"))
            .thenThrow(new IllegalStateException("Search unavailable"));
    }

    @State("Defendant assignment is accepted")
    void acceptedAssignment() {
        CaseDetails details = readableCase(false);
        when(coreCaseDataService.getCase(Long.valueOf(CUI_CASE_REFERENCE))).thenReturn(details);
        stateVerification = () -> {
            verify(pinService).validatePin(details, "123456");
            verify(assignCaseService).assignCase(AUTH_HEADER, CUI_CASE_REFERENCE, Optional.of(CaseRole.DEFENDANT));
            verify(lipAssignmentService).addLipDefendantToCaseDefendantUserDetails(
                AUTH_HEADER, CUI_CASE_REFERENCE, Optional.of(CaseRole.DEFENDANT), Optional.of(details));
        };
    }

    @State("Defendant assignment is rejected PIN")
    void rejectedAssignment() {
        CaseDetails details = readableCase(false);
        when(coreCaseDataService.getCase(Long.valueOf(CUI_CASE_REFERENCE))).thenReturn(details);
        doThrow(new PinNotMatchException()).when(pinService).validatePin(details, "123456");
        stateVerification = () -> verifyNoInteractions(assignCaseService, lipAssignmentService);
    }

    private JsonNode calculationExamples() throws IOException {
        try (var input = getClass().getResourceAsStream("/civil-cui-calculations.json")) {
            return buildObjectMapper().readTree(java.util.Objects.requireNonNull(input));
        }
    }

    private void interestExample(String name) throws IOException {
        JsonNode example = calculationExamples().get("interest").get(name);
        CaseData expected = buildObjectMapper().treeToValue(example.get("request"), CaseData.class);
        when(interestCalculator.getInterestValidationErrors(expected)).thenReturn(List.of());
        when(interestCalculator.calculateInterest(expected)).thenReturn(example.get("result").decimalValue());
        stateVerification = () -> {
            verify(interestCalculator).getInterestValidationErrors(expected);
            verify(interestCalculator).calculateInterest(expected);
        };
    }

    @State("Claim interest statutory can be calculated")
    void statutoryInterest() throws IOException {
        interestExample("statutory");
    }

    @State("Claim interest different rate zero can be calculated")
    void zeroInterest() throws IOException {
        interestExample("different rate zero");
    }

    @State("Claim interest breakdown can be calculated")
    void breakdownInterest() throws IOException {
        interestExample("breakdown");
    }

    @State("A hearing fee is available for a claim amount of 1000")
    void hearingFee() {
        when(feesService.getHearingFeeDataByTotalClaimAmount(new BigDecimal("1000")))
            .thenReturn(new Fee(new BigDecimal("30300"), "FEE0442", "2"));
    }

    @State("Flat and percentage fee ranges are available")
    void feeRanges() throws IOException {
        List<Fee2Dto> ranges = buildObjectMapper().convertValue(calculationExamples().get("ranges"),
                                                              new TypeReference<List<Fee2Dto>>() { });
        when(feesService.getFeeRange()).thenReturn(ranges);
    }

    private void generalApplicationFee(String name) throws IOException {
        GeneralApplicationFeeRequest expected = buildObjectMapper().treeToValue(
            calculationExamples().get("generalApplication").get(name), GeneralApplicationFeeRequest.class);
        when(generalAppFeesService.getFeeForGALiP(expected.getApplicationTypes(), expected.getWithConsent(),
                                                 expected.getWithNotice(), expected.getHearingDate()))
            .thenReturn(new Fee(new BigDecimal("30300"), "FEE0442", "2"));
    }

    @State("A General Application fee for consent is available")
    void consentFee() throws IOException {
        generalApplicationFee("consent");
    }

    @State("A General Application fee for notice is available")
    void noticeFee() throws IOException {
        generalApplicationFee("notice");
    }

    @State("A General Application fee for without notice is available")
    void withoutNoticeFee() throws IOException {
        generalApplicationFee("without notice");
    }

    @State("A General Application fee for adjourn hearing is available")
    void adjournHearingFee() throws IOException {
        generalApplicationFee("adjourn hearing");
    }

    @State("A response deadline with 0 extra days can be calculated")
    void responseDeadlinePlus0() {
        when(deadlineCalculator.calculateExtendedDeadline(LocalDate.of(2025, 2, 3), 0))
            .thenReturn(LocalDate.parse("2025-02-03"));
    }

    @State("A response deadline with 5 extra days can be calculated")
    void responseDeadlinePlus5() {
        when(deadlineCalculator.calculateExtendedDeadline(LocalDate.of(2025, 2, 3), 5))
            .thenReturn(LocalDate.parse("2025-02-10"));
    }

    @State("An agreed response deadline is present")
    void agreedDeadlinePresent() {
        when(coreCaseDataService.getAgreedDeadlineResponseDate(Long.valueOf(CUI_CASE_REFERENCE), AUTH_HEADER))
            .thenReturn(LocalDate.of(2025, 2, 10));
    }

    @State("An agreed response deadline is absent")
    void agreedDeadlineAbsent() {
        when(coreCaseDataService.getAgreedDeadlineResponseDate(Long.valueOf(CUI_CASE_REFERENCE), AUTH_HEADER))
            .thenReturn(null);
    }

    private void repaymentDecision(RepaymentDecisionType decision) throws IOException {
        ClaimantProposedPlan expected = buildObjectMapper().treeToValue(
            calculationExamples().get("repayment"), ClaimantProposedPlan.class);
        CaseDetails details = readableCase(false);
        when(coreCaseDataService.getCase(Long.valueOf(CUI_CASE_REFERENCE), AUTH_HEADER)).thenReturn(details);
        when(repaymentDecisionService.getCalculatedDecision(details, expected)).thenReturn(decision);
    }

    @State("The repayment decision is IN_FAVOUR_OF_CLAIMANT")
    void repaymentDecisionForClaimant() throws IOException {
        repaymentDecision(RepaymentDecisionType.IN_FAVOUR_OF_CLAIMANT);
    }

    @State("The repayment decision is IN_FAVOUR_OF_DEFENDANT")
    void repaymentDecisionForDefendant() throws IOException {
        repaymentDecision(RepaymentDecisionType.IN_FAVOUR_OF_DEFENDANT);
    }

    private ObjectMapper buildObjectMapper() {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        new JacksonConfiguration().jsonDateTimeFormatCustomizer().customize(builder);
        ObjectMapper mapper = builder.build();
        JacksonConfiguration.objectMapperJavaTimePostProcessor().postProcessAfterInitialization(mapper, "objectMapper");
        return mapper;
    }
}
