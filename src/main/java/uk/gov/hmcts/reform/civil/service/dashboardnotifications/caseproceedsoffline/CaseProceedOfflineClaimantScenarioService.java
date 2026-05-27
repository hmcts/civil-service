package uk.gov.hmcts.reform.civil.service.dashboardnotifications.caseproceedsoffline;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CASE_PROCEEDS_OFFLINE_JUDGMENT_REQUESTED_CANCELLED_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CASE_PROCEED_IN_CASE_MAN_CLAIMANT_WITHOUT_TASK_CHANGES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_AVAILABLE_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_INITIATE_APPLICATION_INACTIVE_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_LIP_QM_CASE_OFFLINE_OPEN_QUERIES_CLAIMANT;

@Service
public class CaseProceedOfflineClaimantScenarioService extends CaseProceedOfflinePartyScenarioService {

    public CaseProceedOfflineClaimantScenarioService(FeatureToggleService featureToggleService) {
        super(featureToggleService);
    }

    public String resolvePrimaryScenario(CaseData caseData) {
        return SCENARIO_AAA6_CASE_PROCEED_IN_CASE_MAN_CLAIMANT_WITHOUT_TASK_CHANGES.getScenario();
    }

    public boolean shouldRecordScenarioInCaseProgression(CaseData caseData) {
        return inCaseProgressionState(caseData)
            && (caseData.isLipvLipOneVOne() || caseData.isLipvLROneVOne());
    }

    @Override
    public Map<String, Boolean> resolveAdditionalScenarios(CaseData caseData) {
        Map<String, Boolean> scenarios = new HashMap<>(super.resolveAdditionalScenarios(caseData));
        scenarios.put(
            SCENARIO_AAA6_CASE_PROCEEDS_OFFLINE_JUDGMENT_REQUESTED_CANCELLED_CLAIMANT.getScenario(),
            isJudgmentRequestedLipvLip(caseData)
        );
        return scenarios;
    }

    private boolean isJudgmentRequestedLipvLip(CaseData caseData) {
        return CaseState.JUDGMENT_REQUESTED.equals(caseData.getPreviousCCDState())
            && caseData.isLipvLipOneVOne();
    }

    @Override
    protected String inactiveScenarioId() {
        return SCENARIO_AAA6_GENERAL_APPLICATION_INITIATE_APPLICATION_INACTIVE_CLAIMANT.getScenario();
    }

    @Override
    protected String availableScenarioId() {
        return SCENARIO_AAA6_GENERAL_APPLICATION_AVAILABLE_CLAIMANT.getScenario();
    }

    @Override
    protected String queryScenarioId() {
        return SCENARIO_AAA6_LIP_QM_CASE_OFFLINE_OPEN_QUERIES_CLAIMANT.getScenario();
    }

}
