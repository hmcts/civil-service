package uk.gov.hmcts.reform.civil.service.dashboardnotifications.hearingscheduled;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.hearingnotice.HearingNoticeCamundaService;
import uk.gov.hmcts.reform.civil.service.hearingnotice.HearingNoticeVariables;
import uk.gov.hmcts.reform.civil.service.hearings.HearingFeesService;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.utils.CourtLocationUtils;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_HEARING_DOCUMENTS_UPLOAD_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_HEARING_FEE_REQUIRED_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_HEARING_SCHEDULED_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_TRIAL_ARRANGEMENTS_RELIST_HEARING_CLAIMANT;

@ExtendWith(MockitoExtension.class)
class HearingScheduledClaimantHmcDashboardServiceTest {

    @Mock
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private DashboardNotificationsParamsMapper mapper;
    @Mock
    private LocationReferenceDataService locationRefDataService;
    @Mock
    private CourtLocationUtils courtLocationUtils;
    @Mock
    private HearingNoticeCamundaService camundaService;
    @Mock
    private HearingFeesService hearingFeesService;

    @InjectMocks
    private HearingScheduledClaimantHmcDashboardService service;

    private static final String AUTH_TOKEN = "Bearer token";
    private static final String CASE_ID = "12345678";

    @Test
    void shouldPopulateCourtName_whenNotifyHearingScheduledIsCalled() {
        DynamicList hearingLocation = DynamicList.builder()
            .value(DynamicListElement.builder()
                       .label("Court Name - Address - Postcode")
                       .build())
            .build();
        CaseData caseData = new CaseDataBuilder().atStateClaimSubmitted().build().toBuilder()
            .hearingLocation(hearingLocation)
            .build();
        LocationRefData locationRefData = LocationRefData.builder()
            .siteName("Court Name")
            .courtAddress("Address")
            .postcode("Postcode")
            .build();
        List<LocationRefData> locations = List.of(locationRefData);
        when(locationRefDataService.getHearingCourtLocations(AUTH_TOKEN)).thenReturn(locations);
        when(courtLocationUtils.fillPreferredLocationData(locations, hearingLocation)).thenReturn(locationRefData);

        service.notifyHearingScheduled(caseData, AUTH_TOKEN);

        assertThat(caseData.getHearingLocationCourtName()).isEqualTo("Court Name");
    }

    @Test
    void shouldRecordFeeRequiredScenario_whenHmcHearingAndFeeNotPaid() {
        String processInstanceId = "proc-id";

        when(mapper.mapCaseDataToParams(any())).thenReturn(new HashMap<>());
        when(camundaService.getProcessVariables(processInstanceId)).thenReturn(HearingNoticeVariables.builder()
                                                                                   .hearingType("ABA5-TRI") // Trial
                                                                                   .build());
        Fee expectedFee = Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(10000)).build();
        when(hearingFeesService.getFeeForHearingSmallClaims(any())).thenReturn(expectedFee);

        CaseData caseData = new CaseDataBuilder().atStateClaimSubmitted()
            .ccdCaseReference(Long.valueOf(CASE_ID))
            .applicant1Represented(uk.gov.hmcts.reform.civil.enums.YesOrNo.NO)
            .build().toBuilder()
            .hearingFeePaymentDetails(PaymentDetails.builder().status(null).build())
            .businessProcess(BusinessProcess.builder().processInstanceId(processInstanceId).build())
            .allocatedTrack(AllocatedTrack.SMALL_CLAIM)
            .build();

        service.notifyHearingScheduled(caseData, AUTH_TOKEN);

        assertThat(caseData.getHearingFee()).isEqualTo(expectedFee);
        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_CP_HEARING_FEE_REQUIRED_CLAIMANT.getScenario(),
            CASE_ID,
            ScenarioRequestParams.builder().params(new HashMap<>()).build()
        );
        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_CP_HEARING_SCHEDULED_CLAIMANT.getScenario(),
            CASE_ID,
            ScenarioRequestParams.builder().params(new HashMap<>()).build()
        );
        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_CP_HEARING_DOCUMENTS_UPLOAD_CLAIMANT.getScenario(),
            CASE_ID,
            ScenarioRequestParams.builder().params(new HashMap<>()).build()
        );
    }

    @Test
    void shouldNotRecordScenarios_whenClaimantIsRepresented() {
        CaseData caseData = new CaseDataBuilder().atStateClaimSubmitted()
            .ccdCaseReference(Long.valueOf(CASE_ID))
            .applicant1Represented(uk.gov.hmcts.reform.civil.enums.YesOrNo.YES)
            .build();

        service.notifyHearingScheduled(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService, never()).recordScenarios(
            anyString(),
            anyString(),
            anyString(),
            any()
        );
    }

    @Test
    void shouldNotRecordFeeRequiredScenario_whenFeeAlreadyPaid() {
        CaseData caseData = new CaseDataBuilder().atStateClaimSubmitted()
            .ccdCaseReference(Long.valueOf(CASE_ID))
            .applicant1Represented(uk.gov.hmcts.reform.civil.enums.YesOrNo.NO)
            .build().toBuilder()
            .hearingFeePaymentDetails(PaymentDetails.builder().status(uk.gov.hmcts.reform.civil.enums.PaymentStatus.SUCCESS).build())
            .build();

        service.notifyHearingScheduled(caseData, AUTH_TOKEN);

        verify(camundaService, never()).getProcessVariables(any());
        verify(dashboardScenariosService, never()).recordScenarios(
            anyString(),
            eq(SCENARIO_AAA6_CP_HEARING_FEE_REQUIRED_CLAIMANT.getScenario()),
            anyString(),
            any()
        );
    }

    @Test
    void shouldNotRecordFeeRequiredScenario_whenFeePaid() {
        CaseData caseData = new CaseDataBuilder().atStateClaimSubmitted()
            .ccdCaseReference(Long.valueOf(CASE_ID))
            .applicant1Represented(uk.gov.hmcts.reform.civil.enums.YesOrNo.NO)
            .build().toBuilder()
            .hearingFeePaymentDetails(PaymentDetails.builder().status(uk.gov.hmcts.reform.civil.enums.PaymentStatus.SUCCESS).build())
            .build();
        when(mapper.mapCaseDataToParams(any())).thenReturn(new HashMap<>());

        service.notifyHearingScheduled(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService, never()).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_CP_HEARING_FEE_REQUIRED_CLAIMANT.getScenario(),
            CASE_ID,
            ScenarioRequestParams.builder().params(new HashMap<>()).build()
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"ABA5-DIS", "ABA5-DRH"})
    void shouldNotRecordFeeRequiredScenario_whenHearingTypeExcludesFee(String hearingType) {
        String processInstanceId = "proc-id";
        CaseData caseData = new CaseDataBuilder().atStateClaimSubmitted()
            .ccdCaseReference(Long.valueOf(CASE_ID))
            .applicant1Represented(uk.gov.hmcts.reform.civil.enums.YesOrNo.NO)
            .build().toBuilder()
            .hearingFeePaymentDetails(PaymentDetails.builder().status(null).build())
            .businessProcess(BusinessProcess.builder().processInstanceId(processInstanceId).build())
            .build();
        when(mapper.mapCaseDataToParams(any())).thenReturn(new HashMap<>());

        when(camundaService.getProcessVariables(processInstanceId)).thenReturn(HearingNoticeVariables.builder()
                                                                          .hearingType(hearingType)
                                                                          .build());

        service.notifyHearingScheduled(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService, never()).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_CP_HEARING_FEE_REQUIRED_CLAIMANT.getScenario(),
            CASE_ID,
            ScenarioRequestParams.builder().params(new HashMap<>()).build()
        );
    }

    @Test
    void shouldRecordRelistHearingScenario_whenFastClaimAndTrialReadyIsNull() {
        CaseData caseData = new CaseDataBuilder().atStateClaimSubmitted()
            .ccdCaseReference(Long.valueOf(CASE_ID))
            .applicant1Represented(uk.gov.hmcts.reform.civil.enums.YesOrNo.NO)
            .build().toBuilder()
            .allocatedTrack(AllocatedTrack.FAST_CLAIM)
            .trialReadyApplicant(null)
            .hearingFeePaymentDetails(PaymentDetails.builder().status(uk.gov.hmcts.reform.civil.enums.PaymentStatus.SUCCESS).build())
            .build();
        when(mapper.mapCaseDataToParams(any())).thenReturn(new HashMap<>());

        service.notifyHearingScheduled(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_CP_TRIAL_ARRANGEMENTS_RELIST_HEARING_CLAIMANT.getScenario(),
            CASE_ID,
            ScenarioRequestParams.builder().params(new HashMap<>()).build()
        );
    }

    @Test
    void shouldNotPopulateCourtName_whenLocationRefDataIsNull() {
        CaseData caseData = new CaseDataBuilder().atStateClaimSubmitted().build();
        when(locationRefDataService.getHearingCourtLocations(AUTH_TOKEN)).thenReturn(List.of());
        when(courtLocationUtils.fillPreferredLocationData(any(), any())).thenReturn(null);

        service.notifyHearingScheduled(caseData, AUTH_TOKEN);

        assertThat(caseData.getHearingLocationCourtName()).isNull();
    }
}
