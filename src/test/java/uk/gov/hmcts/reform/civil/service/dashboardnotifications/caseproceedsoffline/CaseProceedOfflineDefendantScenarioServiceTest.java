package uk.gov.hmcts.reform.civil.service.dashboardnotifications.caseproceedsoffline;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CASE_PROCEED_IN_CASE_MAN_DEFENDANT_WITHOUT_TASK_CHANGES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_AVAILABLE_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_INITIATE_APPLICATION_INACTIVE_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_UPDATE_CASE_PROCEED_IN_CASE_MAN_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_UPDATE_CASE_PROCEED_IN_CASE_MAN_DEFENDANT_FAST_TRACK;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
class CaseProceedOfflineDefendantScenarioServiceTest {

    @Mock
    private FeatureToggleService toggleService;

    private CaseProceedOfflineDefendantScenarioService service;

    @BeforeEach
    void setup() {
        service = new CaseProceedOfflineDefendantScenarioService(toggleService);
    }

    @Test
    void shouldResolvePrimaryScenarioForFastTrackJudgment() {
        CaseData fastTrack = CaseData.builder()
            .ccdCaseReference(1L)
            .activeJudgment(JudgmentDetails.builder().build())
            .responseClaimTrack(AllocatedTrack.FAST_CLAIM.name())
            .build();

        assertThat(service.resolvePrimaryScenario(fastTrack))
            .isEqualTo(SCENARIO_AAA6_UPDATE_CASE_PROCEED_IN_CASE_MAN_DEFENDANT_FAST_TRACK.getScenario());

        CaseData judgment = fastTrack.toBuilder().responseClaimTrack(null).build();
        assertThat(service.resolvePrimaryScenario(judgment))
            .isEqualTo(SCENARIO_AAA6_UPDATE_CASE_PROCEED_IN_CASE_MAN_DEFENDANT.getScenario());

        CaseData noJudgment = CaseData.builder().build();
        assertThat(service.resolvePrimaryScenario(noJudgment))
            .isEqualTo(SCENARIO_AAA6_CASE_PROCEED_IN_CASE_MAN_DEFENDANT_WITHOUT_TASK_CHANGES.getScenario());
    }

    @Test
    void shouldResolveAdditionalScenarios() {
        CaseData caseData = CaseData.builder()
            .generalApplications(wrapElements(GeneralApplication.builder().build()))
            .build();

        Map<String, Boolean> scenarios = service.resolveAdditionalScenarios(caseData);

        assertThat(scenarios)
            .containsEntry(SCENARIO_AAA6_GENERAL_APPLICATION_INITIATE_APPLICATION_INACTIVE_DEFENDANT.getScenario(), true)
            .containsEntry(SCENARIO_AAA6_GENERAL_APPLICATION_AVAILABLE_DEFENDANT.getScenario(), true);
    }

    @Test
    void shouldRecordScenarioInCaseProgressionOnlyWhenEligible() {
        CaseData eligible = CaseData.builder()
            .ccdCaseReference(1L)
            .respondent1Represented(YesOrNo.NO)
            .applicant1Represented(YesOrNo.NO)
            .previousCCDState(CaseState.CASE_PROGRESSION)
            .build();

        CaseData ineligible = eligible.toBuilder()
            .respondent1Represented(YesOrNo.YES)
            .build();

        assertThat(service.shouldRecordScenarioInCaseProgression(eligible)).isTrue();
        assertThat(service.shouldRecordScenarioInCaseProgression(ineligible)).isFalse();
    }
}
