package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.hearing.ListingOrRelisting;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.model.citizenui.FeePaymentOutcomeDetails;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.hearingnotice.HearingNoticeCamundaService;
import uk.gov.hmcts.reform.civil.service.hearingnotice.HearingNoticeVariables;
import uk.gov.hmcts.reform.civil.service.hearings.HearingFeesService;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_CLAIMANT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_CLAIMANT_HMC;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_PROGRESSION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.HEARING_READINESS;
import static uk.gov.hmcts.reform.civil.enums.hearing.ListingOrRelisting.LISTING;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_HEARING_DOCUMENTS_UPLOAD_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_HEARING_FEE_REQUIRED_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_HEARING_SCHEDULED_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_TRIAL_ARRANGEMENTS_RELIST_HEARING_CLAIMANT;

@ExtendWith(MockitoExtension.class)
class HearingScheduledClaimantNotificationHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private HearingScheduledClaimantNotificationHandler handler;

    @Mock
    private DashboardScenariosService dashboardScenariosService;

    @Mock
    private DashboardNotificationsParamsMapper dashboardNotificationsParamsMapper;

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private LocationReferenceDataService locationRefDataService;

    @Mock
    private HearingNoticeCamundaService hearingNoticeCamundaService;

    @Mock
    private HearingFeesService hearingFeesService;

    public static final String TASK_ID = "GenerateDashboardNotificationHearingScheduledClaimant";

    HashMap<String, Object> params = new HashMap<>();

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_CLAIMANT,
                                                     CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_CLAIMANT_HMC);
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(
            CallbackParamsBuilder.builder()
                .request(CallbackRequest.builder()
                             .eventId(CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_CLAIMANT.name())
                             .build())
                .build()))
            .isEqualTo(TASK_ID);
    }

    @Test
    void createDashboardNotifications() {
        List<LocationRefData> locations = new ArrayList<>();
        locations.add(LocationRefData.builder().siteName("Name").courtAddress("Loc").postcode("1").build());
        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
        when(locationRefDataService.getHearingCourtLocations(any())).thenReturn(locations);

        DynamicListElement location = new DynamicListElement();
        location.setLabel("Name - Loc - 1");
        DynamicList list = new DynamicList();
        list.setValue(location);
        list.setListItems(List.of(location));
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setLegacyCaseReference("reference");
        caseData.setCcdCaseReference(1234L);
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setResponseClaimTrack("FAST_CLAIM");
        caseData.setHearingLocation(list);

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .request(CallbackRequest.builder().eventId("CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_CLAIMANT").build())
            .build();

        handler.handle(callbackParams);
        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_CP_HEARING_SCHEDULED_CLAIMANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );
        recordScenarioForTrialArrangementsAndDocumentsUpload(caseData);
        verifyNoMoreInteractions(dashboardScenariosService);
    }

    @Test
    void shouldCreateDashboardNotificationsForHearingFeeIfCaseInHRAndListing() {
        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setLegacyCaseReference("reference");
        caseData.setCcdCaseReference(1234L);
        caseData.setCcdState(HEARING_READINESS);
        caseData.setListingOrRelisting(LISTING);
        caseData.setApplicant1Represented(YesOrNo.NO);

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .request(CallbackRequest.builder().eventId("CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_CLAIMANT").build())
            .build();

        handler.handle(callbackParams);
        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_CP_HEARING_SCHEDULED_CLAIMANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );

        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_CP_HEARING_FEE_REQUIRED_CLAIMANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );
    }

    @Test
    void shouldCreateDashboardNotificationsForHearingFeeIfCaseInCPAndListing() {
        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setLegacyCaseReference("reference");
        caseData.setCcdCaseReference(1234L);
        caseData.setCcdState(CASE_PROGRESSION);
        caseData.setListingOrRelisting(LISTING);
        caseData.setApplicant1Represented(YesOrNo.NO);

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .request(CallbackRequest.builder().eventId("CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_CLAIMANT").build())
            .build();

        handler.handle(callbackParams);
        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_CP_HEARING_SCHEDULED_CLAIMANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );

        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_CP_HEARING_FEE_REQUIRED_CLAIMANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );
    }

    @Test
    void shouldCreateDashboardNotificationsForHearingFeeIfFeePaymentFailure_HMC() {
        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
        HearingNoticeVariables hearingNoticeVariables = new HearingNoticeVariables();
        hearingNoticeVariables.setHearingId("HER1234");
        hearingNoticeVariables.setHearingType("AAA7-TRI");
        when(hearingNoticeCamundaService.getProcessVariables(any()))
            .thenReturn(hearingNoticeVariables);
        Fee fee = new Fee();
        fee.setCalculatedAmountInPence(new BigDecimal(10));
        when(hearingFeesService.getFeeForHearingSmallClaims(any())).thenReturn(fee);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setLegacyCaseReference("reference");
        caseData.setCcdCaseReference(1234L);
        caseData.setTotalClaimAmount(new BigDecimal(100));
        caseData.setResponseClaimTrack("SMALL_CLAIM");
        caseData.setApplicant1Represented(YesOrNo.NO);
        PaymentDetails paymentDetails = new PaymentDetails();
        paymentDetails.setStatus(PaymentStatus.FAILED);
        caseData.setHearingFeePaymentDetails(paymentDetails);
        BusinessProcess businessProcess = new BusinessProcess();
        businessProcess.setProcessInstanceId("");
        caseData.setBusinessProcess(businessProcess);

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .request(CallbackRequest.builder().eventId("CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_CLAIMANT_HMC").build())
            .build();

        handler.handle(callbackParams);
        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_CP_HEARING_SCHEDULED_CLAIMANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );

        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_CP_HEARING_FEE_REQUIRED_CLAIMANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );

        verify(hearingFeesService).getFeeForHearingSmallClaims(new BigDecimal(100).setScale(2, RoundingMode.UNNECESSARY));
    }

    @Test
    void shouldCreateDashboardNotificationsForHearingFeeIfFeeNeverPaid_HMC() {
        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
        HearingNoticeVariables hearingNoticeVariables = new HearingNoticeVariables();
        hearingNoticeVariables.setHearingId("HER1234");
        hearingNoticeVariables.setHearingType("AAA7-TRI");
        when(hearingNoticeCamundaService.getProcessVariables(any()))
            .thenReturn(hearingNoticeVariables);
        Fee fee = new Fee();
        fee.setCalculatedAmountInPence(new BigDecimal(10));
        when(hearingFeesService.getFeeForHearingSmallClaims(any())).thenReturn(fee);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setLegacyCaseReference("reference");
        caseData.setCcdCaseReference(1234L);
        caseData.setTotalClaimAmount(new BigDecimal(100));
        caseData.setResponseClaimTrack("SMALL_CLAIM");
        caseData.setApplicant1Represented(YesOrNo.NO);
        BusinessProcess businessProcess = new BusinessProcess();
        businessProcess.setProcessInstanceId("");
        caseData.setBusinessProcess(businessProcess);

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .request(CallbackRequest.builder().eventId("CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_CLAIMANT_HMC").build())
            .build();

        handler.handle(callbackParams);
        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_CP_HEARING_SCHEDULED_CLAIMANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );

        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_CP_HEARING_FEE_REQUIRED_CLAIMANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );

        verify(hearingFeesService).getFeeForHearingSmallClaims(new BigDecimal(100).setScale(2, RoundingMode.UNNECESSARY));
    }

    private void recordScenarioForTrialArrangementsAndDocumentsUpload(CaseData caseData) {
        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_CP_TRIAL_ARRANGEMENTS_RELIST_HEARING_CLAIMANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );

        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_CP_HEARING_DOCUMENTS_UPLOAD_CLAIMANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );
    }

    @Test
    void shouldNotCreateDashboardNotificationsForHearingFeeIfFeePaymentSuccess_HMC() {
        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
        HearingNoticeVariables hearingNoticeVariables = new HearingNoticeVariables();
        hearingNoticeVariables.setHearingId("HER1234");
        hearingNoticeVariables.setHearingType("AAA7-TRI");
        when(hearingNoticeCamundaService.getProcessVariables(any()))
            .thenReturn(hearingNoticeVariables);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setLegacyCaseReference("reference");
        caseData.setCcdCaseReference(1234L);
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setResponseClaimTrack("FAST_CLAIM");
        PaymentDetails paymentDetails = new PaymentDetails();
        paymentDetails.setStatus(PaymentStatus.SUCCESS);
        BusinessProcess businessProcess = new BusinessProcess();
        businessProcess.setProcessInstanceId("");
        caseData.setHearingFeePaymentDetails(paymentDetails);
        caseData.setBusinessProcess(businessProcess);

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .request(CallbackRequest.builder().eventId("CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_CLAIMANT_HMC").build())
            .build();

        handler.handle(callbackParams);
        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_CP_HEARING_SCHEDULED_CLAIMANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );
        recordScenarioForTrialArrangementsAndDocumentsUpload(caseData);
        verifyNoMoreInteractions(dashboardScenariosService);
        verifyNoInteractions(hearingFeesService);
    }

    @Test
    void shouldNotCreateDashboardNotificationsForHearingFeeIfHearingTypeDisposal_HMC() {
        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
        HearingNoticeVariables hearingNoticeVariables = new HearingNoticeVariables();
        hearingNoticeVariables.setHearingId("HER1234");
        hearingNoticeVariables.setHearingType("AAA7-DIS");
        when(hearingNoticeCamundaService.getProcessVariables(any()))
            .thenReturn(hearingNoticeVariables);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setLegacyCaseReference("reference");
        caseData.setCcdCaseReference(1234L);
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setResponseClaimTrack("FAST_CLAIM");
        BusinessProcess businessProcess = new BusinessProcess();
        businessProcess.setProcessInstanceId("");
        caseData.setBusinessProcess(businessProcess);

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .request(CallbackRequest.builder().eventId("CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_CLAIMANT_HMC").build())
            .build();

        handler.handle(callbackParams);
        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_CP_HEARING_SCHEDULED_CLAIMANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );
        recordScenarioForTrialArrangementsAndDocumentsUpload(caseData);
        verifyNoMoreInteractions(dashboardScenariosService);
        verifyNoInteractions(hearingFeesService);
    }

    @Test
    void shouldNotCreateDashboardNotificationsForHearingFeeIfPaidWithHwF_HMC() {
        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
        HearingNoticeVariables hearingNoticeVariables = new HearingNoticeVariables();
        hearingNoticeVariables.setHearingId("HER1234");
        hearingNoticeVariables.setHearingType("AAA7-TRI");
        when(hearingNoticeCamundaService.getProcessVariables(any()))
            .thenReturn(hearingNoticeVariables);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setLegacyCaseReference("reference");
        caseData.setCcdCaseReference(1234L);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setResponseClaimTrack("FAST_CLAIM");
        caseData.setHearingHelpFeesReferenceNumber("123");
        FeePaymentOutcomeDetails feePaymentOutcomeDetails  = new FeePaymentOutcomeDetails();
        feePaymentOutcomeDetails.setHwfFullRemissionGrantedForHearingFee(YesOrNo.YES);
        caseData.setFeePaymentOutcomeDetails(feePaymentOutcomeDetails);
        BusinessProcess businessProcess = new BusinessProcess();
        businessProcess.setProcessInstanceId("");
        caseData.setBusinessProcess(businessProcess);

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .request(CallbackRequest.builder().eventId("CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_CLAIMANT_HMC").build())
            .build();

        handler.handle(callbackParams);
        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_CP_HEARING_SCHEDULED_CLAIMANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );
        recordScenarioForTrialArrangementsAndDocumentsUpload(caseData);
        verifyNoMoreInteractions(dashboardScenariosService);
        verifyNoInteractions(hearingFeesService);
    }

    @Test
    void shouldCreateDashboardNotificationsForHearingFeeIfNotPaidWithHwF_HMC() {
        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
        HearingNoticeVariables hearingNoticeVariables = new HearingNoticeVariables();
        hearingNoticeVariables.setHearingId("HER1234");
        hearingNoticeVariables.setHearingType("AAA7-TRI");
        when(hearingNoticeCamundaService.getProcessVariables(any()))
            .thenReturn(hearingNoticeVariables);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setLegacyCaseReference("reference");
        caseData.setCcdCaseReference(1234L);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setTotalClaimAmount(new BigDecimal(100));
        caseData.setResponseClaimTrack("SMALL_CLAIM");
        caseData.setHearingHelpFeesReferenceNumber("123");
        BusinessProcess businessProcess = new BusinessProcess();
        businessProcess.setProcessInstanceId("");
        caseData.setBusinessProcess(businessProcess);

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .request(CallbackRequest.builder().eventId("CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_CLAIMANT_HMC").build())
            .build();

        handler.handle(callbackParams);
        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_CP_HEARING_SCHEDULED_CLAIMANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );
        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_CP_HEARING_FEE_REQUIRED_CLAIMANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );
        verify(hearingFeesService).getFeeForHearingSmallClaims(new BigDecimal(100).setScale(2, RoundingMode.UNNECESSARY));
    }

    @Test
    void shouldNotCreateDashboardNotificationsForHearingFeeIfCaseInHRAndListing_butApplicantLR() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setLegacyCaseReference("reference");
        caseData.setCcdCaseReference(1234L);
        caseData.setCcdState(HEARING_READINESS);
        caseData.setListingOrRelisting(LISTING);
        caseData.setApplicant1Represented(YesOrNo.YES);

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .request(CallbackRequest.builder().eventId("CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_CLAIMANT").build())
            .build();

        handler.handle(callbackParams);
        verifyNoInteractions(dashboardScenariosService);
    }

    @ParameterizedTest
    @MethodSource("provideTestCases")
    void shouldNotCreateDashboardNotificationsForHearingFee(CaseState ccdState, ListingOrRelisting listingOrRelisting,
                                                            YesOrNo applicant1Represented, YesOrNo respondent1Represented,
                                                            PaymentDetails hearingFeePaymentDetails, FeePaymentOutcomeDetails feePaymentOutcomeDetails) {
        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setLegacyCaseReference("reference");
        caseData.setCcdCaseReference(1234L);
        caseData.setCcdState(ccdState);
        caseData.setListingOrRelisting(listingOrRelisting);
        caseData.setApplicant1Represented(applicant1Represented);
        caseData.setRespondent1Represented(respondent1Represented);
        caseData.setHearingFeePaymentDetails(hearingFeePaymentDetails);
        caseData.setHearingHelpFeesReferenceNumber("123");
        caseData.setResponseClaimTrack("FAST_CLAIM");
        caseData.setFeePaymentOutcomeDetails(feePaymentOutcomeDetails);

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .request(CallbackRequest.builder().eventId("CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_CLAIMANT").build())
            .build();

        handler.handle(callbackParams);
        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_CP_HEARING_SCHEDULED_CLAIMANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );
        verify(dashboardScenariosService, never()).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_CP_HEARING_FEE_REQUIRED_CLAIMANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );
        recordScenarioForTrialArrangementsAndDocumentsUpload(caseData);
    }

    private static Stream<Arguments> provideTestCases() {
        PaymentDetails paymentDetails = new PaymentDetails();
        paymentDetails.setStatus(PaymentStatus.SUCCESS);
        FeePaymentOutcomeDetails feePaymentOutcomeDetails  = new FeePaymentOutcomeDetails();
        feePaymentOutcomeDetails.setHwfFullRemissionGrantedForHearingFee(YesOrNo.YES);
        return Stream.of(
            Arguments.of(CaseState.HEARING_READINESS, ListingOrRelisting.LISTING, YesOrNo.NO, YesOrNo.NO,
                         paymentDetails, null),
            Arguments.of(CaseState.HEARING_READINESS, ListingOrRelisting.LISTING, YesOrNo.NO, YesOrNo.NO,
                         null, feePaymentOutcomeDetails)
        );
    }
}
