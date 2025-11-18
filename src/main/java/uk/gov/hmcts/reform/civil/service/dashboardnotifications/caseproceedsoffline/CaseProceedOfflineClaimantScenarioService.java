package uk.gov.hmcts.reform.civil.service.dashboardnotifications.caseproceedsoffline;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.Map;

import static java.util.Map.entry;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CASE_PROCEED_IN_CASE_MAN_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CASE_PROCEED_IN_CASE_MAN_CLAIMANT_WITHOUT_TASK_CHANGES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_AVAILABLE_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_INITIATE_APPLICATION_INACTIVE_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_LIP_QM_CASE_OFFLINE_OPEN_QUERIES_CLAIMANT;

@Service
public class CaseProceedOfflineClaimantScenarioService extends CaseProceedOfflineScenarioService {

    public CaseProceedOfflineClaimantScenarioService(FeatureToggleService featureToggleService) {
        super(featureToggleService);
    }

    public String resolvePrimaryScenario(CaseData caseData) {
        return SCENARIO_AAA6_CASE_PROCEED_IN_CASE_MAN_CLAIMANT_WITHOUT_TASK_CHANGES.getScenario();
    }

    public Map<String, Boolean> resolveAdditionalScenarios(CaseData caseData) {
        return Map.ofEntries(
            entry(SCENARIO_AAA6_GENERAL_APPLICATION_INITIATE_APPLICATION_INACTIVE_CLAIMANT.getScenario(), true),
            entry(
                SCENARIO_AAA6_GENERAL_APPLICATION_AVAILABLE_CLAIMANT.getScenario(),
                caseData.getGeneralApplications() != null && !caseData.getGeneralApplications().isEmpty()
            ),
            entry(
                SCENARIO_AAA6_LIP_QM_CASE_OFFLINE_OPEN_QUERIES_CLAIMANT.getScenario(),
                claimantQueryAwaitingResponse(caseData)
            )
        );
    }

    public boolean shouldRecordScenarioInCaseProgression(CaseData caseData) {
        return inCaseProgressionState(caseData)
            && caseData.isLipvLipOneVOne();
    }

    private boolean claimantQueryAwaitingResponse(CaseData caseData) {
        return featureToggleService.isPublicQueryManagementEnabled(caseData)
            && caseData.getQueries() != null
            && caseData.getQueries().hasAQueryAwaitingResponse();
    }
}
