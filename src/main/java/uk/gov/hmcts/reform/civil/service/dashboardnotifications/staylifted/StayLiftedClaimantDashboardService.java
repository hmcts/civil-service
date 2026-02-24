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

    protected StayLiftedClaimantDashboardService(DashboardScenariosService dashboardScenariosService,
                                                 DashboardNotificationsParamsMapper mapper,
                                                 FeatureToggleService featureToggleService) {
        super(dashboardScenariosService, mapper);
        this.featureToggleService = featureToggleService;
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
            Map<String, Boolean> scenarios = new HashMap<>();
            scenarios.put(SCENARIO_AAA6_CP_STAY_LIFTED_CLAIMANT.getScenario(), true);
            scenarios.putAll(getScenariosBasedOnPreStayState(caseData));
            return scenarios;
        }

        return new HashMap<>();
    }

    private Map<String, Boolean> getScenariosBasedOnPreStayState(CaseData caseData) {
        return switch (CaseState.valueOf(caseData.getPreStayState())) {
            case AWAITING_RESPONDENT_ACKNOWLEDGEMENT -> Map.of(
                SCENARIO_AAA6_CLAIM_ISSUE_RESPONSE_AWAIT.getScenario(), true);
            case AWAITING_APPLICANT_INTENTION, IN_MEDIATION, JUDICIAL_REFERRAL, CASE_PROGRESSION, DECISION_OUTCOME,
                 All_FINAL_ORDERS_ISSUED -> Map.of(
                getViewDocumentsScenario(caseData).getScenario(), true);
            case HEARING_READINESS, PREPARE_FOR_HEARING_CONDUCT_HEARING -> Map.of(
                getViewDocumentsScenario(caseData).getScenario(), true,
                SCENARIO_AAA6_CP_STAY_LIFTED_RESET_HEARING_TASKS_CLAIMANT.getScenario(), true,
                SCENARIO_AAA6_CP_STAY_LIFTED_RESET_HEARING_FEE_PAID_TASK.getScenario(), !caseData.isHearingFeePaid()
            );
            default -> Map.of();
        };
    }

    private DashboardScenarios getViewDocumentsScenario(CaseData caseData) {
        return nonNull(caseData.getCaseDocumentUploadDate()) || nonNull(caseData.getCaseDocumentUploadDateRes())
            ? SCENARIO_AAA6_CP_STAY_LIFTED_VIEW_DOCUMENTS_TASK_AVAILABLE_CLAIMANT
            : SCENARIO_AAA6_CP_STAY_LIFTED_VIEW_DOCUMENTS_TASK_NOT_AVAILABLE_CLAIMANT;
    }
}
