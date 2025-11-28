package uk.gov.hmcts.reform.civil.ga.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.ga.enums.GADebtorPaymentPlanGAspec;
import uk.gov.hmcts.reform.civil.ga.enums.GAJudicialHearingType;
import uk.gov.hmcts.reform.civil.ga.enums.GARespondentDebtorOfferOptionsGAspec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.GAHearingType;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeDecisionOption;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.ga.handler.GeneralApplicationBaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.ga.model.GARespondentRepresentative;
import uk.gov.hmcts.reform.civil.model.GeneralAppParentCaseLink;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationType;
import uk.gov.hmcts.reform.civil.model.genapplication.GAHearingDetails;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudgesHearingListGAspec;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialDecision;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GARespondentDebtorOfferGAspec;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GARespondentResponse;
import uk.gov.hmcts.reform.civil.model.genapplication.GASolicitorDetailsGAspec;
import uk.gov.hmcts.reform.civil.model.genapplication.GAUnavailabilityDates;
import uk.gov.hmcts.reform.civil.model.genapplication.GAUrgencyRequirement;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.ga.service.DocUploadDashboardNotificationService;
import uk.gov.hmcts.reform.civil.ga.service.GaForLipService;
import uk.gov.hmcts.reform.civil.ga.service.GeneralAppLocationRefDataService;
import uk.gov.hmcts.reform.civil.ga.service.ParentCaseUpdateHelper;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.RESPOND_TO_APPLICATION;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.RESPOND_TO_APPLICATION_URGENT_LIP;
import static uk.gov.hmcts.reform.civil.enums.CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_RESPONDENT_RESPONSE;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.SUMMARY_JUDGEMENT;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.VARY_PAYMENT_TERMS_OF_JUDGMENT;
import static uk.gov.hmcts.reform.civil.model.common.DynamicList.fromList;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@SuppressWarnings({"checkstyle:EmptyLineSeparator", "checkstyle:Indentation"})
@SpringBootTest(classes = {
    CaseDetailsConverter.class,
    RespondToApplicationHandler.class,
    JacksonAutoConfiguration.class,
    AssignCategoryId.class
},
    properties = {"reference.database.enabled=false"})
public class RespondToApplicationHandlerTest extends GeneralApplicationBaseCallbackHandlerTest {

    @Autowired
    RespondToApplicationHandler handler;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    CaseDetailsConverter caseDetailsConverter;

    @MockBean
    private FeatureToggleService featureToggleService;
    @MockBean
    private DocUploadDashboardNotificationService dashboardNotificationService;
    @MockBean
    IdamClient idamClient;
    @MockBean
    GaForLipService gaForLipService;
    @MockBean
    ParentCaseUpdateHelper parentCaseUpdateHelper;

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @MockBean
    protected GeneralAppLocationRefDataService locationRefDataService;
    List<Element<Document>> documents = new ArrayList<>();
    @BeforeEach
        public void setUp() throws IOException {

        when(idamClient.getUserInfo(anyString())).thenReturn(UserInfo.builder()
                                                                 .sub(DUMMY_EMAIL)
                                                                 .uid(DEF_UID)
                                                                 .build());

        Document document = Document.builder().documentUrl("url").documentFileName("file").build();
        documents.add(ElementUtils.element(document));
    }

    List<Element<GARespondentResponse>> respondentsResponses = new ArrayList<>();

    private static final String CAMUNDA_EVENT = "INITIATE_GENERAL_APPLICATION";
    private static final String DUMMY_EMAIL = "test@gmail.com";
    private static final String BUSINESS_PROCESS_INSTANCE_ID = "11111";
    private static final String ACTIVITY_ID = "anyActivity";
    private static final String CONFIRMATION_MESSAGE = "<br/><p> In relation to the following application(s): </p>"
        + "<ul> <li>Summary judgment</li> </ul>"
        + " <p> The application and your response will be reviewed by a Judge. </p> ";
    private static final String ERROR = "The General Application has already received a response.";
    private static final String RESPONDENT_ERROR = "The application has already been responded to.";
    public static final String TRIAL_DATE_FROM_REQUIRED = "Please enter the Date from if the trial has been fixed";
    public static final String INVALID_TRIAL_DATE_RANGE = "Trial Date From cannot be after Trial Date to. "
        + "Please enter valid range.";
    public static final String UNAVAILABLE_DATE_RANGE_MISSING = "Please provide at least one valid Date from if you "
        + "cannot attend hearing within next 3 months.";
    public static final String UNAVAILABLE_FROM_MUST_BE_PROVIDED = "If you selected option to be unavailable then "
        + "you must provide at least one valid Date from";
    public static final String INVALID_UNAVAILABILITY_RANGE = "Unavailability Date From cannot be after "
        + "Unavailability Date to. Please enter valid range.";
    public static final String INVALID_TRAIL_DATE_FROM_BEFORE_TODAY = "Trail date from must not be before today.";
    public static final String INVALID_UNAVAILABLE_DATE_FROM_BEFORE_TODAY = "Unavailability date from must not"
        + " be before today.";
    public static final LocalDate TRIAL_DATE_FROM_INVALID = LocalDate.of(2022, 3, 1);
    public static final LocalDate TRIAL_DATE_FROM_AFTER_INVALID = TRIAL_DATE_FROM_INVALID.plusDays(10L);
    public static final LocalDate TRIAL_DATE_TO_BEFORE_INVALID = TRIAL_DATE_FROM_INVALID.minusDays(10L);

    public static final LocalDate UNAVAILABILITY_DATE_FROM_INVALID = LocalDate.of(2022, 3, 1);
    public static final LocalDate UNAVAILABILITY_DATE_TO_INVALID = TRIAL_DATE_FROM_INVALID.minusDays(10L);
    public static final String PAYMENT_DATE_CANNOT_BE_IN_PAST =
        "The date entered cannot be in the past.";
    private static final String APP_UID = "9";
    private static final String DEF_UID = "10";
    private static final String DEF2_UID = "11";

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(RESPOND_TO_APPLICATION, RESPOND_TO_APPLICATION_URGENT_LIP);
    }

    @Test
    void buildResponseConfirmationReturnsCorrectMessageWhenGaHasLip() {
        when(featureToggleService.isGaForWelshEnabled()).thenReturn(false);
        when(gaForLipService.isGaForLip(any())).thenReturn(true);
        CallbackParams params = callbackParamsOf(getCase(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION),
                                                 CallbackType.SUBMITTED);
        var response = (SubmittedCallbackResponse) handler.handle(params);
        verify(dashboardNotificationService).createResponseDashboardNotification(any(), eq("RESPONDENT"), anyString());
        verify(dashboardNotificationService).createResponseDashboardNotification(any(), eq("APPLICANT"), anyString());
        assertThat(response).isNotNull();
        assertThat(response.getConfirmationBody()).isEqualTo(CONFIRMATION_MESSAGE);
    }

    @Test
    void buildResponseConfirmationReturnsCorrectMessageWhenGaHasLipAndVaryJudgeApppLipVLip() {
        when(featureToggleService.isGaForWelshEnabled()).thenReturn(false);
        when(gaForLipService.isGaForLip(any())).thenReturn(true);
        when(gaForLipService.isLipApp(any())).thenReturn(true);
        when(gaForLipService.isLipResp(any())).thenReturn(true);
        CallbackParams params = callbackParamsOf(getVaryCase(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION),
                                                 CallbackType.SUBMITTED);
        var response = (SubmittedCallbackResponse) handler.handle(params);

        verify(dashboardNotificationService, times(2))
            .createOfflineResponseDashboardNotification(any(), any(), anyString());
        assertThat(response).isNotNull();
    }

    @Test
    void buildResponseConfirmationReturnsCorrectMessageWhenGaHasLipAndVaryJudgeApppLRvLR() {
        when(gaForLipService.isGaForLip(any())).thenReturn(false);
        when(gaForLipService.isLipApp(any())).thenReturn(false);
        when(gaForLipService.isLipResp(any())).thenReturn(false);
        CallbackParams params = callbackParamsOf(getVaryCase(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION),
                                                 CallbackType.SUBMITTED);
        var response = (SubmittedCallbackResponse) handler.handle(params);
        verifyNoInteractions(dashboardNotificationService);

        assertThat(response).isNotNull();
    }

    @Test
    void buildResponseConfirmationReturnsCorrectMessageWhenWelshFlagEnabledAndApplicantBilingual() {
        when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
        when(gaForLipService.isGaForLip(any())).thenReturn(true);
        when(gaForLipService.isLipApp(any())).thenReturn(true);
        when(gaForLipService.isLipResp(any())).thenReturn(false);
        GeneralApplicationCaseData casedata = getVaryCase(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION).toBuilder()
            .isGaApplicantLip(YES).applicantBilingualLanguagePreference(YES).build();

        CallbackParams params = callbackParamsOf(casedata, CallbackType.SUBMITTED);
        var response = (SubmittedCallbackResponse) handler.handle(params);
        verifyNoInteractions(dashboardNotificationService);

        assertThat(response).isNotNull();
    }

    @Test
    void buildResponseConfirmationReturnsCorrectMessageWhenWelshFlagEnabledAndApplicantNotBilingual() {
        when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
        GeneralApplicationCaseData casedata = getVaryCase(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION).toBuilder().build();

        CallbackParams params = callbackParamsOf(casedata, CallbackType.SUBMITTED);
        var response = (SubmittedCallbackResponse) handler.handle(params);
        verifyNoInteractions(dashboardNotificationService);

        assertThat(response).isNotNull();
    }

    @Test
    void buildResponseConfirmationReturnsCorrectMessageWhenWelshFlagDisabledAndApplicantNotBilingual() {
        when(featureToggleService.isGaForWelshEnabled()).thenReturn(false);
        when(gaForLipService.isGaForLip(any())).thenReturn(true);
        when(gaForLipService.isLipApp(any())).thenReturn(true);
        when(gaForLipService.isLipResp(any())).thenReturn(true);
        GeneralApplicationCaseData casedata = getVaryCase(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION).toBuilder()
            .isGaApplicantLip(YES).applicantBilingualLanguagePreference(YES).build();

        CallbackParams params = callbackParamsOf(casedata, CallbackType.SUBMITTED);

        var response = (SubmittedCallbackResponse) handler.handle(params);
        verify(dashboardNotificationService, times(2))
            .createOfflineResponseDashboardNotification(any(), any(), anyString());
        assertThat(response).isNotNull();
    }

    @Test
    void buildResponseConfirmationReturnsCorrectMessageWhenGaHasLipAndVaryJudgeApppLipVLR() {
        when(gaForLipService.isGaForLip(any())).thenReturn(true);
        when(gaForLipService.isLipApp(any())).thenReturn(true);
        when(gaForLipService.isLipResp(any())).thenReturn(false);
        CallbackParams params = callbackParamsOf(getVaryCase(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION),
                                                 CallbackType.SUBMITTED);
        var response = (SubmittedCallbackResponse) handler.handle(params);
        verify(dashboardNotificationService).createOfflineResponseDashboardNotification(any(), any(), anyString());
        assertThat(response).isNotNull();
    }

    @Test
    void buildResponseConfirmationReturnsCorrectMessageWhenGaHasLipAndVaryJudgeApppLRVLip() {
        when(gaForLipService.isGaForLip(any())).thenReturn(true);
        when(gaForLipService.isLipApp(any())).thenReturn(true);
        when(gaForLipService.isLipResp(any())).thenReturn(false);
        CallbackParams params = callbackParamsOf(getVaryCase(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION),
                                                 CallbackType.SUBMITTED);
        var response = (SubmittedCallbackResponse) handler.handle(params);
        verify(dashboardNotificationService).createOfflineResponseDashboardNotification(any(), any(), anyString());
        assertThat(response).isNotNull();
    }

    @Test
    void buildResponseConfirmationReturnsCorrectMessage() {
        when(gaForLipService.isGaForLip(any())).thenReturn(false);
        CallbackParams params = callbackParamsOf(getCase(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION),
                                                 CallbackType.SUBMITTED);
        var response = (SubmittedCallbackResponse) handler.handle(params);
        verifyNoInteractions(dashboardNotificationService);
        assertThat(response).isNotNull();
        assertThat(response.getConfirmationBody()).isEqualTo(CONFIRMATION_MESSAGE);
    }

    @Test
    void generalAppRespondent1RepGivesCorrectValueWhenInvoked() {
        YesOrNo repAgreed = getCase(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
            .getGeneralAppRespondent1Representative().getGeneralAppRespondent1Representative();
        assertThat(repAgreed).isEqualTo(YES);
    }

    @Test
    void aboutToStartCallbackChecksApplicationStateBeforeProceeding() {

        List<LocationRefData> locations = new ArrayList<>();
        locations.add(LocationRefData.builder().siteName("siteName").courtAddress("court Address").postcode("post code")
                          .courtName("Court Name").region("Region").build());
        when(locationRefDataService.getCourtLocations(any())).thenReturn(locations);

        CallbackParams params = callbackParamsOf(getCase(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION),
                                                 CallbackType.ABOUT_TO_START);
        List<String> errors = new ArrayList<>();
        errors.add(ERROR);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response).isNotNull();
        assertThat(response.getErrors()).isEqualTo(errors);
    }

    @Test
    void aboutToStartCallbackChecksApplicationStateBeforeProceedingForUrgentLip() {

        List<LocationRefData> locations = new ArrayList<>();
        locations.add(LocationRefData.builder().siteName("siteName").courtAddress("court Address").postcode("post code")
                          .courtName("Court Name").region("Region").build());
        when(locationRefDataService.getCourtLocations(any())).thenReturn(locations);
        when(gaForLipService.isLipResp(any())).thenReturn(true);
        GeneralApplicationCaseData.GeneralApplicationCaseDataBuilder<?, ?> caseData =
            GeneralApplicationCaseData.builder().ccdState(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION).generalAppUrgencyRequirement(
                    GAUrgencyRequirement.builder().generalAppUrgency(YES).build()).generalAppType(
                    GAApplicationType
                        .builder()
                        .types(List.of(
                            (GeneralApplicationTypes.SUMMARY_JUDGEMENT))).build())
                .parentClaimantIsApplicant(NO);
        CallbackParams params = callbackParamsOf(
            caseData.build(),
            CallbackType.ABOUT_TO_START
        );
        List<String> errors = new ArrayList<>();
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response).isNotNull();
        assertThat(response.getErrors()).isEqualTo(errors);
    }
    @Test
    void aboutToStartCallbackChecksRespondendResponseBeforeProceeding() {

        List<LocationRefData> locations = new ArrayList<>();
        locations.add(LocationRefData.builder().siteName("siteName").courtAddress("court Address").postcode("post code")
                          .courtName("Court Name").region("Region").build());
        when(locationRefDataService.getCourtLocations(any())).thenReturn(locations);

        GeneralApplicationCaseData caseData = getCaseWithRespondentResponse();
        GeneralApplicationCaseData.GeneralApplicationCaseDataBuilder<?, ?> updateCaseData = caseData.toBuilder();
        List<GeneralApplicationTypes> types = List.of(SUMMARY_JUDGEMENT);
        updateCaseData.parentClaimantIsApplicant(YES)
            .generalAppType(GAApplicationType.builder().types(types).build()).build();

        CallbackParams params = callbackParamsOf(updateCaseData.build(), CallbackType.ABOUT_TO_START);
        List<String> errors = new ArrayList<>();
        errors.add(RESPONDENT_ERROR);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response).isNotNull();
        assertThat(response.getErrors()).isEqualTo(errors);

        GeneralApplicationCaseData data = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);
        assertThat(data.getGeneralAppVaryJudgementType()).isEqualTo(NO);
    }

    @Test
    void aboutToStartCallbackChecksRespondendResponseBeforeProceeding_VaryJudgement() {

        List<LocationRefData> locations = new ArrayList<>();
        locations.add(LocationRefData.builder().siteName("siteName").courtAddress("court Address").postcode("post code")
                          .courtName("Court Name").region("Region").build());
        when(locationRefDataService.getCourtLocations(any())).thenReturn(locations);

        GeneralApplicationCaseData caseData = getCaseWithRespondentResponse();
        GeneralApplicationCaseData.GeneralApplicationCaseDataBuilder<?, ?> updateCaseData = caseData.toBuilder();
        List<GeneralApplicationTypes> types = List.of(VARY_PAYMENT_TERMS_OF_JUDGMENT);
        updateCaseData.parentClaimantIsApplicant(NO)
            .generalAppType(GAApplicationType.builder().types(types).build()).build();

        CallbackParams params = callbackParamsOf(updateCaseData.build(), CallbackType.ABOUT_TO_START);
        List<String> errors = new ArrayList<>();
        errors.add(RESPONDENT_ERROR);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response).isNotNull();
        assertThat(response.getErrors()).isEqualTo(errors);

        GeneralApplicationCaseData data = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);
        assertThat(data.getGeneralAppVaryJudgementType()).isEqualTo(YES);
    }

    @Test
    void aboutToStartCallbackAddsLocationDetails() {

        List<LocationRefData> locations = new ArrayList<>();
        locations.add(LocationRefData.builder().siteName("siteName").courtAddress("court Address").postcode("post code")
                          .courtName("Court Name").region("Region").build());
        when(locationRefDataService.getCourtLocations(any())).thenReturn(locations);

        CallbackParams params = callbackParamsOf(getCase(AWAITING_RESPONDENT_RESPONSE),
                                                 CallbackType.ABOUT_TO_START);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        GeneralApplicationCaseData data = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

        assertThat(response.getErrors()).isEmpty();
        assertThat(data.getHearingDetailsResp()).isNotNull();
        DynamicList dynamicList = getLocationDynamicList(data);
        assertThat(dynamicList).isNotNull();
        assertThat(locationsFromDynamicList(dynamicList))
            .containsOnly("siteName - court Address - post code");
    }

    @Test
    void midCallBackValidateDebtorPaymentDatePastDateError() {
        GeneralApplicationCaseData caseData = getCase(AWAITING_RESPONDENT_RESPONSE);
        GeneralApplicationCaseData.GeneralApplicationCaseDataBuilder<?, ?> updateCaseData = caseData.toBuilder();
        List<GeneralApplicationTypes> types = List.of(VARY_PAYMENT_TERMS_OF_JUDGMENT);
        updateCaseData.parentClaimantIsApplicant(NO)
            .generalAppType(GAApplicationType.builder().types(types).build())
            .gaRespondentDebtorOffer(
            GARespondentDebtorOfferGAspec.builder().respondentDebtorOffer(
            GARespondentDebtorOfferOptionsGAspec.DECLINE)
                .paymentPlan(GADebtorPaymentPlanGAspec.PAYFULL)
                .paymentSetDate(LocalDate.now().minusDays(2)).build());

        CallbackParams params = callbackParamsOf(updateCaseData.build(),
                                                 CallbackType.MID, "validate-debtor-offer");


        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        assertThat(response.getErrors()).isNotNull();
        assertThat(response.getErrors().get(0)).isEqualTo(PAYMENT_DATE_CANNOT_BE_IN_PAST);
    }

    @Test
    void midCallBackValidateDebtorPaymentDateIsFuture() {
        GeneralApplicationCaseData caseData = getCase(AWAITING_RESPONDENT_RESPONSE);
        GeneralApplicationCaseData.GeneralApplicationCaseDataBuilder<?, ?> updateCaseData = caseData.toBuilder();
        List<GeneralApplicationTypes> types = List.of(VARY_PAYMENT_TERMS_OF_JUDGMENT);
        updateCaseData.generalAppType(GAApplicationType.builder().types(types).build())
            .gaRespondentDebtorOffer(
                GARespondentDebtorOfferGAspec.builder().respondentDebtorOffer(
                        GARespondentDebtorOfferOptionsGAspec.DECLINE)
                    .paymentPlan(GADebtorPaymentPlanGAspec.PAYFULL)
                    .paymentSetDate(LocalDate.now().plusDays(2)).build());

        CallbackParams params = callbackParamsOf(updateCaseData.build(),
                                                 CallbackType.MID, "validate-debtor-offer");


        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void midCallBackRaisesCorrectErrorForInvalidTrailDateRange() {
        CallbackParams params = getParams(0);
        List<String> errors = new ArrayList<>();
        errors.add(INVALID_TRIAL_DATE_RANGE);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response).isNotNull();
        assertThat(response.getErrors()).isEqualTo(errors);
    }

    @Test
    void midCallBackRaisesCorrectErrorForInvalidTrailDateFromBeforeToday() {
        CallbackParams params = getParams(1);
        List<String> errors = new ArrayList<>();
        errors.add(INVALID_TRAIL_DATE_FROM_BEFORE_TODAY);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response).isNotNull();
        assertThat(response.getErrors()).isEqualTo(errors);
    }

    @Test
    void midCallBackRaisesCorrectErrorForTrailDateFromNotAvailable() {
        CallbackParams params = getParams(2);
        List<String> errors = new ArrayList<>();
        errors.add(TRIAL_DATE_FROM_REQUIRED);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response).isNotNull();
        assertThat(response.getErrors()).isEqualTo(errors);
    }

    @Test
    void midCallBackRaisesCorrectErrorForWrongRangeForUnavailableDates() {
        CallbackParams params = getParams(3);
        List<String> errors = new ArrayList<>();
        errors.add(INVALID_UNAVAILABILITY_RANGE);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response).isNotNull();
        assertThat(response.getErrors()).isEqualTo(errors);
    }

    @Test
    void midCallBackRaisesCorrectErrorForDateBeforeToday() {
        CallbackParams params = getParams(4);
        List<String> errors = new ArrayList<>();
        errors.add(INVALID_UNAVAILABLE_DATE_FROM_BEFORE_TODAY);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response).isNotNull();
        assertThat(response.getErrors()).isEqualTo(errors);
    }

    @Test
    void midCallBackRaisesCorrectErrorWhenUnavailableDatesAreNull() {
        CallbackParams params = getParams(5);
        List<String> errors = new ArrayList<>();
        errors.add(UNAVAILABLE_FROM_MUST_BE_PROVIDED);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response).isNotNull();
        assertThat(response.getErrors()).isEqualTo(errors);
    }

    @Test
    void midCallBackRaisesCorrectErrorWhenUnavailableDateFromIsNull() {
        CallbackParams params = getParams(6);
        List<String> errors = new ArrayList<>();
        errors.add(UNAVAILABLE_DATE_RANGE_MISSING);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response).isNotNull();
        assertThat(response.getErrors()).isEqualTo(errors);
    }

    private CallbackParams getParams(int trialRanges) {
        return switch (trialRanges) {
            case 0 -> CallbackParams.builder()
                .type(CallbackType.MID)
                .pageId("hearing-screen-response")
                .baseCaseData(getCaseWithInvalidTrailDateRange())
                .request(CallbackRequest.builder()
                             .eventId("RESPOND_TO_APPLICATION")
                             .build())
                .build();
            case 1 -> CallbackParams.builder()
                .type(CallbackType.MID)
                .pageId("hearing-screen-response")
                .baseCaseData(getCaseWithInvalidDateToRange())
                .request(CallbackRequest.builder()
                             .eventId("RESPOND_TO_APPLICATION")
                             .build())
                .build();
            case 2 -> CallbackParams.builder()
                .type(CallbackType.MID)
                .pageId("hearing-screen-response")
                .baseCaseData(getCaseWithNullFromAndToDate())
                .request(CallbackRequest.builder()
                             .eventId("RESPOND_TO_APPLICATION")
                             .build())
                .build();
            case 3 -> CallbackParams.builder()
                .type(CallbackType.MID)
                .pageId("hearing-screen-response")
                .baseCaseData(getCaseWithUnavailableDates())
                .request(CallbackRequest.builder()
                             .eventId("RESPOND_TO_APPLICATION")
                             .build())
                .build();
            case 4 -> CallbackParams.builder()
                .type(CallbackType.MID)
                .pageId("hearing-screen-response")
                .baseCaseData(getCaseWithUnavailableDatesBeforeToday())
                .request(CallbackRequest.builder()
                             .eventId("RESPOND_TO_APPLICATION")
                             .build())
                .build();
            case 5 -> CallbackParams.builder()
                .type(CallbackType.MID)
                .pageId("hearing-screen-response")
                .baseCaseData(getCaseWithNullUnavailableDates())
                .request(CallbackRequest.builder()
                             .eventId("RESPOND_TO_APPLICATION")
                             .build())
                .build();
            case 6 -> CallbackParams.builder()
                .type(CallbackType.MID)
                .pageId("hearing-screen-response")
                .baseCaseData(getCaseWithNullUnavailableDateFrom())
                .request(CallbackRequest.builder()
                             .eventId("RESPOND_TO_APPLICATION")
                             .build())
                .build();
            default -> CallbackParams.builder()
                .type(CallbackType.MID)
                .pageId("hearing-screen-response")
                .baseCaseData(getCase(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION))
                .request(CallbackRequest.builder()
                             .eventId("RESPOND_TO_APPLICATION")
                             .build())
                .build();
        };
    }

    @Test
    void shouldReturn_Awaiting_Respondent_Response_1Def_2Responses() {

        List<Element<GASolicitorDetailsGAspec>> respondentSols = new ArrayList<>();

        GASolicitorDetailsGAspec respondent1 = GASolicitorDetailsGAspec.builder().id("id")
            .email(DUMMY_EMAIL).organisationIdentifier("org2").build();

        GASolicitorDetailsGAspec respondent2 = GASolicitorDetailsGAspec.builder().id("id")
            .email("abcd2@gmail.com").organisationIdentifier("org2").build();

        respondentSols.add(element(respondent1));
        respondentSols.add(element(respondent2));

        // Civil Claim CaseDate
        CaseDetails civil = CaseDetails.builder().id(123L).build();
        when(coreCaseDataService.getCase(123L)).thenReturn(civil);
        when(caseDetailsConverter.toGeneralApplicationCaseData(civil))
            .thenReturn(getCivilCaseData("abcd@gmail.com", DUMMY_EMAIL, "abcd@gmail.com"));

        GeneralApplicationCaseData caseData = getCase(respondentSols, respondentsResponses);

        // GA CaseData
        CaseDetails ga = CaseDetails.builder().id(456L).build();
        when(coreCaseDataService.getCase(456L)).thenReturn(ga);
        when(caseDetailsConverter.toGeneralApplicationCaseData(ga))
            .thenReturn(caseData);

        CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_SUBMIT);
        CallbackParams.CallbackParamsBuilder callbackParamsBuilder = params.toBuilder();
        callbackParamsBuilder.request(CallbackRequest.builder().caseDetails(ga).build());
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParamsBuilder.build());

        assertThat(response).isNotNull();
        GeneralApplicationCaseData responseCaseData = getResponseCaseData(response);
        assertThat(responseCaseData.getRespondentsResponses()).hasSize(1);
        assertThat(responseCaseData.getRespondentsResponses().get(0).getValue().getGaHearingDetails()
                       .getHearingPreferredLocation().getListItems()).hasSize(1);
        assertThat(responseCaseData.getRespondentsResponses().get(0).getValue().getGaHearingDetails()
                       .getRespondentResponsePartyName()).isEqualTo("Defendant One - Defendant");
    }

    @Test
    void shouldReturn_Application_Submitted_Awaiting_Judicial_Decision_2Def_2Responses() {

        List<Element<GASolicitorDetailsGAspec>> respondentSols = new ArrayList<>();

        GASolicitorDetailsGAspec respondent1 = GASolicitorDetailsGAspec.builder().id("id")
            .email(DUMMY_EMAIL).organisationIdentifier("org2").build();

        GASolicitorDetailsGAspec respondent2 = GASolicitorDetailsGAspec.builder().id("id")
            .email(DUMMY_EMAIL).organisationIdentifier("org2").build();

        respondentSols.add(element(respondent1));
        respondentSols.add(element(respondent2));

        respondentsResponses.add(element(GARespondentResponse.builder()
                                             .generalAppRespondent1Representative(YES).build()));

        // Civil Claim CaseDate
        CaseDetails civil = CaseDetails.builder().id(123L).build();
        when(coreCaseDataService.getCase(123L)).thenReturn(civil);
        when(caseDetailsConverter.toGeneralApplicationCaseData(civil))
            .thenReturn(getCivilCaseData("abcd@gmail.com", "ab@gmail.com", "test@gmail.com"));

        GeneralApplicationCaseData caseData = getCase(respondentSols, respondentsResponses);

        // GA CaseData
        CaseDetails ga = CaseDetails.builder().id(456L).build();
        when(coreCaseDataService.getCase(456L)).thenReturn(ga);
        when(caseDetailsConverter.toGeneralApplicationCaseData(ga))
            .thenReturn(caseData);

        CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_SUBMIT);

        CallbackParams.CallbackParamsBuilder callbackParamsBuilder = params.toBuilder();
        callbackParamsBuilder.request(CallbackRequest.builder().caseDetails(ga).build());
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParamsBuilder.build());

        assertThat(response).isNotNull();

        GeneralApplicationCaseData responseCaseData = getResponseCaseData(response);
        assertThat(responseCaseData.getGeneralAppRespondent1Representative()
                       .getGeneralAppRespondent1Representative()).isNull();
        assertThat(responseCaseData.getRespondentsResponses()).hasSize(2);
        assertThat(responseCaseData.getRespondentsResponses()
                       .get(1).getValue().getGaHearingDetails().getRespondentResponsePartyName())
            .isEqualTo(StringUtils.EMPTY);

    }

    @Test
    void shouldReturn_Application_Submitted_Awaiting_Judicial_Decision_1Def_1Response() {

        List<Element<GASolicitorDetailsGAspec>> respondentSols = new ArrayList<>();

        GASolicitorDetailsGAspec respondent1 = GASolicitorDetailsGAspec.builder().id("id")
            .email(DUMMY_EMAIL).organisationIdentifier("org2").build();

        respondentSols.add(element(respondent1));

        GeneralApplicationCaseData caseData = getCase(respondentSols, respondentsResponses);

        // Civil Claim CaseDate
        CaseDetails civil = CaseDetails.builder().id(123L).build();
        when(coreCaseDataService.getCase(123L)).thenReturn(civil);
        when(caseDetailsConverter.toGeneralApplicationCaseData(civil))
            .thenReturn(getCivilCaseData(DUMMY_EMAIL, DUMMY_EMAIL, DUMMY_EMAIL));

        // GA CaseData
        CaseDetails ga = CaseDetails.builder().id(456L).build();
        when(coreCaseDataService.getCase(456L)).thenReturn(ga);
        when(caseDetailsConverter.toGeneralApplicationCaseData(ga))
            .thenReturn(caseData);

        CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_SUBMIT);
        CallbackParams.CallbackParamsBuilder callbackParamsBuilder = params.toBuilder();
        callbackParamsBuilder.request(CallbackRequest.builder().caseDetails(ga).build());
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParamsBuilder.build());
        GeneralApplicationCaseData responseData = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);
        assertThat(response).isNotNull();
    }

    @Test
    void shouldReturn_Application_Submitted_Awaiting_Judicial_Decision_1Def_1ResponseLip() {

        List<Element<GASolicitorDetailsGAspec>> respondentSols = new ArrayList<>();

        GASolicitorDetailsGAspec respondent1 = GASolicitorDetailsGAspec.builder().id("id")
            .email(DUMMY_EMAIL).organisationIdentifier("org2").build();

        respondentSols.add(element(respondent1));

        GeneralApplicationCaseData caseData = getCase(respondentSols, respondentsResponses);
        GeneralApplicationCaseData updatedCaseData = caseData.toBuilder().parentClaimantIsApplicant(NO).build();

        // Civil Claim CaseDate
        CaseDetails civil = CaseDetails.builder().id(123L).build();
        when(coreCaseDataService.getCase(123L)).thenReturn(civil);
        when(caseDetailsConverter.toGeneralApplicationCaseData(civil))
            .thenReturn(new GeneralApplicationCaseDataBuilder()
                            .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder().id("id")
                                                          .forename("GAApplnSolicitor")
                                                          .email(DUMMY_EMAIL).organisationIdentifier("1").build())
                            .respondentSolicitor1EmailAddress(DUMMY_EMAIL)
                            .build());
        when(gaForLipService.isLipResp(any())).thenReturn(true);
        // GA CaseData
        CaseDetails ga = CaseDetails.builder().id(456L).build();
        when(coreCaseDataService.getCase(456L)).thenReturn(ga);
        when(caseDetailsConverter.toGeneralApplicationCaseData(ga))
            .thenReturn(updatedCaseData);

        CallbackParams params = callbackParamsOf(updatedCaseData, CallbackType.ABOUT_TO_SUBMIT);
        CallbackParams.CallbackParamsBuilder callbackParamsBuilder = params.toBuilder();
        callbackParamsBuilder.request(CallbackRequest.builder().caseDetails(ga).build());
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParamsBuilder.build());
        GeneralApplicationCaseData responseData = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);
        assertThat(response).isNotNull();
    }

    @Test
    void shouldReturn_Application_Submitted_Awaiting_Judicial_Decision_1Def_1Response_test() {

        List<Element<GASolicitorDetailsGAspec>> respondentSols = new ArrayList<>();
        GASolicitorDetailsGAspec respondent1 = GASolicitorDetailsGAspec.builder().id(DEF_UID)
            .email("test@gmail.com").organisationIdentifier("org2").build();
        GASolicitorDetailsGAspec respondent2 = GASolicitorDetailsGAspec.builder().id(DEF2_UID)
            .email(DUMMY_EMAIL).organisationIdentifier("org2").build();
        respondentSols.add(element(respondent2));
        respondentSols.add(element(respondent1));
        GeneralApplicationCaseData caseData = getCaseWithJudicialDecision(respondentSols, respondentsResponses);
        GeneralApplicationCaseData.GeneralApplicationCaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        caseDataBuilder.isMultiParty(YES);

        // Civil Claim Case Data
        CaseDetails civil = CaseDetails.builder().id(123L).build();
        when(coreCaseDataService.getCase(123L)).thenReturn(civil);
        when(caseDetailsConverter.toGeneralApplicationCaseData(civil))
            .thenReturn(getCivilCaseData("ab@gmail.com", "abcd@gmail.com", DUMMY_EMAIL));

        // GA Case Data
        CaseDetails ga = CaseDetails.builder().id(456L).build();
        when(coreCaseDataService.getCase(456L)).thenReturn(ga);
        when(caseDetailsConverter.toGeneralApplicationCaseData(ga))
            .thenReturn(caseDataBuilder.build());

        CallbackParams params = callbackParamsOf(caseDataBuilder.build(), CallbackType.ABOUT_TO_SUBMIT);
        CallbackParams.CallbackParamsBuilder callbackParamsBuilder = params.toBuilder();
        callbackParamsBuilder.request(CallbackRequest.builder().caseDetails(ga).build());
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParamsBuilder.build());
        GeneralApplicationCaseData responseData = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

        assertThat(response).isNotNull();
        assertThat(responseData.getRespondentsResponses()
                       .get(0).getValue().getGaHearingDetails().getRespondentResponsePartyName())
            .isEqualTo("Defendant Two - Defendant");
    }

    @Test
    void shouldReturn_Awaiting_Respondent_Response_2Def_1Response() {

        // Civil Claim Case Data
        CaseDetails civil = CaseDetails.builder().id(123L).build();
        when(coreCaseDataService.getCase(123L)).thenReturn(civil);
        when(caseDetailsConverter.toGeneralApplicationCaseData(civil))
            .thenReturn(getCivilCaseData(DUMMY_EMAIL, DUMMY_EMAIL, DUMMY_EMAIL));

        List<Element<GASolicitorDetailsGAspec>> respondentSols = new ArrayList<>();

        GASolicitorDetailsGAspec respondent1 = GASolicitorDetailsGAspec.builder().id("id")
            .email(DUMMY_EMAIL).organisationIdentifier("org2").build();

        GASolicitorDetailsGAspec respondent2 = GASolicitorDetailsGAspec.builder().id("id")
            .email(DUMMY_EMAIL).organisationIdentifier("org2").build();

        respondentSols.add(element(respondent1));
        respondentSols.add(element(respondent2));

        GeneralApplicationCaseData caseData = getCase(respondentSols, respondentsResponses);

        // GA Case Data
        CaseDetails ga = CaseDetails.builder().id(456L).build();
        when(coreCaseDataService.getCase(456L)).thenReturn(ga);
        when(caseDetailsConverter.toGeneralApplicationCaseData(ga))
            .thenReturn(caseData);

        CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_SUBMIT);

        CallbackParams.CallbackParamsBuilder callbackParamsBuilder = params.toBuilder();
        callbackParamsBuilder.request(CallbackRequest.builder().caseDetails(ga).build());
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParamsBuilder.build());
        assertThat(response).isNotNull();
        GeneralApplicationCaseData responseCaseData = getResponseCaseData(response);
        assertThat(responseCaseData.getGeneralAppRespondent1Representative()
                       .getGeneralAppRespondent1Representative()).isNull();
        assertThat(responseCaseData.getRespondentsResponses()).hasSize(1);
    }

    @Test
    void shouldReturn_Awaiting_Respondent_Response_For_NoDef_NoResponse() {

        // Civil Claim Case Data
        CaseDetails civil = CaseDetails.builder().id(123L).build();
        when(coreCaseDataService.getCase(123L)).thenReturn(civil);
        when(caseDetailsConverter.toGeneralApplicationCaseData(civil))
            .thenReturn(getCivilCaseData(DUMMY_EMAIL, DUMMY_EMAIL, DUMMY_EMAIL));

        GeneralApplicationCaseData caseData = getCase(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION);

        // GA Case Data
        CaseDetails ga = CaseDetails.builder().id(456L).build();
        when(coreCaseDataService.getCase(456L)).thenReturn(ga);
        when(caseDetailsConverter.toGeneralApplicationCaseData(ga))
            .thenReturn(caseData);

        CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_SUBMIT);

        CallbackParams.CallbackParamsBuilder callbackParamsBuilder = params.toBuilder();
        callbackParamsBuilder.request(CallbackRequest.builder().caseDetails(ga).build());
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParamsBuilder.build());
        assertThat(response).isNotNull();
    }

    @Test
    void shouldReturn_Null_WhenPreferredTypeNotInPerson() {

        GeneralApplicationCaseData caseData = getCaseWithPreferredTypeInPersonLocationNull();
        GeneralApplicationCaseData.GeneralApplicationCaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        caseDataBuilder.parentClaimantIsApplicant(NO)
            .generalAppType(GAApplicationType.builder().types(List.of(SUMMARY_JUDGEMENT)).build()).build();

        // Civil Claim CaseDate
        CaseDetails civil = CaseDetails.builder().id(123L).build();
        when(coreCaseDataService.getCase(123L)).thenReturn(civil);
        when(caseDetailsConverter.toGeneralApplicationCaseData(civil))
            .thenReturn(getCivilCaseData(DUMMY_EMAIL, "abcd@gmail.com", "abc@gmail.com"));

        // GA CaseData
        CaseDetails ga = CaseDetails.builder().id(456L).build();
        when(coreCaseDataService.getCase(456L)).thenReturn(ga);
        when(caseDetailsConverter.toGeneralApplicationCaseData(ga))
            .thenReturn(caseDataBuilder.build());

        CallbackParams params = callbackParamsOf(caseDataBuilder.build(), CallbackType.ABOUT_TO_SUBMIT);
        CallbackParams.CallbackParamsBuilder callbackParamsBuilder = params.toBuilder();
        callbackParamsBuilder.request(CallbackRequest.builder().caseDetails(ga).build());
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParamsBuilder.build());

        GeneralApplicationCaseData responseCaseData = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);
        assertThat(response).isNotNull();
        assertThat(responseCaseData.getHearingDetailsResp()).isNull();
        assertThat(responseCaseData.getRespondentsResponses()
                       .get(0).getValue().getGaHearingDetails()
                       .getRespondentResponsePartyName()).isEqualTo("Claimant One - Claimant");
    }

    @Test
    void shouldPopulatePreferredLocation_WhenRespondentIsLiP() {

        GeneralApplicationCaseData caseData = getCase(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION);
        GeneralApplicationCaseData.GeneralApplicationCaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        caseDataBuilder.parentClaimantIsApplicant(NO)
            .generalAppType(GAApplicationType.builder().types(List.of(SUMMARY_JUDGEMENT)).build()).build();
        caseDataBuilder.hearingDetailsResp(caseData.getHearingDetailsResp().toBuilder()
                                               .hearingPreferencesPreferredType(GAHearingType.VIDEO).build());
        caseDataBuilder.isGaRespondentOneLip(YES);

        // Civil Claim CaseDate
        CaseDetails civil = CaseDetails.builder().id(123L).build();
        when(coreCaseDataService.getCase(123L)).thenReturn(civil);
        when(caseDetailsConverter.toGeneralApplicationCaseData(civil))
            .thenReturn(getCivilCaseData(DUMMY_EMAIL, "abcd@gmail.com", "abc@gmail.com"));

        // GA CaseData
        CaseDetails ga = CaseDetails.builder().id(456L).build();
        when(coreCaseDataService.getCase(456L)).thenReturn(ga);
        when(caseDetailsConverter.toGeneralApplicationCaseData(ga))
            .thenReturn(caseDataBuilder.build());

        CallbackParams params = callbackParamsOf(caseDataBuilder.build(), CallbackType.ABOUT_TO_SUBMIT);
        CallbackParams.CallbackParamsBuilder callbackParamsBuilder = params.toBuilder();
        callbackParamsBuilder.request(CallbackRequest.builder().caseDetails(ga).build());
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParamsBuilder.build());

        GeneralApplicationCaseData responseCaseData = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);
        assertThat(response).isNotNull();
        assertThat(responseCaseData.getHearingDetailsResp()).isNull();
        assertThat(responseCaseData.getRespondentsResponses()
                       .get(0).getValue().getGaHearingDetails()
                       .getHearingPreferredLocation().getValue().getLabel()).isEqualTo("ABCD - RG0 0AL");
    }

    @Test
    void shouldReturn_No_WhenRespondIsNotAcceptedByRespondent() {

        // Civil Claim Case Data
        CaseDetails civil = CaseDetails.builder().id(123L).build();
        when(coreCaseDataService.getCase(123L)).thenReturn(civil);
        when(caseDetailsConverter.toGeneralApplicationCaseData(civil))
            .thenReturn(getCivilCaseData(DUMMY_EMAIL, DUMMY_EMAIL, DUMMY_EMAIL));

        GeneralApplicationCaseData caseData = getCaseWithPreferredTypeInPersonLocationNull();
        GeneralApplicationCaseData.GeneralApplicationCaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        caseDataBuilder.parentClaimantIsApplicant(NO)
            .generalAppParentCaseLink(GeneralAppParentCaseLink.builder().caseReference("123").build())
            .defendant2PartyName("Defendant Two")
            .defendant1PartyName("Defendant One")
            .claimant1PartyName("Claimant One")
            .claimant2PartyName("Claimant Two")
            .generalAppType(GAApplicationType.builder().types(List.of(SUMMARY_JUDGEMENT)).build())
            .generalAppRespondReason("reason")
            .generalAppRespondent1Representative(GARespondentRepresentative.builder()
                                                     .generalAppRespondent1Representative(NO).build())
            .generalAppRespondDocument(documents);


        // GA Case Data
        CaseDetails ga = CaseDetails.builder().id(456L).build();
        when(coreCaseDataService.getCase(456L)).thenReturn(ga);
        when(caseDetailsConverter.toGeneralApplicationCaseData(ga))
            .thenReturn(caseDataBuilder.build());

        CallbackParams params = callbackParamsOf(caseDataBuilder.build(), CallbackType.ABOUT_TO_SUBMIT);
        CallbackParams.CallbackParamsBuilder callbackParamsBuilder = params.toBuilder();
        callbackParamsBuilder.request(CallbackRequest.builder().caseDetails(ga).build());

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParamsBuilder.build());
        GeneralApplicationCaseData responseCaseData = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

        assertThat(response).isNotNull();
        assertThat(responseCaseData.getRespondentsResponses().get(0).getValue()
                .getGeneralAppRespondent1Representative()).isEqualTo(NO);
        assertThat(responseCaseData.getRespondentsResponses().get(0)
                .getValue().getGaRespondentResponseReason()).isEqualTo("reason");
        assertThat(responseCaseData.getGeneralAppRespondDocument()).isNull();
        assertThat(responseCaseData.getGeneralAppRespondReason()).isNull();
        assertThat(responseCaseData.getGeneralAppRespondent1Representative().getGeneralAppRespondent1Representative()).isNull();
        assertThat(responseCaseData.getGaAddlDoc().size()).isEqualTo(1);
        assertThat(responseCaseData.getGaAddlDocStaff().size()).isEqualTo(1);
        assertThat(responseCaseData.getGaAddlDocRespondentSol().size()).isEqualTo(1);
        assertThat(responseCaseData.getGaAddlDoc().get(0).getValue().getDocumentName()).isEqualTo("Respond evidence");
        assertThat(responseCaseData.getGaAddlDoc().get(0).getValue().getCreatedBy()).isEqualTo("Respondent One");
        assertThat(responseCaseData.getGaAddlDoc().get(0).getValue().getDocumentLink().getCategoryID()).isEqualTo("applications");
    }

    @Test
    void shouldReturn_No_WhenRespondIsNotAcceptedByRespondentTWO() {
        when(idamClient.getUserInfo(anyString())).thenReturn(UserInfo.builder()
                .sub(DUMMY_EMAIL)
                .uid(DEF2_UID)
                .build());
        // Civil Claim Case Data
        CaseDetails civil = CaseDetails.builder().id(123L).build();
        when(coreCaseDataService.getCase(123L)).thenReturn(civil);
        when(caseDetailsConverter.toGeneralApplicationCaseData(civil))
                .thenReturn(getCivilCaseData(DUMMY_EMAIL, DUMMY_EMAIL, DUMMY_EMAIL));

        GeneralApplicationCaseData caseData = getCaseWithPreferredTypeInPersonLocationNull();
        GeneralApplicationCaseData.GeneralApplicationCaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        caseDataBuilder.parentClaimantIsApplicant(NO)
                .generalAppParentCaseLink(GeneralAppParentCaseLink.builder().caseReference("123").build())
                .defendant2PartyName("Defendant Two")
                .defendant1PartyName("Defendant One")
                .claimant1PartyName("Claimant One")
                .claimant2PartyName("Claimant Two")
                .generalAppType(GAApplicationType.builder().types(List.of(SUMMARY_JUDGEMENT)).build())
                .generalAppRespondReason("reason")
                .generalAppRespondent1Representative(GARespondentRepresentative.builder()
                        .generalAppRespondent1Representative(NO).build())
                .generalAppRespondDocument(documents);


        // GA Case Data
        CaseDetails ga = CaseDetails.builder().id(456L).build();
        when(coreCaseDataService.getCase(456L)).thenReturn(ga);
        when(caseDetailsConverter.toGeneralApplicationCaseData(ga))
                .thenReturn(caseDataBuilder.build());

        CallbackParams params = callbackParamsOf(caseDataBuilder.build(), CallbackType.ABOUT_TO_SUBMIT);
        CallbackParams.CallbackParamsBuilder callbackParamsBuilder = params.toBuilder();
        callbackParamsBuilder.request(CallbackRequest.builder().caseDetails(ga).build());

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParamsBuilder.build());
        GeneralApplicationCaseData responseCaseData = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

        assertThat(response).isNotNull();
        assertThat(responseCaseData.getRespondentsResponses().get(0).getValue()
                .getGeneralAppRespondent1Representative()).isEqualTo(NO);
        assertThat(responseCaseData.getRespondentsResponses().get(0)
                .getValue().getGaRespondentResponseReason()).isEqualTo("reason");
        assertThat(responseCaseData.getGeneralAppRespondDocument()).isNull();
        assertThat(responseCaseData.getGeneralAppRespondReason()).isNull();
        assertThat(responseCaseData.getGeneralAppRespondent1Representative().getGeneralAppRespondent1Representative()).isNull();
        assertThat(responseCaseData.getGaAddlDoc().size()).isEqualTo(1);
        assertThat(responseCaseData.getGaAddlDocStaff().size()).isEqualTo(1);
        assertThat(responseCaseData.getGaAddlDocRespondentSolTwo().size()).isEqualTo(1);
        assertThat(responseCaseData.getGaAddlDoc().get(0).getValue().getDocumentName()).isEqualTo("Respond evidence");
        assertThat(responseCaseData.getGaAddlDoc().get(0).getValue().getCreatedBy()).isEqualTo("Respondent Two");
        assertThat(responseCaseData.getGaAddlDoc().get(0).getValue().getDocumentLink().getCategoryID()).isEqualTo("applications");
    }

    @Test
    void shouldReturn_No_WhenConsentRespondIsNotAcceptedByRespondent() {
        GeneralApplicationCaseData caseData = getCaseWithPreferredTypeInPersonLocationNull();
        GeneralApplicationCaseData.GeneralApplicationCaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        caseDataBuilder.parentClaimantIsApplicant(NO)
                .generalAppType(GAApplicationType.builder().types(List.of(SUMMARY_JUDGEMENT)).build())
                .generalAppConsentOrder(YES)
                .generalAppRespondConsentReason("reason")
                .gaRespondentConsent(NO)
                .generalAppRespondConsentDocument(documents);

        // GA CaseData
        CaseDetails ga = CaseDetails.builder().id(456L).build();
        when(coreCaseDataService.getCase(456L)).thenReturn(ga);
        when(caseDetailsConverter.toGeneralApplicationCaseData(ga))
            .thenReturn(caseDataBuilder.build());

        CallbackParams params = callbackParamsOf(caseDataBuilder.build(), CallbackType.ABOUT_TO_SUBMIT);


        // Civil Claim CaseDate
        CaseDetails civil = CaseDetails.builder().id(123L).build();
        when(coreCaseDataService.getCase(123L)).thenReturn(civil);
        when(caseDetailsConverter.toGeneralApplicationCaseData(civil))
            .thenReturn(getCivilCaseData(DUMMY_EMAIL, DUMMY_EMAIL, DUMMY_EMAIL));

        CallbackParams.CallbackParamsBuilder callbackParamsBuilder = params.toBuilder();
        callbackParamsBuilder.request(CallbackRequest.builder().caseDetails(ga).build());
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParamsBuilder.build());

        GeneralApplicationCaseData responseCaseData = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);
        assertThat(response).isNotNull();
        assertThat(responseCaseData.getRespondentsResponses().get(0).getValue()
                .getGeneralAppRespondent1Representative()).isEqualTo(NO);
        assertThat(responseCaseData.getRespondentsResponses().get(0)
                .getValue().getGaRespondentResponseReason()).isEqualTo("reason");
        assertThat(responseCaseData.getGeneralAppRespondConsentDocument()).isNull();
        assertThat(responseCaseData.getGeneralAppRespondConsentReason()).isNull();
        assertThat(responseCaseData.getGaRespondentConsent()).isNull();
        assertThat(responseCaseData.getGaAddlDoc().size()).isEqualTo(1);
        assertThat(responseCaseData.getGaAddlDocStaff().size()).isEqualTo(1);
        assertThat(responseCaseData.getGaAddlDocRespondentSol().size()).isEqualTo(1);
    }

    @Test
    void shouldReturn_No_WhenDebtorIsDeclinedByRespondent() {
        GeneralApplicationCaseData caseData = getCaseWithPreferredTypeInPersonLocationNull();
        GeneralApplicationCaseData.GeneralApplicationCaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        LocalDate planDate = LocalDate.of(2023, 11, 29);
        caseDataBuilder.parentClaimantIsApplicant(NO)
            .generalAppType(GAApplicationType.builder().types(List.of(VARY_PAYMENT_TERMS_OF_JUDGMENT)).build())
            .gaRespondentDebtorOffer(
            GARespondentDebtorOfferGAspec.builder().respondentDebtorOffer(
                    GARespondentDebtorOfferOptionsGAspec.DECLINE)
                    .debtorObjections("I have no money")
                .paymentPlan(GADebtorPaymentPlanGAspec.PAYFULL)
                .paymentSetDate(planDate).build())
            .generalAppRespondDebtorDocument(documents);

        // Civil Claim CaseDate
        CaseDetails civil = CaseDetails.builder().id(123L).build();
        when(coreCaseDataService.getCase(123L)).thenReturn(civil);
        when(caseDetailsConverter.toGeneralApplicationCaseData(civil))
            .thenReturn(getCivilCaseData(DUMMY_EMAIL, DUMMY_EMAIL, DUMMY_EMAIL));

        // GA CaseData
        CaseDetails ga = CaseDetails.builder().id(456L).build();
        when(coreCaseDataService.getCase(456L)).thenReturn(ga);
        when(caseDetailsConverter.toGeneralApplicationCaseData(ga))
            .thenReturn(caseDataBuilder.build());

        CallbackParams params = callbackParamsOf(caseDataBuilder.build(), CallbackType.ABOUT_TO_SUBMIT);
        CallbackParams.CallbackParamsBuilder callbackParamsBuilder = params.toBuilder();
        callbackParamsBuilder.request(CallbackRequest.builder().caseDetails(ga).build());
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParamsBuilder.build());

        String expectedReason = "Proposed payment plan is I will accept payment in full by a set date. " +
                "Proposed set date is 29 November 2023. " +
                "Objections to the debtor's proposals is I have no money";
        GeneralApplicationCaseData responseCaseData = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);
        assertThat(response).isNotNull();
        assertThat(responseCaseData.getHearingDetailsResp()).isNull();
        assertThat(responseCaseData.getRespondentsResponses().get(0).getValue()
                       .getGeneralAppRespondent1Representative()).isEqualTo(NO);
        assertThat(responseCaseData.getRespondentsResponses().get(0).getValue()
                .getGaRespondentResponseReason()).isEqualTo(expectedReason);
        assertThat(responseCaseData.getGeneralAppRespondDebtorDocument()).isNull();
        assertThat(responseCaseData.getGaAddlDoc().size()).isEqualTo(1);
        assertThat(responseCaseData.getGaAddlDocStaff().size()).isEqualTo(1);
        assertThat(responseCaseData.getGaAddlDocRespondentSol().size()).isEqualTo(1);
    }

    @Test
    void shouldReturn_No_Instalment_WhenDebtorIsDeclinedByRespondent() {
        GeneralApplicationCaseData caseData = getCaseWithPreferredTypeInPersonLocationNull();
        GeneralApplicationCaseData.GeneralApplicationCaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        caseDataBuilder.parentClaimantIsApplicant(NO)
                .generalAppType(GAApplicationType.builder().types(List.of(VARY_PAYMENT_TERMS_OF_JUDGMENT)).build())
            .defendant2PartyName("Defendant Two")
            .defendant1PartyName("Defendant One")
            .claimant1PartyName("Claimant One")
            .claimant2PartyName("Claimant Two")
            .generalAppParentCaseLink(GeneralAppParentCaseLink.builder().caseReference("123").build())
            .gaRespondentDebtorOffer(
                GARespondentDebtorOfferGAspec.builder().respondentDebtorOffer(
                    GARespondentDebtorOfferOptionsGAspec.DECLINE)
                    .debtorObjections("I have no money")
                    .paymentPlan(GADebtorPaymentPlanGAspec.INSTALMENT)
                    .monthlyInstalment(new BigDecimal(1234)).build())
            .generalAppRespondDebtorDocument(documents);

        // GA CaseData
        CaseDetails ga = CaseDetails.builder().id(456L).build();
        when(coreCaseDataService.getCase(456L)).thenReturn(ga);
        when(caseDetailsConverter.toGeneralApplicationCaseData(ga))
            .thenReturn(caseDataBuilder.build());

        // Civil Claim CaseDate
        CaseDetails civil = CaseDetails.builder().id(123L).build();
        when(coreCaseDataService.getCase(123L)).thenReturn(civil);
        when(caseDetailsConverter.toGeneralApplicationCaseData(civil))
            .thenReturn(getCivilCaseData(DUMMY_EMAIL, DUMMY_EMAIL, DUMMY_EMAIL));

        CallbackParams params = callbackParamsOf(caseDataBuilder.build(), CallbackType.ABOUT_TO_SUBMIT);
        String expectedReason = "Proposed payment plan is I will accept the following instalments per month." +
                " Proposed instalments per month is 12.34 pounds." +
                " Objections to the debtor's proposals is I have no money";
        CallbackParams.CallbackParamsBuilder callbackParamsBuilder = params.toBuilder();
        callbackParamsBuilder.request(CallbackRequest.builder().caseDetails(ga).build());
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParamsBuilder.build());
        GeneralApplicationCaseData responseCaseData = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);
        assertThat(responseCaseData.getRespondentsResponses().get(0).getValue()
                .getGaRespondentResponseReason()).isEqualTo(expectedReason);
    }

    @Test
    void shouldReturn_Yes_WhenDebtorIsAcceptedByRespondent() {
        GeneralApplicationCaseData caseData = getCaseWithPreferredTypeInPersonLocationNull();
        GeneralApplicationCaseData.GeneralApplicationCaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        caseDataBuilder.parentClaimantIsApplicant(NO)
            .generalAppType(GAApplicationType.builder().types(List.of(VARY_PAYMENT_TERMS_OF_JUDGMENT)).build())
            .gaRespondentDebtorOffer(
                GARespondentDebtorOfferGAspec.builder().respondentDebtorOffer(
                        GARespondentDebtorOfferOptionsGAspec.ACCEPT)
                    .paymentPlan(GADebtorPaymentPlanGAspec.PAYFULL).build());

        CallbackParams params = callbackParamsOf(caseDataBuilder.build(), CallbackType.ABOUT_TO_SUBMIT);

        // Civil Claim Case Data
        CaseDetails civil = CaseDetails.builder().id(123L).build();
        when(coreCaseDataService.getCase(123L)).thenReturn(civil);
        when(caseDetailsConverter.toGeneralApplicationCaseData(civil))
            .thenReturn(getCivilCaseData(DUMMY_EMAIL, DUMMY_EMAIL, DUMMY_EMAIL));

        // GA Case Data
        CaseDetails ga = CaseDetails.builder().id(456L).build();
        when(coreCaseDataService.getCase(456L)).thenReturn(ga);
        when(caseDetailsConverter.toGeneralApplicationCaseData(ga))
            .thenReturn(caseDataBuilder.build());

        CallbackParams.CallbackParamsBuilder callbackParamsBuilder = params.toBuilder();
        callbackParamsBuilder.request(CallbackRequest.builder().caseDetails(ga).build());
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParamsBuilder.build());

        GeneralApplicationCaseData responseCaseData = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);
        assertThat(response).isNotNull();
        assertThat(responseCaseData.getHearingDetailsResp()).isNull();
        assertThat(responseCaseData.getRespondentsResponses().get(0).getValue()
                       .getGeneralAppRespondent1Representative()).isEqualTo(YES);
    }

    @Test
    void shouldReturn_Null_RespondentResponseAfterAddingToCollections() {

        List<Element<GASolicitorDetailsGAspec>> respondentSols = new ArrayList<>();

        GASolicitorDetailsGAspec respondent1 = GASolicitorDetailsGAspec.builder().id("id")
            .email(DUMMY_EMAIL).organisationIdentifier("org2").build();

        respondentSols.add(element(respondent1));

        GeneralApplicationCaseData caseData = getCase(respondentSols, respondentsResponses);
        DynamicList dynamicListTest = fromList(getSampleCourLocations());
        Optional<DynamicListElement> first = dynamicListTest.getListItems().stream().findFirst();
        first.ifPresent(dynamicListTest::setValue);

        GeneralApplicationCaseData updatedCaseData = caseData.toBuilder().hearingDetailsResp(GAHearingDetails.builder()
                                                                               .hearingPreferredLocation(
                                                                                   dynamicListTest)
                                                                               .hearingPreferencesPreferredType(
                                                                                   GAHearingType.IN_PERSON)
                                                                               .build()).build();
        // Civil Claim Case Data
        CaseDetails civil = CaseDetails.builder().id(123L).build();
        when(coreCaseDataService.getCase(123L)).thenReturn(civil);
        when(caseDetailsConverter.toGeneralApplicationCaseData(civil))
            .thenReturn(getCivilCaseData(DUMMY_EMAIL, DUMMY_EMAIL, DUMMY_EMAIL));

        // GA Case Data
        CaseDetails ga = CaseDetails.builder().id(456L).build();
        when(coreCaseDataService.getCase(456L)).thenReturn(ga);
        when(caseDetailsConverter.toGeneralApplicationCaseData(ga))
            .thenReturn(updatedCaseData);

        CallbackParams params = callbackParamsOf(updatedCaseData, CallbackType.ABOUT_TO_SUBMIT);
        CallbackParams.CallbackParamsBuilder callbackParamsBuilder = params.toBuilder();
        callbackParamsBuilder.request(CallbackRequest.builder().caseDetails(ga).build());
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParamsBuilder.build());

        assertThat(response).isNotNull();
        GeneralApplicationCaseData responseCaseData = getResponseCaseData(response);
        assertThat(responseCaseData.getGeneralAppRespondent1Representative()
                       .getGeneralAppRespondent1Representative()).isNull();
        assertThat(responseCaseData.getRespondentsResponses()).hasSize(1);
    }

    private GeneralApplicationCaseData getResponseCaseData(AboutToStartOrSubmitCallbackResponse response) {
        return objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);
    }

    private GeneralApplicationCaseData getCaseWithNullUnavailableDateFrom() {
        return GeneralApplicationCaseData.builder()
            .hearingDetailsResp(GAHearingDetails.builder()
                                    .unavailableTrialRequiredYesOrNo(YES)
                                    .generalAppUnavailableDates(null)
                                    .build())
            .build();
    }

    private GeneralApplicationCaseData getCaseWithNullUnavailableDates() {
        return GeneralApplicationCaseData.builder()
            .hearingDetailsResp(GAHearingDetails.builder()
                                    .unavailableTrialRequiredYesOrNo(YES)
                                    .generalAppUnavailableDates(getUnavailableNullDateList())
                                    .build())
            .build();
    }

    private GeneralApplicationCaseData getCaseWithInvalidTrailDateRange() {
        return GeneralApplicationCaseData.builder()
            .hearingDetailsResp(GAHearingDetails.builder()
                                    .trialRequiredYesOrNo(YES)
                                    .trialDateFrom(TRIAL_DATE_FROM_INVALID)
                                    .trialDateTo(TRIAL_DATE_TO_BEFORE_INVALID)
                                    .build())
            .build();
    }

    private GeneralApplicationCaseData getCaseWithInvalidDateToRange() {
        return GeneralApplicationCaseData.builder()
            .hearingDetailsResp(GAHearingDetails.builder()
                                    .trialRequiredYesOrNo(YES)
                                    .trialDateFrom(TRIAL_DATE_FROM_INVALID)
                                    .trialDateTo(TRIAL_DATE_FROM_AFTER_INVALID)
                                    .build())
            .build();
    }

    private GeneralApplicationCaseData getCaseWithNullFromAndToDate() {
        return GeneralApplicationCaseData.builder()
            .generalAppParentCaseLink(GeneralAppParentCaseLink.builder().caseReference("123").build())
            .defendant2PartyName("Defendant Two")
            .defendant1PartyName("Defendant One")
            .claimant1PartyName("Claimant One")
            .claimant2PartyName("Claimant Two")
            .hearingDetailsResp(GAHearingDetails.builder()
                                    .trialRequiredYesOrNo(YES)
                                    .trialDateFrom(null)
                                    .trialDateTo(null)
                                    .build())
            .build();
    }

    private GeneralApplicationCaseData getCaseWithPreferredTypeInPersonLocationNull() {
        return GeneralApplicationCaseData.builder()
            .generalAppParentCaseLink(GeneralAppParentCaseLink.builder().caseReference("123").build())
                .parentClaimantIsApplicant(YES)
                .generalAppParentCaseLink(GeneralAppParentCaseLink.builder().caseReference("123").build())
                .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder()
                        .email("abc@gmail.com").id(APP_UID).build())
                .generalAppRespondentSolicitors(getRespondentSolicitors())
            .defendant2PartyName("Defendant Two")
            .defendant1PartyName("Defendant One")
            .claimant1PartyName("Claimant One")
            .claimant2PartyName("Claimant Two")
            .hearingDetailsResp(GAHearingDetails.builder()
                                    .hearingPreferencesPreferredType(GAHearingType.IN_PERSON)
                                    .build())
            .build();
    }

    private GeneralApplicationCaseData getCaseWithUnavailableDates() {
        return GeneralApplicationCaseData.builder()
            .generalAppParentCaseLink(GeneralAppParentCaseLink.builder().caseReference("123").build())
            .defendant2PartyName("Defendant Two")
            .defendant1PartyName("Defendant One")
            .claimant1PartyName("Claimant One")
            .claimant2PartyName("Claimant Two")
            .hearingDetailsResp(GAHearingDetails.builder()
                                    .unavailableTrialRequiredYesOrNo(YES)
                                    .generalAppUnavailableDates(getUnavailableDateList())
                                    .build())
            .build();
    }

    private GeneralApplicationCaseData getCaseWithUnavailableDatesBeforeToday() {
        return GeneralApplicationCaseData.builder()
            .generalAppParentCaseLink(GeneralAppParentCaseLink.builder().caseReference("123").build())
            .defendant2PartyName("Defendant Two")
            .defendant1PartyName("Defendant One")
            .claimant1PartyName("Claimant One")
            .claimant2PartyName("Claimant Two")
            .hearingDetailsResp(GAHearingDetails.builder()
                                    .unavailableTrialRequiredYesOrNo(YES)
                                    .generalAppUnavailableDates(getUnavailableDateBeforeToday())
                                    .build())
            .build();
    }

    private List<Element<GAUnavailabilityDates>> getUnavailableNullDateList() {
        GAUnavailabilityDates invalidDates = GAUnavailabilityDates.builder()
            .unavailableTrialDateFrom(null)
            .unavailableTrialDateTo(null)
            .build();
        return wrapElements(invalidDates);
    }

    private List<Element<GAUnavailabilityDates>> getUnavailableDateList() {
        GAUnavailabilityDates invalidDates = GAUnavailabilityDates.builder()
            .unavailableTrialDateFrom(UNAVAILABILITY_DATE_FROM_INVALID)
            .unavailableTrialDateTo(UNAVAILABILITY_DATE_TO_INVALID)
            .build();
        return wrapElements(invalidDates);
    }

    private List<Element<GAUnavailabilityDates>> getUnavailableDateBeforeToday() {
        GAUnavailabilityDates invalidDates = GAUnavailabilityDates.builder()
            .unavailableTrialDateFrom(UNAVAILABILITY_DATE_FROM_INVALID)
            .build();
        return wrapElements(invalidDates);
    }

    private GeneralApplicationCaseData getCaseWithRespondentResponse() {

        respondentsResponses.add(element(GARespondentResponse.builder()
                                             .generalAppRespondent1Representative(NO)
                                             .gaRespondentDetails(DEF_UID).build()));
        return GeneralApplicationCaseData.builder().parentClaimantIsApplicant(YES)
                .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder()
                        .email("abc@gmail.com").id(APP_UID).build())
                .generalAppRespondentSolicitors(getRespondentSolicitors())
            .respondentsResponses(respondentsResponses)
            .build();
    }

    private List<Element<GASolicitorDetailsGAspec>> getRespondentSolicitors() {
        List<Element<GASolicitorDetailsGAspec>> respondentSols = new ArrayList<>();

        GASolicitorDetailsGAspec respondent1 = GASolicitorDetailsGAspec.builder().id(DEF_UID)
                .email("test@gmail.com").organisationIdentifier("org2").build();

        GASolicitorDetailsGAspec respondent2 = GASolicitorDetailsGAspec.builder().id(DEF2_UID)
                .email("test@gmail.com").organisationIdentifier("org3").build();

        respondentSols.add(element(respondent1));
        respondentSols.add(element(respondent2));

        return respondentSols;
    }

    private GeneralApplicationCaseData getVaryCase(CaseState state) {
        List<GeneralApplicationTypes> types = List.of(
            (VARY_PAYMENT_TERMS_OF_JUDGMENT));
        DynamicList dynamicListTest = fromList(getSampleCourLocations());
        Optional<DynamicListElement> first = dynamicListTest.getListItems().stream().findFirst();
        first.ifPresent(dynamicListTest::setValue);

        return GeneralApplicationCaseData.builder()
            .generalAppRespondent1Representative(
                GARespondentRepresentative.builder()
                    .generalAppRespondent1Representative(YES)
                    .build())
            .defendant2PartyName("Defendant Two")
            .defendant1PartyName("Defendant One")
            .claimant1PartyName("Claimant One")
            .claimant2PartyName("Claimant Two")
            .judicialListForHearing(GAJudgesHearingListGAspec.builder()
                                        .hearingPreferredLocation(dynamicListTest)
                                        .hearingPreferencesPreferredType(GAJudicialHearingType.IN_PERSON)
                                        .build())
            .hearingDetailsResp(GAHearingDetails.builder()
                                    .hearingPreferredLocation(dynamicListTest)
                                    .hearingPreferencesPreferredType(GAHearingType.IN_PERSON)
                                    .build())
            .generalAppType(
                GAApplicationType
                    .builder()
                    .types(types).build())
            .parentClaimantIsApplicant(NO)
            .generalAppParentCaseLink(GeneralAppParentCaseLink.builder().caseReference("123").build())
            .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder()
                                          .email("abc@gmail.com").id(APP_UID).build())
            .generalAppRespondentSolicitors(getRespondentSolicitors())
            .businessProcess(BusinessProcess
                                 .builder()
                                 .camundaEvent(CAMUNDA_EVENT)
                                 .processInstanceId(BUSINESS_PROCESS_INSTANCE_ID)
                                 .status(BusinessProcessStatus.STARTED)
                                 .activityId(ACTIVITY_ID)
                                 .build())
            .ccdState(state)
            .build();
    }

    private GeneralApplicationCaseData getCase(CaseState state) {
        List<GeneralApplicationTypes> types = List.of(
            (GeneralApplicationTypes.SUMMARY_JUDGEMENT));
        DynamicList dynamicListTest = fromList(getSampleCourLocations());
        Optional<DynamicListElement> first = dynamicListTest.getListItems().stream().findFirst();
        first.ifPresent(dynamicListTest::setValue);

        return GeneralApplicationCaseData.builder()
            .generalAppRespondent1Representative(
                GARespondentRepresentative.builder()
                    .generalAppRespondent1Representative(YES)
                    .build())
            .defendant2PartyName("Defendant Two")
            .defendant1PartyName("Defendant One")
            .claimant1PartyName("Claimant One")
            .claimant2PartyName("Claimant Two")
            .judicialListForHearing(GAJudgesHearingListGAspec.builder()
                                       .hearingPreferredLocation(dynamicListTest)
                                        .hearingPreferencesPreferredType(GAJudicialHearingType.IN_PERSON)
                                       .build())
            .hearingDetailsResp(GAHearingDetails.builder()
                                    .hearingPreferredLocation(dynamicListTest)
                                    .hearingPreferencesPreferredType(GAHearingType.IN_PERSON)
                                    .build())
            .generalAppType(
                GAApplicationType
                    .builder()
                    .types(types).build())
            .parentClaimantIsApplicant(NO)
            .generalAppParentCaseLink(GeneralAppParentCaseLink.builder().caseReference("123").build())
                .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder()
                        .email("abc@gmail.com").id(APP_UID).build())
            .generalAppRespondentSolicitors(getRespondentSolicitors())
            .businessProcess(BusinessProcess
                                 .builder()
                                 .camundaEvent(CAMUNDA_EVENT)
                                 .processInstanceId(BUSINESS_PROCESS_INSTANCE_ID)
                                 .status(BusinessProcessStatus.STARTED)
                                 .activityId(ACTIVITY_ID)
                                 .build())
            .ccdState(state)
            .build();
    }

    private GeneralApplicationCaseData getCase(List<Element<GASolicitorDetailsGAspec>> respondentSols,
                             List<Element<GARespondentResponse>> respondentsResponses) {
        List<GeneralApplicationTypes> types = List.of(
            (GeneralApplicationTypes.SUMMARY_JUDGEMENT));
        DynamicList dynamicListTest = fromList(getSampleCourLocations());
        Optional<DynamicListElement> first = dynamicListTest.getListItems().stream().findFirst();
        first.ifPresent(dynamicListTest::setValue);

        return GeneralApplicationCaseData.builder()
                .parentClaimantIsApplicant(YES)
                .generalAppParentCaseLink(GeneralAppParentCaseLink.builder().caseReference("123").build())
                .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder()
                        .email("abc@gmail.com").id(APP_UID).build())
            .generalAppRespondentSolicitors(respondentSols)
            .hearingDetailsResp(GAHearingDetails.builder()
                                    .hearingPreferredLocation(
                                        dynamicListTest)
                                    .hearingPreferencesPreferredType(GAHearingType.IN_PERSON)
                                    .build())
            .respondentsResponses(respondentsResponses)
            .generalAppRespondent1Representative(
                GARespondentRepresentative.builder()
                    .generalAppRespondent1Representative(YES)
                    .build())
            .parentClaimantIsApplicant(YES)
            .generalAppParentCaseLink(GeneralAppParentCaseLink.builder().caseReference("123").build())
            .defendant2PartyName("Defendant Two")
            .defendant1PartyName("Defendant One")
            .claimant1PartyName("Claimant One")
            .claimant2PartyName("Claimant Two")
            .generalAppType(
                GAApplicationType
                    .builder()
                    .types(types).build())
            .businessProcess(BusinessProcess
                                 .builder()
                                 .camundaEvent(CAMUNDA_EVENT)
                                 .processInstanceId(BUSINESS_PROCESS_INSTANCE_ID)
                                 .status(BusinessProcessStatus.STARTED)
                                 .activityId(ACTIVITY_ID)
                                 .build())
            .ccdState(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
            .build();
    }

    private GeneralApplicationCaseData getCaseWithJudicialDecision(List<Element<GASolicitorDetailsGAspec>> respondentSols,
                             List<Element<GARespondentResponse>> respondentsResponses) {
        List<GeneralApplicationTypes> types = List.of(
            (GeneralApplicationTypes.SUMMARY_JUDGEMENT));
        DynamicList dynamicListTest = fromList(getSampleCourLocations());
        Optional<DynamicListElement> first = dynamicListTest.getListItems().stream().findFirst();
        first.ifPresent(dynamicListTest::setValue);

        return GeneralApplicationCaseData.builder()
            .parentClaimantIsApplicant(YES)
            .generalAppParentCaseLink(GeneralAppParentCaseLink.builder().caseReference("123").build())
                .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder()
                        .email("abc@gmail.com").id(APP_UID).build())
            .generalAppRespondentSolicitors(respondentSols)
            .hearingDetailsResp(GAHearingDetails.builder()
                                    .hearingPreferredLocation(
                                        dynamicListTest)
                                    .hearingPreferencesPreferredType(GAHearingType.IN_PERSON)
                                    .build())
            .respondentsResponses(respondentsResponses)
            .generalAppRespondent1Representative(
                GARespondentRepresentative.builder()
                    .generalAppRespondent1Representative(YES)
                    .build())
            .defendant2PartyName("Defendant Two")
            .defendant1PartyName("Defendant One")
            .claimant1PartyName("Claimant One")
            .claimant2PartyName("Claimant Two")
            .generalAppType(
                GAApplicationType
                    .builder()
                    .types(types).build())
            .judicialDecision(GAJudicialDecision.builder().decision(
                GAJudgeDecisionOption.MAKE_ORDER_FOR_WRITTEN_REPRESENTATIONS).build())
            .businessProcess(BusinessProcess
                                 .builder()
                                 .camundaEvent(CAMUNDA_EVENT)
                                 .processInstanceId(BUSINESS_PROCESS_INSTANCE_ID)
                                 .status(BusinessProcessStatus.STARTED)
                                 .activityId(ACTIVITY_ID)
                                 .build())
            .ccdState(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
            .build();
    }

    private DynamicList getLocationDynamicList(GeneralApplicationCaseData responseCaseData) {
        return responseCaseData.getHearingDetailsResp().getHearingPreferredLocation();
    }

    private List<String> locationsFromDynamicList(DynamicList dynamicList) {
        return dynamicList.getListItems().stream()
            .map(DynamicListElement::getLabel)
            .collect(Collectors.toList());
    }

    protected List<String> getSampleCourLocations() {
        return new ArrayList<>(Arrays.asList("ABCD - RG0 0AL", "PQRS - GU0 0EE", "WXYZ - EW0 0HE", "LMNO - NE0 0BH"));
    }

    public GeneralApplicationCaseData getCivilCaseData(String applicantEmail, String respondent1SolEmail, String respondent2SolEmail) {

        return new GeneralApplicationCaseDataBuilder()
            .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder().id("id").forename("GAApplnSolicitor")
                                          .email(DUMMY_EMAIL).organisationIdentifier("1").build())
            .respondentSolicitor1EmailAddress(respondent1SolEmail)
            .respondentSolicitor2EmailAddress(respondent2SolEmail)
            .applicantSolicitor1UserDetails(IdamUserDetails.builder()
                                                .id("123")
                                                .email(applicantEmail)
                                                .build())
            .respondent2SameLegalRepresentative(NO)
            .build();
    }
}
