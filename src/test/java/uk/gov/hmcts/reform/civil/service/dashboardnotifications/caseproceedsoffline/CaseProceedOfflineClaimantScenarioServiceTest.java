package uk.gov.hmcts.reform.civil.service.dashboardnotifications.caseproceedsoffline;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CASE_PROCEED_IN_CASE_MAN_CLAIMANT_WITHOUT_TASK_CHANGES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_AVAILABLE_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_INITIATE_APPLICATION_INACTIVE_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_LIP_QM_CASE_OFFLINE_OPEN_QUERIES_CLAIMANT;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
class CaseProceedOfflineClaimantScenarioServiceTest {

    @Mock
    private FeatureToggleService toggleService;

    private CaseProceedOfflineClaimantScenarioService service;

    @BeforeEach
    void setup() {
        service = new CaseProceedOfflineClaimantScenarioService(toggleService);
    }

    @Test
    void shouldResolvePrimaryScenario() {
        CaseData caseData = CaseData.builder().build();

        assertThat(service.resolvePrimaryScenario(caseData))
            .isEqualTo(SCENARIO_AAA6_CASE_PROCEED_IN_CASE_MAN_CLAIMANT_WITHOUT_TASK_CHANGES.getScenario());
    }

    @Test
    void shouldResolveAdditionalScenarios() {
        List<Element<GeneralApplication>> generalApplications = wrapElements(GeneralApplication.builder().build());
        CaseData caseData = CaseData.builder()
            .generalApplications(generalApplications)
            .build();

        Map<String, Boolean> scenarios = service.resolveAdditionalScenarios(caseData);

        assertThat(scenarios)
            .containsEntry(SCENARIO_AAA6_GENERAL_APPLICATION_INITIATE_APPLICATION_INACTIVE_CLAIMANT.getScenario(), true)
            .containsEntry(SCENARIO_AAA6_GENERAL_APPLICATION_AVAILABLE_CLAIMANT.getScenario(), true)
            .containsEntry(SCENARIO_AAA6_LIP_QM_CASE_OFFLINE_OPEN_QUERIES_CLAIMANT.getScenario(), false);
    }

    @Test
    void shouldRecordScenarioInCaseProgressionOnlyForLip() {
        CaseData lipCase = CaseData.builder()
            .applicant1Represented(YesOrNo.NO)
            .respondent1Represented(YesOrNo.NO)
            .ccdCaseReference(1L)
            .previousCCDState(CaseState.CASE_PROGRESSION)
            .build();

        CaseData nonLipCase = lipCase.toBuilder()
            .applicant1Represented(YesOrNo.YES)
            .build();

        assertThat(service.shouldRecordScenarioInCaseProgression(lipCase)).isTrue();
        assertThat(service.shouldRecordScenarioInCaseProgression(nonLipCase)).isFalse();
    }
}
