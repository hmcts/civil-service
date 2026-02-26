package uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.hwf;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.ga.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.HelpWithFeesDetails;
import uk.gov.hmcts.reform.civil.ga.service.GaDashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

import java.util.HashMap;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INVALID_HWF_REFERENCE_GA;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MORE_INFORMATION_HWF_GA;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NO_REMISSION_HWF_GA;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.PARTIAL_REMISSION_HWF_GA;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPS_HWF_INVALID_REFERENCE_APPLICANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPS_HWF_MORE_INFORMATION_APPLICANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPS_HWF_PARTIAL_REMISSION_APPLICANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPS_HWF_REJECTED_APPLICANT;

@ExtendWith(MockitoExtension.class)
class HwfOutcomeApplicantDashboardServiceTest {

    private static final String AUTH_TOKEN = "auth-token";

    @Mock
    private DashboardApiClient dashboardApiClient;

    @Mock
    private GaDashboardNotificationsParamsMapper mapper;

    @InjectMocks
    private HwfOutcomeApplicantDashboardService service;

    @Test
    void shouldRecordScenarioForApplicationFeeNoRemission() {
        GeneralApplicationCaseData caseData = buildCase(FeeType.APPLICATION, NO_REMISSION_HWF_GA);
        assertScenarioRecorded(caseData, SCENARIO_AAA6_GENERAL_APPS_HWF_REJECTED_APPLICANT.getScenario());
    }

    @Test
    void shouldRecordScenarioForAdditionalFeeNoRemission() {
        GeneralApplicationCaseData caseData = buildCase(FeeType.ADDITIONAL, NO_REMISSION_HWF_GA);
        assertScenarioRecorded(caseData, SCENARIO_AAA6_GENERAL_APPS_HWF_REJECTED_APPLICANT.getScenario());
    }

    @Test
    void shouldRecordScenarioForPartialRemission() {
        GeneralApplicationCaseData caseData = buildCase(FeeType.APPLICATION, PARTIAL_REMISSION_HWF_GA);
        assertScenarioRecorded(caseData, SCENARIO_AAA6_GENERAL_APPS_HWF_PARTIAL_REMISSION_APPLICANT.getScenario());
    }

    @Test
    void shouldRecordScenarioForMoreInformation() {
        GeneralApplicationCaseData caseData = buildCase(FeeType.ADDITIONAL, MORE_INFORMATION_HWF_GA);
        assertScenarioRecorded(caseData, SCENARIO_AAA6_GENERAL_APPS_HWF_MORE_INFORMATION_APPLICANT.getScenario());
    }

    @Test
    void shouldRecordScenarioForInvalidReferenceApplicationFee() {
        GeneralApplicationCaseData caseData = buildCase(FeeType.APPLICATION, INVALID_HWF_REFERENCE_GA);
        assertScenarioRecorded(caseData, SCENARIO_AAA6_GENERAL_APPS_HWF_INVALID_REFERENCE_APPLICANT.getScenario());
    }

    @Test
    void shouldRecordScenarioForInvalidReferenceAdditionalFee() {
        GeneralApplicationCaseData caseData = buildCase(FeeType.ADDITIONAL, INVALID_HWF_REFERENCE_GA);
        assertScenarioRecorded(caseData, SCENARIO_AAA6_GENERAL_APPS_HWF_INVALID_REFERENCE_APPLICANT.getScenario());
    }

    private GeneralApplicationCaseData buildCase(FeeType feeType, CaseEvent hwfCaseEvent) {

        HelpWithFeesDetails hwfDetails = new HelpWithFeesDetails().setHwfCaseEvent(hwfCaseEvent);

        return GeneralApplicationCaseDataBuilder.builder()
            .atStateClaimDraft()
            .withNoticeCaseData()
            .hwfFeeType(feeType)
            .gaHwfDetails(feeType == FeeType.APPLICATION ? hwfDetails : null)
            .additionalHwfDetails(feeType == FeeType.ADDITIONAL ? hwfDetails : null)
            .build();
    }

    private void assertScenarioRecorded(GeneralApplicationCaseData caseData, String expectedScenario) {
        HashMap<String, Object> params = new HashMap<>();
        when(mapper.mapCaseDataToParams(caseData)).thenReturn(params);

        service.notifyHwfOutcome(caseData, AUTH_TOKEN);

        verify(dashboardApiClient).recordScenario(
            caseData.getCcdCaseReference().toString(),
            expectedScenario,
            AUTH_TOKEN,
            ScenarioRequestParams.builder().params(params).build()
        );
    }
}
