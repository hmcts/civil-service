package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.callback.DashboardCallbackHandler;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;
import uk.gov.hmcts.reform.dashboard.services.TaskListService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_CASE_PROCEED_OFFLINE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CASE_PROCEED_IN_CASE_MAN_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CASE_PROCEED_IN_CASE_MAN_CLAIMANT_WITHOUT_TASK_CHANGES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_AVAILABLE_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_INITIATE_APPLICATION_INACTIVE_CLAIMANT;

@Service
public class CaseProceedOfflineClaimantNotificationHandler extends DashboardCallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_CASE_PROCEED_OFFLINE);
    private static final List<CaseState> caseMovedInCaseManStates = List.of(CaseState.AWAITING_APPLICANT_INTENTION,
            CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT,
            CaseState.IN_MEDIATION, CaseState.JUDICIAL_REFERRAL);
    private static final List<CaseState> caseMovedInCaseManStatesCaseProgression =
        List.of(CaseState.CASE_PROGRESSION,
                CaseState.HEARING_READINESS,
                CaseState.PREPARE_FOR_HEARING_CONDUCT_HEARING,
                CaseState.DECISION_OUTCOME,
                CaseState.All_FINAL_ORDERS_ISSUED);
    public static final String TASK_ID = "GenerateClaimantDashboardNotificationCaseProceedOffline";
    public static final String GA = "Applications";
    private final DashboardNotificationService dashboardNotificationService;
    private final TaskListService taskListService;

    public CaseProceedOfflineClaimantNotificationHandler(DashboardScenariosService dashboardScenariosService,
                                                         DashboardNotificationsParamsMapper mapper,
                                                         FeatureToggleService featureToggleService,
                                                         DashboardNotificationService dashboardNotificationService,
                                                         TaskListService taskListService) {
        super(dashboardScenariosService, mapper, featureToggleService);
        this.dashboardNotificationService = dashboardNotificationService;
        this.taskListService = taskListService;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public String getScenario(CaseData caseData) {
        if (featureToggleService.isCaseProgressionEnabled()) {
            return SCENARIO_AAA6_CASE_PROCEED_IN_CASE_MAN_CLAIMANT_WITHOUT_TASK_CHANGES.getScenario();
        }
        return SCENARIO_AAA6_CASE_PROCEED_IN_CASE_MAN_CLAIMANT.getScenario();
    }

    @Override
    public Map<String, Boolean> getScenarios(CaseData caseData) {
        return Map.of(
            SCENARIO_AAA6_GENERAL_APPLICATION_INITIATE_APPLICATION_INACTIVE_CLAIMANT.getScenario(),
            featureToggleService.isGaForLipsEnabled(),
            SCENARIO_AAA6_GENERAL_APPLICATION_AVAILABLE_CLAIMANT.getScenario(),
            featureToggleService.isGaForLipsEnabled()
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    @Override
    public boolean shouldRecordScenario(CaseData caseData) {
        return (caseData.getPreviousCCDState() != null && caseMovedInCaseManStates.contains(caseData.getPreviousCCDState())
                && caseData.isLipvLipOneVOne()) || (shouldRecordScenarioInCaseProgression(caseData));
    }

    public boolean shouldRecordScenarioInCaseProgression(CaseData caseData) {
        return featureToggleService.isCaseProgressionEnabled()
            && caseMovedInCaseManStatesCaseProgression.contains(caseData.getPreviousCCDState())
            && caseData.isLipvLipOneVOne();
    }

    @Override
    protected void beforeRecordScenario(CaseData caseData, String authToken) {
        final String caseId = String.valueOf(caseData.getCcdCaseReference());

        dashboardNotificationService.deleteByReferenceAndCitizenRole(
            caseId,
            "CLAIMANT"
        );

        taskListService.makeProgressAbleTasksInactiveForCaseIdentifierAndRole(
            caseId,
            "CLAIMANT",
            GA
        );
    }
}
