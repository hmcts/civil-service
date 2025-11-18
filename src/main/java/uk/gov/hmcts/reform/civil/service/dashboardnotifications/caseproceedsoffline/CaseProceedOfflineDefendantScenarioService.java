package uk.gov.hmcts.reform.civil.service.dashboardnotifications.caseproceedsoffline;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.Map;

import static java.util.Map.entry;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CASE_PROCEED_IN_CASE_MAN_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CASE_PROCEED_IN_CASE_MAN_DEFENDANT_WITHOUT_TASK_CHANGES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_AVAILABLE_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_INITIATE_APPLICATION_INACTIVE_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_LIP_QM_CASE_OFFLINE_OPEN_QUERIES_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_UPDATE_CASE_PROCEED_IN_CASE_MAN_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_UPDATE_CASE_PROCEED_IN_CASE_MAN_DEFENDANT_FAST_TRACK;

@Service
public class CaseProceedOfflineDefendantScenarioService extends CaseProceedOfflineScenarioService {

    public CaseProceedOfflineDefendantScenarioService(FeatureToggleService featureToggleService) {
        super(featureToggleService);
    }

    public String resolvePrimaryScenario(CaseData caseData) {
        if (caseData.getActiveJudgment() != null) {
            if (caseData.isFastTrackClaim()) {
                return SCENARIO_AAA6_UPDATE_CASE_PROCEED_IN_CASE_MAN_DEFENDANT_FAST_TRACK.getScenario();
            }
            return SCENARIO_AAA6_UPDATE_CASE_PROCEED_IN_CASE_MAN_DEFENDANT.getScenario();
        }
        return SCENARIO_AAA6_CASE_PROCEED_IN_CASE_MAN_DEFENDANT_WITHOUT_TASK_CHANGES.getScenario();
    }

    public Map<String, Boolean> resolveAdditionalScenarios(CaseData caseData) {
        return Map.ofEntries(
            entry(SCENARIO_AAA6_GENERAL_APPLICATION_INITIATE_APPLICATION_INACTIVE_DEFENDANT.getScenario(), true),
            entry(
                SCENARIO_AAA6_GENERAL_APPLICATION_AVAILABLE_DEFENDANT.getScenario(),
                caseData.getGeneralApplications() != null && !caseData.getGeneralApplications().isEmpty()
            ),
            entry(
                SCENARIO_AAA6_LIP_QM_CASE_OFFLINE_OPEN_QUERIES_DEFENDANT.getScenario(),
                defendantQueryAwaitingResponse(caseData)
            )
        );
    }

    public boolean shouldRecordScenarioInCaseProgression(CaseData caseData, boolean isLipvLipOrLRvLip) {
        return inCaseProgressionState(caseData)
            && isLipvLipOrLRvLip;
    }

    private boolean defendantQueryAwaitingResponse(CaseData caseData) {
        return featureToggleService.isPublicQueryManagementEnabled(caseData)
            && caseData.getQueries() != null
            && caseData.getQueries().hasAQueryAwaitingResponse();
    }
}
