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
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.civil.advice.ControllerExceptionHandler;
import uk.gov.hmcts.reform.civil.advice.ResourceExceptionHandler;
import uk.gov.hmcts.reform.civil.advice.UncaughtExceptionHandler;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.config.JacksonConfiguration;
import uk.gov.hmcts.reform.civil.controllers.cases.CaseAssignmentController;
import uk.gov.hmcts.reform.civil.controllers.cases.CasesController;
import uk.gov.hmcts.reform.civil.controllers.cases.DocumentController;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentNotFoundException;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DownloadedDocumentResponse;
import uk.gov.hmcts.reform.civil.documentmanagement.model.UploadedDocument;
import uk.gov.hmcts.reform.civil.controllers.fees.FeesController;
import uk.gov.hmcts.reform.civil.controllers.fees.FeesPaymentController;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.filters.RequestFilter;
import uk.gov.hmcts.reform.civil.ga.service.GaFeesPaymentService;
import uk.gov.hmcts.reform.civil.ga.service.GaCoreCaseDataService;
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
import uk.gov.hmcts.reform.civil.service.documentmanagement.ClaimFormService;
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
import uk.gov.hmcts.reform.dashboard.controllers.DashboardController;
import uk.gov.hmcts.reform.dashboard.data.Notification;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.data.TaskList;
import uk.gov.hmcts.reform.dashboard.data.TaskStatus;
import uk.gov.hmcts.reform.dashboard.entities.TaskListEntity;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;
import uk.gov.hmcts.reform.dashboard.services.TaskListService;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.doThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
    private static final String CUI_GA_CASE_REFERENCE = "2222333344445555";

    MockMvc mockMvc;

    @Mock
    private FeesPaymentService feesPaymentService;
    @Mock
    private GaFeesPaymentService gaFeesPaymentService;
    @Mock
    private GaCoreCaseDataService gaCoreCaseDataService;
    @Mock
    private uk.gov.hmcts.reform.civil.ga.service.events.GaCaseEventService gaCaseEventService;
    @Mock
    private FeesService feesService;
    @Mock
    private GeneralAppFeesService generalAppFeesService;
    @Mock
    private CoreCaseDataService coreCaseDataService;
    @Mock
    private CaseEventService caseEventService;
    @Mock
    private DocumentManagementService documentManagementService;
    @Mock
    private ClaimFormService claimFormService;
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
    @Mock
    private TaskListService taskListService;
    @Mock
    private DashboardNotificationService dashboardNotificationService;
    @Mock
    private DashboardScenariosService dashboardScenariosService;
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
            gaCoreCaseDataService,
            dashboardClaimInfoService, caseEventService,
            gaCaseEventService,
            mock(uk.gov.hmcts.reform.civil.service.search.CaseSdtRequestSearchService.class),
            mock(uk.gov.hmcts.reform.civil.service.bulkclaims.CaseworkerCaseEventService.class),
            deadlineCalculator,
            mock(uk.gov.hmcts.reform.civil.validation.PostcodeValidator.class), userInformationService,
            repaymentDecisionService);
        CaseAssignmentController assignmentController = new CaseAssignmentController(
            referenceSearchService, pinService, assignCaseService, lipAssignmentService, coreCaseDataService);
        DashboardController dashboardController = new DashboardController(
            taskListService, dashboardNotificationService, dashboardScenariosService);
        DocumentController documentController = new DocumentController(claimFormService, documentManagementService);
        stateVerification = () -> { };
        rawOcmcResponse = false;
        ObjectMapper mapper = buildObjectMapper();
        MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter(mapper);
        mockMvc = MockMvcBuilders.standaloneSetup(
                paymentController, feesController, casesController, assignmentController, dashboardController,
                documentController)
            .addFilters(new RequestFilter())
            .setMessageConverters(new StringHttpMessageConverter(), new ResourceHttpMessageConverter(), messageConverter)
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
                .setPaymentAmount(new BigDecimal("200"))
        );
    }

    @State("A hearing payment can be initiated")
    void hearingPaymentCreation() {
        when(feesPaymentService.createGovPaymentRequest(FeeType.HEARING, CASE_REFERENCE, AUTH_HEADER))
            .thenReturn(new CardPaymentStatusResponse().setExternalReference("2023-1701090705688")
                .setPaymentReference(PAYMENT_REFERENCE).setStatus("Initiated")
                .setNextUrl("https://card.payments.service.gov.uk/secure/hearing-payment")
                .setDateCreated(OffsetDateTime.parse("2023-11-27T13:15:06.313Z")));
    }

    private void paymentStatus(String kind, String status) {
        CardPaymentStatusResponse response = new CardPaymentStatusResponse()
            .setExternalReference("2023-1701090705688").setPaymentReference(PAYMENT_REFERENCE)
            .setStatus(status).setPaymentAmount(new BigDecimal("200"));
        if ("Failed".equals(status)) {
            response.setErrorCode("P010").setErrorDescription("Payment was cancelled by the user");
        }
        if ("GA".equals(kind)) {
            when(gaFeesPaymentService.getGovPaymentRequestStatus(CASE_REFERENCE, PAYMENT_REFERENCE, AUTH_HEADER))
                .thenReturn(response);
        } else {
            response.setPaymentFor(kind.toLowerCase(java.util.Locale.ROOT));
            when(feesPaymentService.getGovPaymentRequestStatus(
                FeeType.valueOf(kind), CASE_REFERENCE, PAYMENT_REFERENCE, AUTH_HEADER)).thenReturn(response);
        }
    }

    private void missingPayment(String kind) {
        // PaymentStatusService wraps downstream not-found failures in PaymentsApiException;
        // the production uncaught-exception advice returns 500, not 404.
        uk.gov.hmcts.reform.civil.exceptions.PaymentsApiException error =
            new uk.gov.hmcts.reform.civil.exceptions.PaymentsApiException("Payment not found");
        if ("GA".equals(kind)) {
            when(gaFeesPaymentService.getGovPaymentRequestStatus(CASE_REFERENCE, PAYMENT_REFERENCE, AUTH_HEADER))
                .thenThrow(error);
        } else {
            when(feesPaymentService.getGovPaymentRequestStatus(
                FeeType.valueOf(kind), CASE_REFERENCE, PAYMENT_REFERENCE, AUTH_HEADER)).thenThrow(error);
        }
    }

    @State("Hearing Help with Fees can be submitted")
    void hearingHelpWithFees() {
        Map<String, Object> updates = Map.of("hwfFeeType", "HEARING", "hearingHelpFeesReferenceNumber", "HWF-123-456");
        when(caseEventService.submitEvent(eventParams(CASE_REFERENCE, updates)
            .setEvent(CaseEvent.APPLY_HELP_WITH_HEARING_FEE)))
            .thenReturn(CaseDetails.builder().id(Long.valueOf(CASE_REFERENCE)).state("HEARING_READINESS")
                .lastModified(LocalDateTime.parse("2025-02-03T10:00:00")).data(updates).build());
    }

    @State("GA Help with Fees submission is Yes")
    void gaHelpWithFeesYes() throws IOException {
        gaHelpWithFees("Yes", false);
    }

    @State("GA Help with Fees submission is No")
    void gaHelpWithFeesNo() throws IOException {
        gaHelpWithFees("No", false);
    }

    @State("GA Help with Fees submission is rejected")
    void gaHelpWithFeesRejected() throws IOException {
        gaHelpWithFees("Yes", true);
    }

    private void gaHelpWithFees(String option, boolean rejected) throws IOException {
        Map<String, Object> help = new HashMap<>();
        help.put("helpWithFee", option);
        if ("Yes".equals(option)) {
            help.put("helpWithFeesReferenceNumber", "HWF-123-456");
        }
        EventSubmissionParams params = eventParams(CASE_REFERENCE, Map.of("generalAppHelpWithFees", help))
            .setEvent(CaseEvent.NOTIFY_HELP_WITH_FEE);
        if (rejected) {
            when(gaCaseEventService.submitEvent(params)).thenThrow(new FeignException.UnprocessableEntity(
                "Help with Fees rejected", mock(feign.Request.class),
                buildObjectMapper().writeValueAsBytes(Map.of("callbackErrors", List.of("Reference is invalid"))), Map.of()));
        } else {
            when(gaCaseEventService.submitEvent(params)).thenReturn(CaseDetails.builder()
                .id(Long.valueOf(CASE_REFERENCE)).state("AWAITING_APPLICATION_PAYMENT")
                .data(Map.of("generalAppHelpWithFees", help)).build());
        }
        stateVerification = () -> verify(gaCaseEventService).submitEvent(params);
    }

    @State("The CLAIMISSUED payment status is Failed")
    void paymentClaimIssuedFailed() {
        paymentStatus("CLAIMISSUED", "Failed");
    }

    @State("The CLAIMISSUED payment status is Initiated")
    void paymentClaimIssuedInitiated() {
        paymentStatus("CLAIMISSUED", "Initiated");
    }

    @State("The CLAIMISSUED payment status is Pending")
    void paymentClaimIssuedPending() {
        paymentStatus("CLAIMISSUED", "Pending");
    }

    @State("The CLAIMISSUED payment cannot be found upstream")
    void missingClaimIssuedPayment() {
        missingPayment("CLAIMISSUED");
    }

    @State("The HEARING payment status is Success")
    void paymentHearingSuccess() {
        paymentStatus("HEARING", "Success");
    }

    @State("The HEARING payment status is Failed")
    void paymentHearingFailed() {
        paymentStatus("HEARING", "Failed");
    }

    @State("The HEARING payment status is Initiated")
    void paymentHearingInitiated() {
        paymentStatus("HEARING", "Initiated");
    }

    @State("The HEARING payment status is Pending")
    void paymentHearingPending() {
        paymentStatus("HEARING", "Pending");
    }

    @State("The HEARING payment cannot be found upstream")
    void missingHearingPayment() {
        missingPayment("HEARING");
    }

    @State("The GA payment status is Failed")
    void paymentGAFailed() {
        paymentStatus("GA", "Failed");
    }

    @State("The GA payment status is Initiated")
    void paymentGAInitiated() {
        paymentStatus("GA", "Initiated");
    }

    @State("The GA payment status is Pending")
    void paymentGAPending() {
        paymentStatus("GA", "Pending");
    }

    @State("The GA payment cannot be found upstream")
    void missingGAPayment() {
        missingPayment("GA");
    }

    private Map<String, Object> responseExample(String resource, String variant) throws IOException {
        ObjectMapper mapper = buildObjectMapper();
        try (var stream = getClass().getResourceAsStream(resource)) {
            return mapper.convertValue(mapper.readTree(java.util.Objects.requireNonNull(stream)).get(variant),
                                       new TypeReference<>() { });
        }
    }

    private void citizenResponse(String variant, String responseType) throws IOException {
        Map<String, Object> updates = responseExample("/civil-cui-responses.json", variant);
        // CCD returns case data, not an echo of every UI update field. Select the
        // persisted fields consumed by this journey and use their real model types.
        Map<String, Object> data = new HashMap<>();
        for (String field : List.of("respondent1", "respondent1LiPResponse", "respondent1LiPResponseCarm",
            "respondent1DQLanguage", "respondent1RepaymentPlan", "respondToClaimAdmitPartLRspec",
            "defenceRouteRequired", "detailsOfWhyDoesYouDisputeTheClaim", "defenceAdmitPartPaymentTimeRouteRequired",
            "respondToAdmittedClaimOwingAmountPounds", "applicant1AcceptAdmitAmountPaidSpec",
            "applicant1AcceptFullAdmitPaymentPlanSpec", "applicant1RepaymentOptionForDefendantSpec",
            "applicant1SuggestInstalmentsPaymentAmountForDefendantSpec",
            "applicant1SuggestInstalmentsRepaymentFrequencyForDefendantSpec",
            "applicant1SuggestInstalmentsFirstRepaymentDateForDefendantSpec",
            "applicant1RequestedPaymentDateForDefendantSpec", "applicant1LiPResponseCarm")) {
            if (updates.containsKey(field)) {
                data.put(field, updates.get(field));
            }
        }
        data.put("respondent1ClaimResponseTypeForSpec", responseType);
        // Serialize real CaseData models, including money strings and LocalDate fields.
        CaseData returned = buildObjectMapper().convertValue(data, CaseData.class);
        data = buildObjectMapper().convertValue(returned, new TypeReference<>() { });
        verifiedCitizenEvent(variant.startsWith("claimant-") ? CaseEvent.CLAIMANT_RESPONSE_CUI
            : CaseEvent.DEFENDANT_RESPONSE_CUI, updates, data, "AWAITING_APPLICANT_INTENTION");
    }

    private void verifiedCitizenEvent(CaseEvent event, Map<String, Object> updates,
                                      Map<String, Object> data, String state) {
        EventSubmissionParams params = eventParams(CUI_CASE_REFERENCE, updates).setEvent(event);
        when(caseEventService.submitEvent(params)).thenReturn(CaseDetails.builder()
            .id(Long.valueOf(CUI_CASE_REFERENCE)).state(state)
            .lastModified(LocalDateTime.of(2025, 5, 1, 10, 0)).data(data).build());
        stateVerification = () -> verify(caseEventService).submitEvent(params);
    }

    @State("A synthetic citizen document can be uploaded")
    void syntheticDocumentUpload() {
        Document link = new Document("https://documents.example.test/documents/evidence-001",
            "https://documents.example.test/documents/evidence-001/binary", "evidence.bin", null, null, null);
        CaseDocument uploaded = new CaseDocument().setDocumentLink(link).setDocumentName("evidence.bin")
            .setDocumentSize(5).setCreatedDatetime(LocalDateTime.of(2025, 4, 1, 9, 30)).setCreatedBy("citizen");
        when(documentManagementService.uploadDocument(eq(AUTH_HEADER), any(UploadedDocument.class))).thenReturn(uploaded);
        stateVerification = () -> verify(documentManagementService).uploadDocument(eq(AUTH_HEADER),
            org.mockito.ArgumentMatchers.<UploadedDocument>argThat(value -> value != null && "evidence.bin".equals(value.getFileBaseName())
                && value.getFile() != null && "evidence.bin".equals(value.getFile().getOriginalFilename())
                && value.getFile().getSize() == 5));
    }

    @State("A synthetic document exists for download")
    void syntheticDocumentDownload() {
        byte[] bytes = "Synthetic CUI document contract bytes; no personal or case data.\n"
            .getBytes(java.nio.charset.StandardCharsets.UTF_8);
        when(claimFormService.downloadDocumentById(AUTH_HEADER, "document-001", null))
            .thenReturn(new DownloadedDocumentResponse(new ByteArrayResource(bytes), "evidence.txt", "text/plain"));
        stateVerification = () -> verify(claimFormService)
            .downloadDocumentById(AUTH_HEADER, "document-001", null);
    }

    @State("The requested document does not exist")
    void missingSyntheticDocument() {
        when(claimFormService.downloadDocumentById(AUTH_HEADER, "missing-document", null))
            .thenThrow(new DocumentNotFoundException("missing-document", null));
        stateVerification = () -> verify(claimFormService)
            .downloadDocumentById(AUTH_HEADER, "missing-document", null);
    }

    @State("A translated EVIDENCE_UPLOAD_APPLICANT event can be submitted")
    void applicantEvidenceUpload() {
        Map<String, Object> value = Map.of("createdDatetime", "2025-04-01T09:30:00.000Z",
            "witnessOptionDocument", Map.of("document_url", "https://documents.example.test/documents/evidence-001",
                "document_binary_url", "https://documents.example.test/documents/evidence-001/binary",
                "document_filename", "evidence.txt"), "witnessOptionName", "Witness statement",
            "witnessOptionUploadDate", "2025-03-30T00:00:00.000Z");
        Map<String, Object> updates = Map.of("caseDocumentUploadDate", "2025-04-01T09:30:00.000Z",
            "documentWitnessStatement", List.of(Map.of("id", "evidence-entry-001", "value", value)));
        verifiedCitizenEvent(CaseEvent.EVIDENCE_UPLOAD_APPLICANT, updates, updates, "CASE_PROGRESSION");
    }

    @State("A translated EVIDENCE_UPLOAD_RESPONDENT event can be submitted")
    void respondentEvidenceUpload() {
        Map<String, Object> value = Map.of("createdDatetime", "2025-04-01T09:30:00.000Z",
            "witnessOptionDocument", Map.of("document_url", "https://documents.example.test/documents/evidence-001",
                "document_binary_url", "https://documents.example.test/documents/evidence-001/binary",
                "document_filename", "evidence.txt"), "witnessOptionName", "Witness statement",
            "witnessOptionUploadDate", "2025-03-30T00:00:00.000Z");
        Map<String, Object> updates = Map.of("caseDocumentUploadDateRes", "2025-04-01T09:30:00.000Z",
            "documentWitnessStatementRes", List.of(Map.of("id", "evidence-entry-001", "value", value)));
        verifiedCitizenEvent(CaseEvent.EVIDENCE_UPLOAD_RESPONDENT, updates, updates, "CASE_PROGRESSION");
    }

    @State("A CUI mediation document submission can be made")
    void mediationDocumentSubmission() {
        Map<String, Object> updates = Map.of("app1MediationDocumentsReferred", List.of(Map.of("id", "mediation-entry-001",
            "value", Map.of("document", Map.of("document_url", "https://documents.example.test/documents/evidence-001",
                "document_binary_url", "https://documents.example.test/documents/evidence-001/binary",
                "document_filename", "evidence.txt", "category_id", "ClaimantOneMediationDocs"),
                "documentDate", "2025-03-30T00:00:00.000Z",
                "documentType", "Invoice", "documentUploadedDatetime", "2025-04-01T09:30:00.000Z"))));
        verifiedCitizenEvent(CaseEvent.CUI_UPLOAD_MEDIATION_DOCUMENTS, updates, updates, "CASE_PROGRESSION");
    }

    @State("A translated trial readiness response can be submitted")
    void trialReadinessSubmission() {
        Map<String, Object> updates = Map.of("trialReadyApplicant", "Yes",
            "applicantRevisedHearingRequirements", Map.of("revisedHearingRequirements", "Yes",
                "revisedHearingComments", "A step-free hearing room is required."),
            "applicantHearingOtherComments", Map.of("hearingOtherComments", "Please list the witness first."));
        verifiedCitizenEvent(CaseEvent.TRIAL_READINESS, updates, updates, "CASE_PROGRESSION");
    }

    @State("A translated reconsideration initial request can be submitted")
    void initialReconsiderationSubmission() {
        Map<String, Object> updates = Map.of("requestForReviewCommentsDefendant",
            "The decision should be reconsidered because the payment was recorded.");
        verifiedCitizenEvent(CaseEvent.REQUEST_FOR_RECONSIDERATION, updates, updates, "CASE_PROGRESSION");
    }

    @State("A translated reconsideration comments response can be submitted")
    void reconsiderationCommentsSubmission() {
        Map<String, Object> updates = Map.of("requestForReviewCommentsClaimant",
            "Please review the evidence filed after judgment.");
        verifiedCitizenEvent(CaseEvent.REQUEST_FOR_RECONSIDERATION, updates, updates, "CASE_PROGRESSION");
    }

    @State("An agreed response extension can be submitted")
    void agreedResponseExtension() {
        Map<String, Object> updates = Map.of("respondentSolicitor1AgreedDeadlineExtension", "2025-07-01",
                                             "respondent1LiPResponse", Map.of("respondent1ResponseLanguage", "BOTH"));
        Map<String, Object> data = new HashMap<>(updates);
        data.put("respondentSolicitor1AgreedDeadlineExtension", LocalDate.of(2025, 7, 1));
        verifiedCitizenEvent(CaseEvent.INFORM_AGREED_EXTENSION_DATE_SPEC, updates, data, "AWAITING_RESPONDENT_ACKNOWLEDGEMENT");
    }

    @State("The defence citizen response can be submitted")
    void defenceResponse() throws IOException {
        citizenResponse("defence", "FULL_DEFENCE");
    }

    @State("The full-admission citizen response can be submitted")
    void fullAdmissionResponse() throws IOException {
        citizenResponse("full-admission", "FULL_ADMISSION");
    }

    @State("The part-admission citizen response can be submitted")
    void partAdmissionResponse() throws IOException {
        citizenResponse("part-admission", "PART_ADMISSION");
    }

    @State("The claimant-acceptance citizen response can be submitted")
    void claimantAcceptanceResponse() throws IOException {
        citizenResponse("claimant-acceptance", "PART_ADMISSION");
    }

    @State("The claimant-rejection citizen response can be submitted")
    void claimantRejectionResponse() throws IOException {
        citizenResponse("claimant-rejection", "PART_ADMISSION");
    }

    @State("The claimant-instalments citizen response can be submitted")
    void claimantInstalmentsResponse() throws IOException {
        citizenResponse("claimant-instalments", "FULL_ADMISSION");
    }

    @State("The claimant-set-date citizen response can be submitted")
    void claimantSetDateResponse() throws IOException {
        citizenResponse("claimant-set-date", "FULL_ADMISSION");
    }

    @State("A new citizen query can be submitted")
    void newQuery() throws IOException {
        Map<String, Object> updates = responseExample("/civil-cui-queries.json", "new");
        Map<String, Object> data = Map.of("queries", buildObjectMapper().convertValue(updates.get("queries"),
            uk.gov.hmcts.reform.civil.model.querymanagement.CaseQueriesCollection.class));
        verifiedCitizenEvent(CaseEvent.queryManagementRaiseQuery, updates, data, "CASE_PROGRESSION");
    }

    @State("A follow-up citizen query can be submitted")
    void followUpQuery() throws IOException {
        Map<String, Object> updates = responseExample("/civil-cui-queries.json", "follow-up");
        Map<String, Object> data = Map.of("queries", buildObjectMapper().convertValue(updates.get("queries"),
            uk.gov.hmcts.reform.civil.model.querymanagement.CaseQueriesCollection.class));
        verifiedCitizenEvent(CaseEvent.queryManagementRaiseQuery, updates, data, "CASE_PROGRESSION");
    }

    private void judgmentEvent(CaseEvent event, Map<String, Object> updates, String state) {
        EventSubmissionParams params = eventParams(CUI_CASE_REFERENCE, updates).setEvent(event);
        when(caseEventService.submitEvent(params)).thenReturn(CaseDetails.builder()
            .id(Long.valueOf(CUI_CASE_REFERENCE)).state("CASE_PROGRESSION")
            .lastModified(LocalDateTime.of(2025, 6, 4, 10, 0)).data(updates).build());
        stateVerification = () -> verify(caseEventService).submitEvent(params);
    }

    @State("The default judgment or settlement event can be submitted")
    void defaultJudgmentEvent() {
        Map<String, Object> updates = new HashMap<>();
        updates.put("ccjPaymentPaidSomeOption", "No");
        updates.put("ccjPaymentPaidSomeAmount", null);
        updates.put("ccjJudgmentAmountClaimFee", "11500");
        updates.put("ccjJudgmentLipInterest", "0");
        updates.put("applicant1RepaymentOptionForDefendantSpec", "SET_DATE");
        updates.put("applicant1RequestedPaymentDateForDefendantSpec", Map.of("paymentSetDate", "2025-06-01"));
        updates.put("totalClaimAmount", 1000);
        judgmentEvent(CaseEvent.DEFAULT_JUDGEMENT_SPEC, updates, "CASE_PROGRESSION");
    }

    @State("The admission judgment or settlement event can be submitted")
    void admissionJudgmentEvent() {
        Map<String, Object> updates = Map.ofEntries(
            Map.entry("ccjPaymentPaidSomeOption", "Yes"),
            Map.entry("ccjPaymentPaidSomeAmount", "20000"),
            Map.entry("ccjJudgmentAmountClaimFee", "11500"),
            Map.entry("ccjJudgmentLipInterest", "0"),
            Map.entry("applicant1RepaymentOptionForDefendantSpec", "REPAYMENT_PLAN"),
            Map.entry("applicant1SuggestInstalmentsRepaymentFrequencyForDefendantSpec", "ONCE_ONE_MONTH"),
            Map.entry("applicant1SuggestInstalmentsPaymentAmountForDefendantSpec", 10000),
            Map.entry("applicant1SuggestInstalmentsFirstRepaymentDateForDefendantSpec", "2025-06-01"),
            Map.entry("totalClaimAmount", 1000));
        judgmentEvent(CaseEvent.REQUEST_JUDGEMENT_ADMISSION_SPEC, updates, "CASE_PROGRESSION");
    }

    @State("The settled judgment or settlement event can be submitted")
    void settledJudgmentEvent() {
        Map<String, Object> updates = Map.ofEntries(
            Map.entry("applicant1ClaimSettledDate", "2025-06-02"));
        judgmentEvent(CaseEvent.LIP_CLAIM_SETTLED, updates, "CASE_PROGRESSION");
    }

    @State("The signed judgment or settlement event can be submitted")
    void signedJudgmentEvent() {
        Map<String, Object> updates = Map.ofEntries(
            Map.entry("respondentSignSettlementAgreement", "Yes"));
        judgmentEvent(CaseEvent.DEFENDANT_SIGN_SETTLEMENT_AGREEMENT, updates, "CASE_PROGRESSION");
    }

    @State("The paid judgment or settlement event can be submitted")
    void paidJudgmentEvent() {
        Map<String, Object> updates = Map.ofEntries(
            Map.entry("joJudgmentPaidInFull", Map.of("dateOfFullPaymentMade", "2025-06-03", "confirmFullPaymentMade", List.of("CONFIRMED"))));
        judgmentEvent(CaseEvent.JUDGMENT_PAID_IN_FULL, updates, "CASE_PROGRESSION");
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

    @State("A General Application case exists")
    void generalApplicationCaseExists() {
        Map<String, Object> caseData = Map.of(
            "applicationTypes", "ADJOURN_HEARING",
            "generalAppType", Map.of("types", List.of("ADJOURN_HEARING")),
            "applicationFeeAmountInPence", "27500",
            "generalAppRespondentDebtorOffer", Map.of("respondentDebtorOffer", "DECLINE", "monthlyInstalment", "12500"),
            "caseLink", Map.of("CaseReference", CUI_CASE_REFERENCE),
            "gaAddlDoc", List.of(Map.of("id", "document-1", "value", Map.of(
                "document_url", "http://dm-store/documents/ga-doc-1",
                "document_binary_url", "http://dm-store/documents/ga-doc-1/binary",
                "document_filename", "hearing-evidence.pdf")))
        );
        when(coreCaseDataService.getCase(Long.valueOf(CUI_GA_CASE_REFERENCE), AUTH_HEADER))
            .thenReturn(gaCase(CUI_GA_CASE_REFERENCE, "AWAITING_RESPONDENT_RESPONSE", "2025-04-28T09:00:00",
                "2025-05-01T10:00:00", caseData));
    }

    @State("General Applications for parent case are populated")
    void generalApplicationsForParentArePopulated() {
        SearchResult result = SearchResult.builder().total(2).cases(List.of(
            gaCase("3333444455556666", "APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION", "2025-04-30T09:00:00",
                "2025-05-02T10:00:00", Map.of("generalAppType", Map.of("types", List.of("SETTLE_BY_CONSENT")),
                    "applicationFeeAmountInPence", "10000", "caseLink", Map.of("CaseReference", CUI_CASE_REFERENCE))),
            gaCase(CUI_GA_CASE_REFERENCE, "AWAITING_RESPONDENT_RESPONSE", "2025-04-28T09:00:00",
                "2025-05-01T10:00:00", Map.of("generalAppType", Map.of("types", List.of("ADJOURN_HEARING")),
                    "applicationFeeAmountInPence", "27500", "caseLink", Map.of("CaseReference", CUI_CASE_REFERENCE)))
        )).build();
        when(gaCoreCaseDataService.searchGeneralApplicationWithCaseId(CUI_CASE_REFERENCE, AUTH_HEADER)).thenReturn(result);
    }

    @State("General Applications for parent case are empty")
    void generalApplicationsForParentAreEmpty() {
        when(gaCoreCaseDataService.searchGeneralApplicationWithCaseId(CUI_CASE_REFERENCE, AUTH_HEADER))
            .thenReturn(SearchResult.builder().total(0).cases(List.of()).build());
    }

    private CaseDetails gaCase(String id, String state, String createdDate, String lastModified,
                               Map<String, Object> caseData) {
        return CaseDetails.builder().id(Long.valueOf(id)).state(state)
            .createdDate(LocalDateTime.parse(createdDate)).lastModified(LocalDateTime.parse(lastModified))
            .data(caseData).build();
    }

    @State("An ordinary GA can be initiated")
    void ordinaryGaCanBeInitiated() {
        Map<String, Object> updates = ordinaryGaUpdates();
        verifiedCitizenGaEvent(CaseEvent.INITIATE_GENERAL_APPLICATION, updates, Map.of(
            "generalApplications", List.of(Map.of("id", CUI_GA_CASE_REFERENCE, "value", Map.of(
                "caseLink", Map.of("CaseReference", CUI_CASE_REFERENCE),
                "generalAppSubmittedDateGAspec", "2025-05-01", "parentClaimantIsApplicant", "Yes")))));
    }

    @State("A COSC GA can be initiated")
    void coscGaCanBeInitiated() {
        Map<String, Object> updates = Map.of(
            "generalAppType", Map.of("types", List.of("CONFIRM_CCJ_DEBT_PAID")),
            "generalAppRespondentAgreement", Map.of("hasAgreed", "No"),
            "certOfSC", Map.of("defendantFinalPaymentDate", "2025-04-15",
                "debtPaymentEvidence", Map.of("debtPaymentOption", "MADE_FULL_PAYMENT_TO_COURT")),
            "generalAppStatementOfTruth", Map.of("name", "Alex Applicant", "role", "Claimant"),
            "generalAppInformOtherParty", Map.of("isWithNotice", "No", "reasonsForWithoutNotice", "DummyVal"),
            "generalAppEvidenceDocument", List.of(Map.of("value", Map.of(
                "document_url", "http://dm-store/documents/ga-doc-1",
                "document_binary_url", "http://dm-store/documents/ga-doc-1/binary",
                "document_filename", "hearing-evidence.pdf", "category_id", "evidence")))
        );
        verifiedCitizenGaEvent(CaseEvent.INITIATE_GENERAL_APPLICATION_COSC, updates, Map.of(
            "generalApplications", List.of(Map.of("id", CUI_GA_CASE_REFERENCE, "value", Map.of(
                "caseLink", Map.of("CaseReference", CUI_CASE_REFERENCE),
                "generalAppSubmittedDateGAspec", "2025-05-01", "parentClaimantIsApplicant", "Yes")))));
    }

    private Map<String, Object> ordinaryGaUpdates() {
        return Map.ofEntries(
            Map.entry("generalAppType", Map.of("types", List.of("ADJOURN_HEARING"))),
            Map.entry("generalAppRespondentAgreement", Map.of("hasAgreed", "Yes")),
            Map.entry("generalAppInformOtherParty", Map.of("isWithNotice", "No", "reasonsForWithoutNotice", "Urgent hearing date")),
            Map.entry("generalAppAskForCosts", "Yes"),
            Map.entry("generalAppDetailsOfOrder", "Move the hearing to a later date"),
            Map.entry("generalAppDetailsOfOrderColl", List.of(Map.of("value", "Move the hearing to a later date"))),
            Map.entry("generalAppReasonsOfOrder", "The parties need additional preparation time"),
            Map.entry("generalAppReasonsOfOrderColl", List.of(Map.of("value", "The parties need additional preparation time"))),
            Map.entry("generalAppEvidenceDocument", List.of(Map.of("value", Map.of(
                "document_url", "http://dm-store/documents/ga-doc-1",
                "document_binary_url", "http://dm-store/documents/ga-doc-1/binary",
                "document_filename", "hearing-evidence.pdf", "category_id", "evidence")))),
            Map.entry("generalAppHearingDetails", Map.of(
                "HearingPreferencesPreferredType", "TELEPHONE", "ReasonForPreferredHearingType", "The witness cannot travel",
                "HearingPreferredLocation", Map.of("value", Map.of("label", "Leeds")),
                "HearingDetailsTelephoneNumber", "07123456789", "HearingDetailsEmailID", "applicant@example.com",
                "unavailableTrialRequiredYesOrNo", "No", "SupportRequirement", List.of())),
            Map.entry("generalAppStatementOfTruth", Map.of("name", "Alex Applicant", "role", "Claimant"))
        );
    }

    private void verifiedCitizenGaEvent(CaseEvent event, Map<String, Object> updates, Map<String, Object> responseData) {
        EventSubmissionParams params = eventParams(CUI_CASE_REFERENCE, updates).setEvent(event);
        when(caseEventService.submitEvent(params)).thenReturn(CaseDetails.builder()
            .id(Long.valueOf(CUI_CASE_REFERENCE)).state("CASE_PROGRESSION")
            .lastModified(LocalDateTime.parse("2025-05-01T10:00:00")).data(responseData).build());
        stateVerification = () -> verify(caseEventService).submitEvent(params);
    }

    @State("An ordinary GA response can be submitted")
    void ordinaryGaResponse() {
        gaApplicationEvent(CaseEvent.RESPOND_TO_APPLICATION, gaResponseUpdates());
    }

    @State("An urgent GA response can be submitted")
    void urgentGaResponse() {
        gaApplicationEvent(CaseEvent.RESPOND_TO_APPLICATION_URGENT_LIP, gaResponseUpdates());
    }

    private Map<String, Object> gaResponseUpdates() {
        return Map.of(
            "hearingDetailsResp", Map.of(
                "HearingPreferencesPreferredType", "TELEPHONE", "ReasonForPreferredHearingType", "A remote hearing is needed",
                "HearingPreferredLocation", Map.of("value", Map.of("label", "Leeds")),
                "HearingDetailsTelephoneNumber", "07123456789", "HearingDetailsEmailID", "respondent@example.com",
                "unavailableTrialRequiredYesOrNo", "No", "SupportRequirement", List.of()),
            "gaRespondentDebtorOffer", Map.of("respondentDebtorOffer", "DECLINE", "debtorObjections", "The offer should be paid monthly",
                "paymentPlan", "INSTALMENT", "monthlyInstalment", "12500"),
            "gaRespondentConsent", "No", "generalAppRespondent1Representative", Map.of("hasAgreed", "No"),
            "generalAppRespondReason", "The proposed order is not agreed",
            "generalAppRespondDocument", List.of(Map.of("value", Map.of(
                "document_url", "http://dm-store/documents/ga-doc-1", "document_binary_url", "http://dm-store/documents/ga-doc-1/binary",
                "document_filename", "hearing-evidence.pdf", "category_id", "evidence"))),
            "generalAppResponseStatementOfTruth", Map.of("name", "Riley Respondent", "role", "Defendant")
        );
    }

    @State("additional documents can be submitted to a General Application")
    void gaAdditionalDocuments() {
        gaApplicationEvent(CaseEvent.UPLOAD_ADDL_DOCUMENTS, Map.of("uploadDocument", List.of(Map.of(
            "id", "10000000-0000-4000-8000-000000000001", "value", Map.of("typeOfDocument", "Hearing evidence",
                "documentUpload", Map.of("document_url", "http://dm-store/documents/ga-doc-1",
                    "document_binary_url", "http://dm-store/documents/ga-doc-1/binary", "document_filename", "hearing-evidence.pdf"))))));
    }

    @State("judge directions documents can be submitted to a General Application")
    void gaJudgeDirections() {
        gaApplicationEvent(CaseEvent.RESPOND_TO_JUDGE_DIRECTIONS, Map.of("generalAppDirOrderUpload", gaDocumentCollection()));
    }

    @State("judge additional information documents can be submitted to a General Application")
    void gaJudgeAdditionalInformation() {
        gaApplicationEvent(CaseEvent.RESPOND_TO_JUDGE_ADDITIONAL_INFO, Map.of(
            "generalAppAddlnInfoUpload", gaDocumentCollection(), "generalAppAddlnInfoText", "Please review this document"));
    }

    @State("judge written representation documents can be submitted to a General Application")
    void gaJudgeWrittenRepresentation() {
        gaApplicationEvent(CaseEvent.RESPOND_TO_JUDGE_WRITTEN_REPRESENTATION, Map.of("generalAppWrittenRepUpload", gaDocumentCollection()));
    }

    private List<Map<String, Object>> gaDocumentCollection() {
        return List.of(Map.of("value", Map.of("document_url", "http://dm-store/documents/ga-doc-1",
            "document_binary_url", "http://dm-store/documents/ga-doc-1/binary",
            "document_filename", "hearing-evidence.pdf", "category_id", "evidence")));
    }

    private void gaApplicationEvent(CaseEvent event, Map<String, Object> updates) {
        EventSubmissionParams params = new EventSubmissionParams().setAuthorisation(AUTH_HEADER)
            .setCaseId(CUI_GA_CASE_REFERENCE).setUserId("cui-user-id").setEvent(event).setUpdates(updates);
        when(gaCaseEventService.submitEvent(params)).thenReturn(CaseDetails.builder()
            .id(Long.valueOf(CUI_GA_CASE_REFERENCE)).state("AWAITING_RESPONDENT_RESPONSE")
            .lastModified(LocalDateTime.parse("2025-05-01T10:00:00")).data(updates).build());
        stateVerification = () -> verify(gaCaseEventService).submitEvent(params);
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

    private JsonNode dashboardExamples() throws IOException {
        try (var input = getClass().getResourceAsStream("/civil-cui-dashboard.json")) {
            return buildObjectMapper().readTree(java.util.Objects.requireNonNull(input));
        }
    }

    private void dashboardTasks(String role, boolean populated) throws IOException {
        List<TaskList> tasks = populated ? buildObjectMapper().convertValue(dashboardExamples().get("tasks"),
                                                                          new TypeReference<List<TaskList>>() { })
            : List.of();
        // Check fixture statuses against the real provider enum and its Welsh names.
        for (TaskList task : tasks) {
            TaskStatus status = TaskStatus.getTaskStatusByName(task.getCurrentStatusEn());
            org.junit.jupiter.api.Assertions.assertEquals(status.getWelshName(), task.getCurrentStatusCy());
        }
        when(taskListService.getTaskList(CUI_CASE_REFERENCE, role)).thenReturn(tasks);
    }

    private List<Notification> dashboardNotifications(boolean populated) throws IOException {
        return populated ? buildObjectMapper().convertValue(dashboardExamples().get("notifications"),
                                                           new TypeReference<List<Notification>>() { }) : List.of();
    }

    @State("The CLAIMANT dashboard task list is populated")
    void claimantTasksPopulated() throws IOException {
        dashboardTasks("CLAIMANT", true);
    }

    @State("The CLAIMANT Civil notifications are populated")
    void claimantNotificationsPopulated() throws IOException {
        when(dashboardNotificationService.getNotifications(CUI_CASE_REFERENCE, "CLAIMANT"))
            .thenReturn(dashboardNotifications(true));
    }

    @State("The CLAIMANT dashboard task list is empty")
    void claimantTasksEmpty() throws IOException {
        dashboardTasks("CLAIMANT", false);
    }

    @State("The CLAIMANT Civil notifications are empty")
    void claimantNotificationsEmpty() throws IOException {
        when(dashboardNotificationService.getNotifications(CUI_CASE_REFERENCE, "CLAIMANT"))
            .thenReturn(dashboardNotifications(false));
    }

    @State("The DEFENDANT dashboard task list is populated")
    void defendantTasksPopulated() throws IOException {
        dashboardTasks("DEFENDANT", true);
    }

    @State("The DEFENDANT Civil notifications are populated")
    void defendantNotificationsPopulated() throws IOException {
        when(dashboardNotificationService.getNotifications(CUI_CASE_REFERENCE, "DEFENDANT"))
            .thenReturn(dashboardNotifications(true));
    }

    @State("The DEFENDANT dashboard task list is empty")
    void defendantTasksEmpty() throws IOException {
        dashboardTasks("DEFENDANT", false);
    }

    @State("The DEFENDANT Civil notifications are empty")
    void defendantNotificationsEmpty() throws IOException {
        when(dashboardNotificationService.getNotifications(CUI_CASE_REFERENCE, "DEFENDANT"))
            .thenReturn(dashboardNotifications(false));
    }

    @State("The APPLICANT GA notifications are populated")
    void applicantGaNotificationsPopulated() throws IOException {
        when(dashboardNotificationService.getAllCasesNotifications(
            List.of("2222333344445555", "3333444455556666"), "APPLICANT"))
            .thenReturn(Map.of("2222333344445555", dashboardNotifications(true),
                               "3333444455556666", List.of()));
    }

    @State("The APPLICANT GA notifications are empty")
    void applicantGaNotificationsEmpty() throws IOException {
        when(dashboardNotificationService.getAllCasesNotifications(
            List.of("2222333344445555", "3333444455556666"), "APPLICANT"))
            .thenReturn(Map.of("2222333344445555", dashboardNotifications(false),
                               "3333444455556666", List.of()));
    }

    @State("The RESPONDENT GA notifications are populated")
    void respondentGaNotificationsPopulated() throws IOException {
        when(dashboardNotificationService.getAllCasesNotifications(
            List.of("2222333344445555", "3333444455556666"), "RESPONDENT"))
            .thenReturn(Map.of("2222333344445555", dashboardNotifications(true),
                               "3333444455556666", List.of()));
    }

    @State("The RESPONDENT GA notifications are empty")
    void respondentGaNotificationsEmpty() throws IOException {
        when(dashboardNotificationService.getAllCasesNotifications(
            List.of("2222333344445555", "3333444455556666"), "RESPONDENT"))
            .thenReturn(Map.of("2222333344445555", dashboardNotifications(false),
                               "3333444455556666", List.of()));
    }

    @State("A draft dashboard scenario can be created")
    void draftDashboardScenario() {
        stateVerification = () -> verify(dashboardScenariosService).recordScenarios(
            AUTH_HEADER, "Scenario.AAA6.ClaimIssue.ClaimSubmit.Required", "cui-user-id",
            new ScenarioRequestParams(new HashMap<>()));
    }

    @State("A dashboard notification update has a valid identifier")
    void notificationClick() {
        stateVerification = () -> verify(dashboardNotificationService).recordClick(dashboardItemId(), AUTH_HEADER);
    }

    @State("A dashboard task update has a valid identifier")
    void dashboardTaskUpdate() {
        TaskListEntity updated = new TaskListEntity();
        updated.setId(dashboardItemId());
        updated.setReference(CUI_CASE_REFERENCE);
        updated.setCurrentStatus(TaskStatus.DONE.getPlaceValue());
        when(taskListService.updateTaskListItem(dashboardItemId())).thenReturn(updated);
        stateVerification = () -> verify(taskListService).updateTaskListItem(dashboardItemId());
    }

    @State({"A dashboard notification update has a malformed identifier",
        "A dashboard task update has a malformed identifier"})
    void malformedDashboardUpdate() {
        stateVerification = () -> verifyNoInteractions(taskListService, dashboardNotificationService);
    }

    @State("The dashboard task to update does not exist")
    void missingDashboardTask() {
        when(taskListService.updateTaskListItem(dashboardItemId()))
            .thenThrow(new IllegalArgumentException("Invalid task item identifier " + dashboardItemId()));
    }

    private UUID dashboardItemId() {
        return UUID.fromString("10000000-0000-4000-8000-000000000001");
    }

    private ObjectMapper buildObjectMapper() {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        new JacksonConfiguration().jsonDateTimeFormatCustomizer().customize(builder);
        ObjectMapper mapper = builder.build();
        JacksonConfiguration.objectMapperJavaTimePostProcessor().postProcessAfterInitialization(mapper, "objectMapper");
        return mapper;
    }
}
