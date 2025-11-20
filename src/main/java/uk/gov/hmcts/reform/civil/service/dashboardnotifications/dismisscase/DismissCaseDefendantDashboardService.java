package uk.gov.hmcts.reform.civil.service.dashboardnotifications.dismisscase;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardScenarioService;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;
import uk.gov.hmcts.reform.dashboard.services.TaskListService;

import java.util.Map;

@Service
public class DismissCaseDefendantDashboardService extends DashboardScenarioService {

    private final DashboardNotificationService dashboardNotificationService;
    private final TaskListService taskListService;
    private final FeatureToggleService featureToggleService;

    public DismissCaseDefendantDashboardService(DashboardScenariosService dashboardScenariosService,
                                                DashboardNotificationsParamsMapper mapper,
                                                FeatureToggleService featureToggleService,
                                                DashboardNotificationService dashboardNotificationService,
                                                TaskListService taskListService) {
        super(dashboardScenariosService, mapper);
        this.dashboardNotificationService = dashboardNotificationService;
        this.taskListService = taskListService;
        this.featureToggleService = featureToggleService;
    }

    public void notifyCaseDismissed(CaseData caseData, String authToken) {
        recordScenario(caseData, authToken);
    }

    @Override
    protected String getScenario(CaseData caseData) {
        return DashboardScenarios.SCENARIO_AAA6_DISMISS_CASE_DEFENDANT.getScenario();
    }

    @Override
    protected Map<String, Boolean> getScenarios(CaseData caseData) {
        return Map.of(
            DashboardScenarios.SCENARIO_AAA6_LIP_QM_CASE_OFFLINE_OPEN_QUERIES_DEFENDANT.getScenario(),
            defendantQueryAwaitingResponse(caseData)
        );
    }

    @Override
    protected boolean shouldRecordScenario(CaseData caseData) {
        return YesOrNo.NO.equals(caseData.getRespondent1Represented());
    }

    @Override
    protected void beforeRecordScenario(CaseData caseData, String authToken) {
        String caseId = String.valueOf(caseData.getCcdCaseReference());
        dashboardNotificationService.deleteByReferenceAndCitizenRole(caseId, "DEFENDANT");
        taskListService.makeProgressAbleTasksInactiveForCaseIdentifierAndRole(caseId, "DEFENDANT");
    }

    private boolean defendantQueryAwaitingResponse(CaseData caseData) {
        return featureToggleService.isPublicQueryManagementEnabled(caseData)
            && caseData.getQueries() != null
            && caseData.getQueries().hasAQueryAwaitingResponse();
    }
}
