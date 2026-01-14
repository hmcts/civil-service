package uk.gov.hmcts.reform.civil.service.dashboardnotifications.hearingfeeunpaid;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.enums.sdo.ClaimsTrack;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_HEARING_FEE_UNPAID_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_HEARING_FEE_UNPAID_TRIAL_READY_CLAIMANT;

@ExtendWith(MockitoExtension.class)
class HearingFeeUnpaidClaimantNotificationServiceTest {

    private static final String AUTH_TOKEN = "BEARER";
    public static final String CLAIMANT = "CLAIMANT";
    public static final String CCD_REF = "1234";

    @Mock
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private DashboardNotificationService dashboardNotificationService;
    @Mock
    private DashboardNotificationsParamsMapper mapper;

    @InjectMocks
    private HearingFeeUnpaidClaimantNotificationService service;

    @BeforeEach
    void setup() {
        when(mapper.mapCaseDataToParams(any())).thenReturn(new HashMap<>());
    }

    @Test
    void shouldRecordFastTrackScenarioWhenTrialReadyApplicantIsNull() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setCcdCaseReference(1234L);
        caseData.setDrawDirectionsOrderRequired(YesOrNo.NO);
        caseData.setClaimsTrack(ClaimsTrack.fastTrack);
        caseData.setTrialReadyApplicant(null);

        service.notifyHearingFeeUnpaid(caseData, AUTH_TOKEN);

        verify(dashboardNotificationService).deleteByReferenceAndCitizenRole(CCD_REF, CLAIMANT);
        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_HEARING_FEE_UNPAID_CLAIMANT.getScenario(),
            CCD_REF,
            ScenarioRequestParams.builder().params(new HashMap<>()).build()
        );
    }

    @Test
    void shouldRecordTrialReadyScenarioWhenTrialReadyApplicantIsNotNull() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setCcdCaseReference(1234L);
        caseData.setDrawDirectionsOrderRequired(YesOrNo.NO);
        caseData.setClaimsTrack(ClaimsTrack.fastTrack);
        caseData.setTrialReadyApplicant(YesOrNo.YES);

        service.notifyHearingFeeUnpaid(caseData, AUTH_TOKEN);

        verify(dashboardNotificationService).deleteByReferenceAndCitizenRole(CCD_REF, CLAIMANT);
        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_HEARING_FEE_UNPAID_TRIAL_READY_CLAIMANT.getScenario(),
            CCD_REF,
            ScenarioRequestParams.builder().params(new HashMap<>()).build()
        );
    }

    @Test
    void shouldRecordTrialReadyScenarioWhenNotFastTrack() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setDrawDirectionsOrderRequired(YesOrNo.YES);
        caseData.setDrawDirectionsOrderSmallClaims(YesOrNo.NO);
        caseData.setTrialReadyApplicant(null);
        caseData.setCcdCaseReference(1234L);

        service.notifyHearingFeeUnpaid(caseData, AUTH_TOKEN);

        verify(dashboardNotificationService).deleteByReferenceAndCitizenRole(CCD_REF, CLAIMANT);
        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_HEARING_FEE_UNPAID_TRIAL_READY_CLAIMANT.getScenario(),
            CCD_REF,
            ScenarioRequestParams.builder().params(new HashMap<>()).build()
        );
    }

    @Test
    void shouldNotRecordScenarioWhenApplicantRepresented() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setApplicant1Represented(YesOrNo.YES);
        caseData.setCcdCaseReference(1234L);
        caseData.setDrawDirectionsOrderRequired(YesOrNo.NO);
        caseData.setClaimsTrack(ClaimsTrack.fastTrack);
        caseData.setTrialReadyApplicant(null);

        service.notifyHearingFeeUnpaid(caseData, AUTH_TOKEN);

        verify(dashboardNotificationService, never()).deleteByReferenceAndCitizenRole(any(), any());
        verify(dashboardScenariosService, never()).recordScenarios(any(), any(), any(), any());
    }
}


