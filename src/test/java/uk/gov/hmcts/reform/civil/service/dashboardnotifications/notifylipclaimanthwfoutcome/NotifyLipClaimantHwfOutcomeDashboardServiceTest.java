package uk.gov.hmcts.reform.civil.service.dashboardnotifications.notifylipclaimanthwfoutcome;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIM_ISSUE_HWF_FULL_REMISSION;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_HEARING_FEE_HWF_NO_REMISSION;

@ExtendWith(MockitoExtension.class)
class NotifyLipClaimantHwfOutcomeDashboardServiceTest {

    private static final String AUTH_TOKEN = "Bearer token";
    private static final Long CASE_REFERENCE = 123456789L;

    private NotifyLipClaimantHwfOutcomeDashboardService service;

    private DashboardScenariosService dashboardScenariosService;
    private DashboardNotificationsParamsMapper mapper;

    @BeforeEach
    void setUp() {
        dashboardScenariosService = mock(DashboardScenariosService.class);
        mapper = mock(DashboardNotificationsParamsMapper.class);
        service = new NotifyLipClaimantHwfOutcomeDashboardService(dashboardScenariosService, mapper);
    }

    @Test
    void shouldRecordClaimIssueScenarioWhenApplicantIsLip() {
        CaseData caseData = mockCaseData(CaseEvent.FULL_REMISSION_HWF, true, false, true);
        HashMap<String, Object> params = new HashMap<>();
        params.put("someKey", "someValue");
        when(mapper.mapCaseDataToParams(caseData)).thenReturn(params);

        service.notifyNotifyLipClaimantHwfOutcome(caseData, AUTH_TOKEN);

        ArgumentCaptor<ScenarioRequestParams> captor = ArgumentCaptor.forClass(ScenarioRequestParams.class);
        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIM_ISSUE_HWF_FULL_REMISSION.getScenario()),
            eq(CASE_REFERENCE.toString()),
            captor.capture()
        );
        assertThat(captor.getValue().getParams()).isEqualTo(params);
    }

    @Test
    void shouldRecordHearingFeeScenarioWhenApplicantIsLip() {
        CaseData caseData = mockCaseData(CaseEvent.NO_REMISSION_HWF, false, true, true);
        HashMap<String, Object> params = new HashMap<>();
        params.put("feeType", "hearing");
        when(mapper.mapCaseDataToParams(caseData)).thenReturn(params);

        service.notifyNotifyLipClaimantHwfOutcome(caseData, AUTH_TOKEN);

        ArgumentCaptor<ScenarioRequestParams> captor = ArgumentCaptor.forClass(ScenarioRequestParams.class);
        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_HEARING_FEE_HWF_NO_REMISSION.getScenario()),
            eq(CASE_REFERENCE.toString()),
            captor.capture()
        );
        assertThat(captor.getValue().getParams()).isEqualTo(params);
    }

    @Test
    void shouldNotRecordScenarioWhenApplicantRepresented() {
        CaseData caseData = mockCaseData(CaseEvent.INVALID_HWF_REFERENCE, true, false, false);
        when(mapper.mapCaseDataToParams(caseData)).thenReturn(new HashMap<>());

        service.notifyNotifyLipClaimantHwfOutcome(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }

    @Test
    void shouldNotRecordScenarioWhenEventMissing() {
        CaseData caseData = mockCaseData(null, true, false, true);
        when(mapper.mapCaseDataToParams(caseData)).thenReturn(new HashMap<>());

        service.notifyNotifyLipClaimantHwfOutcome(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }

    private CaseData mockCaseData(CaseEvent hwfEvent,
                                  boolean claimIssued,
                                  boolean hearingFee,
                                  boolean applicantNotRepresented) {
        CaseData caseData = mock(CaseData.class);
        lenient().when(caseData.getHwFEvent()).thenReturn(hwfEvent);
        lenient().when(caseData.isHWFTypeClaimIssued()).thenReturn(claimIssued);
        lenient().when(caseData.isHWFTypeHearing()).thenReturn(hearingFee);
        lenient().when(caseData.isApplicantNotRepresented()).thenReturn(applicantNotRepresented);
        lenient().when(caseData.getCcdCaseReference()).thenReturn(CASE_REFERENCE);
        return caseData;
    }
}
