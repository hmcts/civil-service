package uk.gov.hmcts.reform.civil.service.dashboardnotifications.staylifted;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardScenarioService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIM_ISSUE_RESPONSE_AWAIT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_STAY_LIFTED_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_STAY_LIFTED_RESET_HEARING_FEE_PAID_TASK;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_STAY_LIFTED_RESET_HEARING_TASKS_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_STAY_LIFTED_VIEW_DOCUMENTS_TASK_AVAILABLE_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_STAY_LIFTED_VIEW_DOCUMENTS_TASK_NOT_AVAILABLE_CLAIMANT;

@Service
public class StayLiftedClaimantDashboardService extends DashboardScenarioService {

    private final FeatureToggleService featureToggleService;
    private final StayLiftedDashboardHelper stayLiftedDashboardHelper;

    protected StayLiftedClaimantDashboardService(DashboardScenariosService dashboardScenariosService,
                                                 DashboardNotificationsParamsMapper mapper,
                                                 FeatureToggleService featureToggleService,
                                                 StayLiftedDashboardHelper stayLiftedDashboardHelper) {
        super(dashboardScenariosService, mapper);
        this.featureToggleService = featureToggleService;
        this.stayLiftedDashboardHelper = stayLiftedDashboardHelper;
    }

    public void notifyStayLifted(CaseData caseData, String authToken) {
        recordScenario(caseData, authToken);
    }

    @Override
    protected String getScenario(CaseData caseData) {
        return null;
    }

    @Override
    protected Map<String, Boolean> getScenarios(CaseData caseData) {
        if (caseData.isApplicant1NotRepresented() && featureToggleService.isLipVLipEnabled()) {
            return Map.of(
                SCENARIO_AAA6_CP_STAY_LIFTED_CLAIMANT.getScenario(),
                true,
                SCENARIO_AAA6_CP_STAY_LIFTED_RESET_HEARING_TASKS_CLAIMANT.getScenario(),
                stayLiftedDashboardHelper.hadHearingScheduled(caseData),
                SCENARIO_AAA6_CP_STAY_LIFTED_RESET_HEARING_FEE_PAID_TASK.getScenario(),
                stayLiftedDashboardHelper.hadHearingScheduled(caseData) && !caseData.isHearingFeePaid(),
                getViewDocumentsScenario(caseData).getScenario(),
                stayLiftedDashboardHelper.isNotPreCaseProgression(caseData),
                SCENARIO_AAA6_CLAIM_ISSUE_RESPONSE_AWAIT.getScenario(),
                CaseState.AWAITING_RESPONDENT_RESPONSE.toString().equals(caseData.getPreStayState())
            );
        }

        return new HashMap<>();
    }

    private DashboardScenarios getViewDocumentsScenario(CaseData caseData) {
        return nonNull(caseData.getCaseDocumentUploadDate()) || nonNull(caseData.getCaseDocumentUploadDateRes())
            ? SCENARIO_AAA6_CP_STAY_LIFTED_VIEW_DOCUMENTS_TASK_AVAILABLE_CLAIMANT
            : SCENARIO_AAA6_CP_STAY_LIFTED_VIEW_DOCUMENTS_TASK_NOT_AVAILABLE_CLAIMANT;
    }
}
