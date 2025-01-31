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
import uk.gov.hmcts.reform.civil.client.DashboardApiClient;
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
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.hearingnotice.HearingNoticeCamundaService;
import uk.gov.hmcts.reform.civil.service.hearingnotice.HearingNoticeVariables;
import uk.gov.hmcts.reform.civil.service.hearings.HearingFeesService;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_CLAIMANT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_CLAIMANT_HMC;
import static uk.gov.hmcts.reform.civil.enums.CaseState.HEARING_READINESS;
import static uk.gov.hmcts.reform.civil.enums.hearing.ListingOrRelisting.LISTING;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_HEARING_DOCUMENTS_UPLOAD_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_HEARING_FEE_REQUIRED_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_HEARING_SCHEDULED_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_TRIAL_ARRANGEMENTS_RELIST_HEARING_CLAIMANT;

@ExtendWith(MockitoExtension.class)
public class HearingScheduledClaimantNotificationHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private HearingScheduledClaimantNotificationHandler handler;

    @Mock
    private DashboardApiClient dashboardApiClient;

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
    void shouldNotCallRecordScenario_whenCaseProgressionIsDisabled() {
        when(featureToggleService.isCaseProgressionEnabled()).thenReturn(false);

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, CaseData.builder().build())
            .build();

        handler.handle(callbackParams);
        verify(dashboardApiClient, never())
            .recordScenario(anyString(), anyString(), anyString(), any(ScenarioRequestParams.class));
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
        when(featureToggleService.isCaseProgressionEnabled()).thenReturn(true);
        when(locationRefDataService.getHearingCourtLocations(any())).thenReturn(locations);

        DynamicListElement location = DynamicListElement.builder().label("Name - Loc - 1").build();
        DynamicList list = DynamicList.builder().value(location).listItems(List.of(location)).build();
        CaseData caseData = CaseData.builder()
            .legacyCaseReference("reference")
            .ccdCaseReference(1234L)
            .applicant1Represented(YesOrNo.NO)
            .responseClaimTrack("FAST_CLAIM")
            .build().toBuilder().hearingLocation(list).build();

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .request(CallbackRequest.builder().eventId("CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_CLAIMANT").build())
            .build();

        handler.handle(callbackParams);
        verify(dashboardApiClient).recordScenario(
            caseData.getCcdCaseReference().toString(),
            SCENARIO_AAA6_CP_HEARING_SCHEDULED_CLAIMANT.getScenario(),
            "BEARER_TOKEN",
            ScenarioRequestParams.builder().params(params).build()
        );
        recordScenarioForTrialArrangementsAndDocumentsUpload(caseData, "BEARER_TOKEN");
        verifyNoMoreInteractions(dashboardApiClient);
    }

    @Test
    void shouldCreateDashboardNotificationsForHearingFeeIfCaseInHRAndListing() {
        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
        when(featureToggleService.isCaseProgressionEnabled()).thenReturn(true);

        CaseData caseData = CaseData.builder()
            .legacyCaseReference("reference")
            .ccdCaseReference(1234L)
            .ccdState(HEARING_READINESS)
            .listingOrRelisting(LISTING)
            .applicant1Represented(YesOrNo.NO)
            .build();

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .request(CallbackRequest.builder().eventId("CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_CLAIMANT").build())
            .build();

        handler.handle(callbackParams);
        verify(dashboardApiClient).recordScenario(
            caseData.getCcdCaseReference().toString(),
            SCENARIO_AAA6_CP_HEARING_SCHEDULED_CLAIMANT.getScenario(),
            "BEARER_TOKEN",
            ScenarioRequestParams.builder().params(params).build()
        );
        verify(dashboardApiClient).recordScenario(
            caseData.getCcdCaseReference().toString(),
            SCENARIO_AAA6_CP_HEARING_FEE_REQUIRED_CLAIMANT.getScenario(),
            "BEARER_TOKEN",
            ScenarioRequestParams.builder().params(params).build()
        );
    }

    @Test
    void shouldCreateDashboardNotificationsForHearingFeeIfFeePaymentFailure_HMC() {
        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
        when(featureToggleService.isCaseProgressionEnabled()).thenReturn(true);
        when(hearingNoticeCamundaService.getProcessVariables(any()))
            .thenReturn(HearingNoticeVariables.builder()
                            .hearingId("HER1234")
                            .hearingType("AAA7-TRI")
                            .build());
        when(hearingFeesService.getFeeForHearingSmallClaims(any()))
            .thenReturn(Fee.builder().calculatedAmountInPence(new BigDecimal(10)).build());

        CaseData caseData = CaseData.builder()
            .legacyCaseReference("reference")
            .ccdCaseReference(1234L)
            .totalClaimAmount(new BigDecimal(100))
            .responseClaimTrack("SMALL_CLAIM")
            .applicant1Represented(YesOrNo.NO)
            .hearingFeePaymentDetails(PaymentDetails.builder().status(PaymentStatus.FAILED).build())
            .businessProcess(BusinessProcess.builder().processInstanceId("").build())
            .build();

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .request(CallbackRequest.builder().eventId("CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_CLAIMANT_HMC").build())
            .build();

        handler.handle(callbackParams);
        verify(dashboardApiClient).recordScenario(
            caseData.getCcdCaseReference().toString(),
            SCENARIO_AAA6_CP_HEARING_SCHEDULED_CLAIMANT.getScenario(),
            "BEARER_TOKEN",
            ScenarioRequestParams.builder().params(params).build()
        );
        verify(dashboardApiClient).recordScenario(
            caseData.getCcdCaseReference().toString(),
            SCENARIO_AAA6_CP_HEARING_FEE_REQUIRED_CLAIMANT.getScenario(),
            "BEARER_TOKEN",
            ScenarioRequestParams.builder().params(params).build()
        );
        verify(hearingFeesService).getFeeForHearingSmallClaims(new BigDecimal(100).setScale(2, RoundingMode.UNNECESSARY));
    }

    @Test
    void shouldCreateDashboardNotificationsForHearingFeeIfFeeNeverPaid_HMC() {
        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
        when(featureToggleService.isCaseProgressionEnabled()).thenReturn(true);
        when(hearingNoticeCamundaService.getProcessVariables(any()))
            .thenReturn(HearingNoticeVariables.builder()
                            .hearingId("HER1234")
                            .hearingType("AAA7-TRI")
                            .build());
        when(hearingFeesService.getFeeForHearingSmallClaims(any()))
            .thenReturn(Fee.builder().calculatedAmountInPence(new BigDecimal(10)).build());

        CaseData caseData = CaseData.builder()
            .legacyCaseReference("reference")
            .ccdCaseReference(1234L)
            .totalClaimAmount(new BigDecimal(100))
            .responseClaimTrack("SMALL_CLAIM")
            .applicant1Represented(YesOrNo.NO)
            .businessProcess(BusinessProcess.builder().processInstanceId("").build())
            .build();

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .request(CallbackRequest.builder().eventId("CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_CLAIMANT_HMC").build())
            .build();

        handler.handle(callbackParams);
        verify(dashboardApiClient).recordScenario(
            caseData.getCcdCaseReference().toString(),
            SCENARIO_AAA6_CP_HEARING_SCHEDULED_CLAIMANT.getScenario(),
            "BEARER_TOKEN",
            ScenarioRequestParams.builder().params(params).build()
        );
        verify(dashboardApiClient).recordScenario(
            caseData.getCcdCaseReference().toString(),
            SCENARIO_AAA6_CP_HEARING_FEE_REQUIRED_CLAIMANT.getScenario(),
            "BEARER_TOKEN",
            ScenarioRequestParams.builder().params(params).build()
        );
        verify(hearingFeesService).getFeeForHearingSmallClaims(new BigDecimal(100).setScale(2, RoundingMode.UNNECESSARY));
    }

    private void recordScenarioForTrialArrangementsAndDocumentsUpload(CaseData caseData, String authToken) {
        verify(dashboardApiClient).recordScenario(
            caseData.getCcdCaseReference().toString(),
            SCENARIO_AAA6_CP_TRIAL_ARRANGEMENTS_RELIST_HEARING_CLAIMANT.getScenario(),
            authToken,
            ScenarioRequestParams.builder().params(params).build()
        );
        verify(dashboardApiClient).recordScenario(
            caseData.getCcdCaseReference().toString(),
            SCENARIO_AAA6_CP_HEARING_DOCUMENTS_UPLOAD_CLAIMANT.getScenario(),
            authToken,
            ScenarioRequestParams.builder().params(params).build()
        );
    }

    @Test
    void shouldNotCreateDashboardNotificationsForHearingFeeIfFeePaymentSuccess_HMC() {
        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
        when(featureToggleService.isCaseProgressionEnabled()).thenReturn(true);
        when(hearingNoticeCamundaService.getProcessVariables(any()))
            .thenReturn(HearingNoticeVariables.builder()
                            .hearingId("HER1234")
                            .hearingType("AAA7-TRI")
                            .build());

        CaseData caseData = CaseData.builder()
            .legacyCaseReference("reference")
            .ccdCaseReference(1234L)
            .applicant1Represented(YesOrNo.NO)
            .responseClaimTrack("FAST_CLAIM")
            .hearingFeePaymentDetails(PaymentDetails.builder().status(PaymentStatus.SUCCESS).build())
            .businessProcess(BusinessProcess.builder().processInstanceId("").build())
            .build();

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .request(CallbackRequest.builder().eventId("CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_CLAIMANT_HMC").build())
            .build();

        handler.handle(callbackParams);
        verify(dashboardApiClient).recordScenario(
            caseData.getCcdCaseReference().toString(),
            SCENARIO_AAA6_CP_HEARING_SCHEDULED_CLAIMANT.getScenario(),
            "BEARER_TOKEN",
            ScenarioRequestParams.builder().params(params).build()
        );
        recordScenarioForTrialArrangementsAndDocumentsUpload(caseData, "BEARER_TOKEN");
        verifyNoMoreInteractions(dashboardApiClient);
        verifyNoInteractions(hearingFeesService);
    }

    @Test
    void shouldNotCreateDashboardNotificationsForHearingFeeIfHearingTypeDisposal_HMC() {
        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
        when(featureToggleService.isCaseProgressionEnabled()).thenReturn(true);
        when(hearingNoticeCamundaService.getProcessVariables(any()))
            .thenReturn(HearingNoticeVariables.builder()
                            .hearingId("HER1234")
                            .hearingType("AAA7-DIS")
                            .build());

        CaseData caseData = CaseData.builder()
            .legacyCaseReference("reference")
            .ccdCaseReference(1234L)
            .applicant1Represented(YesOrNo.NO)
            .responseClaimTrack("FAST_CLAIM")
            .businessProcess(BusinessProcess.builder().processInstanceId("").build())
            .build();

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .request(CallbackRequest.builder().eventId("CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_CLAIMANT_HMC").build())
            .build();

        handler.handle(callbackParams);
        verify(dashboardApiClient).recordScenario(
            caseData.getCcdCaseReference().toString(),
            SCENARIO_AAA6_CP_HEARING_SCHEDULED_CLAIMANT.getScenario(),
            "BEARER_TOKEN",
            ScenarioRequestParams.builder().params(params).build()
        );
        recordScenarioForTrialArrangementsAndDocumentsUpload(caseData, "BEARER_TOKEN");
        verifyNoMoreInteractions(dashboardApiClient);
        verifyNoInteractions(hearingFeesService);
    }

    @Test
    void shouldNotCreateDashboardNotificationsForHearingFeeIfPaidWithHwF_HMC() {
        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
        when(featureToggleService.isCaseProgressionEnabled()).thenReturn(true);
        when(hearingNoticeCamundaService.getProcessVariables(any()))
            .thenReturn(HearingNoticeVariables.builder()
                            .hearingId("HER1234")
                            .hearingType("AAA7-TRI")
                            .build());

        CaseData caseData = CaseData.builder()
            .legacyCaseReference("reference")
            .ccdCaseReference(1234L)
            .respondent1Represented(YesOrNo.NO)
            .applicant1Represented(YesOrNo.NO)
            .responseClaimTrack("FAST_CLAIM")
            .hearingHelpFeesReferenceNumber("123")
            .feePaymentOutcomeDetails(FeePaymentOutcomeDetails.builder().hwfFullRemissionGrantedForHearingFee(YesOrNo.YES).build())
            .businessProcess(BusinessProcess.builder().processInstanceId("").build())
            .build();

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .request(CallbackRequest.builder().eventId("CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_CLAIMANT_HMC").build())
            .build();

        handler.handle(callbackParams);
        verify(dashboardApiClient).recordScenario(
            caseData.getCcdCaseReference().toString(),
            SCENARIO_AAA6_CP_HEARING_SCHEDULED_CLAIMANT.getScenario(),
            "BEARER_TOKEN",
            ScenarioRequestParams.builder().params(params).build()
        );
        recordScenarioForTrialArrangementsAndDocumentsUpload(caseData, "BEARER_TOKEN");
        verifyNoMoreInteractions(dashboardApiClient);
        verifyNoInteractions(hearingFeesService);
    }

    @Test
    void shouldCreateDashboardNotificationsForHearingFeeIfNotPaidWithHwF_HMC() {
        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
        when(featureToggleService.isCaseProgressionEnabled()).thenReturn(true);
        when(hearingNoticeCamundaService.getProcessVariables(any()))
            .thenReturn(HearingNoticeVariables.builder()
                            .hearingId("HER1234")
                            .hearingType("AAA7-TRI")
                            .build());

        CaseData caseData = CaseData.builder()
            .legacyCaseReference("reference")
            .ccdCaseReference(1234L)
            .respondent1Represented(YesOrNo.NO)
            .applicant1Represented(YesOrNo.NO)
            .totalClaimAmount(new BigDecimal(100))
            .responseClaimTrack("SMALL_CLAIM")
            .hearingHelpFeesReferenceNumber("123")
            .businessProcess(BusinessProcess.builder().processInstanceId("").build())
            .build();

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .request(CallbackRequest.builder().eventId("CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_CLAIMANT_HMC").build())
            .build();

        handler.handle(callbackParams);
        verify(dashboardApiClient).recordScenario(
            caseData.getCcdCaseReference().toString(),
            SCENARIO_AAA6_CP_HEARING_SCHEDULED_CLAIMANT.getScenario(),
            "BEARER_TOKEN",
            ScenarioRequestParams.builder().params(params).build()
        );
        verify(dashboardApiClient).recordScenario(
            caseData.getCcdCaseReference().toString(),
            SCENARIO_AAA6_CP_HEARING_FEE_REQUIRED_CLAIMANT.getScenario(),
            "BEARER_TOKEN",
            ScenarioRequestParams.builder().params(params).build()
        );
        verify(hearingFeesService).getFeeForHearingSmallClaims(new BigDecimal(100).setScale(2, RoundingMode.UNNECESSARY));
    }

    @Test
    void shouldNotCreateDashboardNotificationsForHearingFeeIfCaseInHRAndListing_butApplicantLR() {
        when(featureToggleService.isCaseProgressionEnabled()).thenReturn(true);

        CaseData caseData = CaseData.builder()
            .legacyCaseReference("reference")
            .ccdCaseReference(1234L)
            .ccdState(HEARING_READINESS)
            .listingOrRelisting(LISTING)
            .applicant1Represented(YesOrNo.YES)
            .build();

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .request(CallbackRequest.builder().eventId("CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_CLAIMANT").build())
            .build();

        handler.handle(callbackParams);
        verifyNoInteractions(dashboardApiClient);
    }

    @ParameterizedTest
    @MethodSource("provideTestCases")
    void shouldNotCreateDashboardNotificationsForHearingFee(CaseState ccdState, ListingOrRelisting listingOrRelisting,
                                                            YesOrNo applicant1Represented, YesOrNo respondent1Represented,
                                                            PaymentDetails hearingFeePaymentDetails, FeePaymentOutcomeDetails feePaymentOutcomeDetails) {
        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
        when(featureToggleService.isCaseProgressionEnabled()).thenReturn(true);

        CaseData caseData = CaseData.builder()
            .legacyCaseReference("reference")
            .ccdCaseReference(1234L)
            .ccdState(ccdState)
            .listingOrRelisting(listingOrRelisting)
            .applicant1Represented(applicant1Represented)
            .respondent1Represented(respondent1Represented)
            .hearingFeePaymentDetails(hearingFeePaymentDetails)
            .hearingHelpFeesReferenceNumber("123")
            .responseClaimTrack("FAST_CLAIM")
            .feePaymentOutcomeDetails(feePaymentOutcomeDetails)
            .build();

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .request(CallbackRequest.builder().eventId("CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_CLAIMANT").build())
            .build();

        handler.handle(callbackParams);
        verify(dashboardApiClient).recordScenario(
            caseData.getCcdCaseReference().toString(),
            SCENARIO_AAA6_CP_HEARING_SCHEDULED_CLAIMANT.getScenario(),
            "BEARER_TOKEN",
            ScenarioRequestParams.builder().params(params).build()
        );
        verify(dashboardApiClient, never()).recordScenario(
            caseData.getCcdCaseReference().toString(),
            SCENARIO_AAA6_CP_HEARING_FEE_REQUIRED_CLAIMANT.getScenario(),
            "BEARER_TOKEN",
            ScenarioRequestParams.builder().params(params).build()
        );
        recordScenarioForTrialArrangementsAndDocumentsUpload(caseData, "BEARER_TOKEN");
    }

    private static Stream<Arguments> provideTestCases() {
        return Stream.of(
            Arguments.of(CaseState.HEARING_READINESS, ListingOrRelisting.LISTING, YesOrNo.NO, YesOrNo.NO,
                         PaymentDetails.builder().status(PaymentStatus.SUCCESS).build(), null),
            Arguments.of(CaseState.HEARING_READINESS, ListingOrRelisting.LISTING, YesOrNo.NO, YesOrNo.NO,
                         null, FeePaymentOutcomeDetails.builder().hwfFullRemissionGrantedForHearingFee(YesOrNo.YES).build())
        );
    }
}

