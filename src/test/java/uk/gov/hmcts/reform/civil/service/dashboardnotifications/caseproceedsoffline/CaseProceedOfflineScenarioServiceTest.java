package uk.gov.hmcts.reform.civil.service.dashboardnotifications.caseproceedsoffline;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.CaseData;

import static org.assertj.core.api.Assertions.assertThat;

class CaseProceedOfflineScenarioServiceTest {

    private static final class ScenarioService extends CaseProceedOfflineScenarioService {

        ScenarioService() {
            super(null);
        }

        public boolean isInCaseProgression(CaseData caseData) {
            return inCaseProgressionState(caseData);
        }
    }

    private final ScenarioService scenarioService = new ScenarioService();

    @Test
    void shouldReturnTrueWhenPreviousStateIsCaseProgressionGroup() {
        CaseData caseData = CaseData.builder()
            .previousCCDState(CaseState.CASE_PROGRESSION)
            .build();

        assertThat(scenarioService.isInCaseProgression(caseData)).isTrue();
    }

    @Test
    void shouldReturnFalseWhenNoPreviousState() {
        CaseData caseData = CaseData.builder().build();

        assertThat(scenarioService.isInCaseProgression(caseData)).isFalse();
    }

    @Test
    void shouldReturnFalseWhenPreviousStateNotInAllowedList() {
        CaseData caseData = CaseData.builder()
            .previousCCDState(CaseState.CASE_SETTLED)
            .build();

        assertThat(scenarioService.isInCaseProgression(caseData)).isFalse();
    }
}
