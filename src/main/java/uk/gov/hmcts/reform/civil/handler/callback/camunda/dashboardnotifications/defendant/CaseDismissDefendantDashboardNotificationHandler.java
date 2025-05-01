package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.callback.DashboardCallbackHandler;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;
import uk.gov.hmcts.reform.dashboard.services.TaskListService;

import java.util.List;

@Service
public class CaseDismissDefendantDashboardNotificationHandler extends DashboardCallbackHandler {

    private static final List<CaseEvent> EVENTS = List
        .of(CaseEvent.CREATE_DASHBOARD_NOTIFICATION_DISMISS_CASE_DEFENDANT);
    private final DashboardNotificationService dashboardNotificationService;
    private final TaskListService taskListService;

    public CaseDismissDefendantDashboardNotificationHandler(DashboardScenariosService dashboardScenariosService,
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
        return DashboardScenarios.SCENARIO_AAA6_DISMISS_CASE_DEFENDANT.getScenario();
    }

    @Override
    public boolean shouldRecordScenario(CaseData caseData) {
        return YesOrNo.NO.equals(caseData.getRespondent1Represented());
    }

    @Override
    protected void beforeRecordScenario(CaseData caseData, String authToken) {
        final String caseId = String.valueOf(caseData.getCcdCaseReference());
        final String role = "DEFENDANT";
        dashboardNotificationService.deleteByReferenceAndCitizenRole(
            caseId,
            role
        );

        taskListService.makeProgressAbleTasksInactiveForCaseIdentifierAndRole(
            caseId,
            role
        );
    }
}
