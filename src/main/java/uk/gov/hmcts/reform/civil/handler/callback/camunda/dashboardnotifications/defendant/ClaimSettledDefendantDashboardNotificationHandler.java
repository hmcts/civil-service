package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.callback.DashboardCallbackHandler;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;
import uk.gov.hmcts.reform.dashboard.services.TaskListService;

import java.util.List;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_FOR_CLAIM_SETTLED_FOR_DEFENDANT1;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_CLAIM_SETTLE_EVENT_DEFENDANT;

@Service
public class ClaimSettledDefendantDashboardNotificationHandler extends DashboardCallbackHandler {

    private static final List<CaseEvent> EVENTS =
        List.of(CREATE_DASHBOARD_NOTIFICATION_FOR_CLAIM_SETTLED_FOR_DEFENDANT1);
    public static final String TASK_ID = "CreateClaimSettledDashboardNotificationsForDefendant1";
    private final DashboardNotificationService dashboardNotificationService;
    private final TaskListService taskListService;

    public ClaimSettledDefendantDashboardNotificationHandler(DashboardScenariosService dashboardScenariosService,
                                                             DashboardNotificationsParamsMapper mapper,
                                                             FeatureToggleService featureToggleService,
                                                             DashboardNotificationService dashboardNotificationService,
                                                             TaskListService taskListService) {
        super(dashboardScenariosService, mapper, featureToggleService);
        this.dashboardNotificationService = dashboardNotificationService;
        this.taskListService = taskListService;
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public String getScenario(CaseData caseData) {
        return SCENARIO_AAA6_CLAIMANT_INTENT_CLAIM_SETTLE_EVENT_DEFENDANT.getScenario();
    }

    @Override
    protected void beforeRecordScenario(CaseData caseData, String authToken) {
        final String caseId = String.valueOf(caseData.getCcdCaseReference());
        if (featureToggleService.isQueryManagementLRsEnabled() && (caseData.getEaCourtLocation().equals(YesOrNo.YES))) {

            dashboardNotificationService.deleteByReferenceAndCitizenRole(
                caseId,
                "DEFENDANT"
            );

            taskListService.makeProgressAbleTasksInactiveForCaseIdentifierAndRoleExcludingTemplate(
                caseId,
                "DEFENDANT",
                "Application.View"
            );
        }
    }
}
