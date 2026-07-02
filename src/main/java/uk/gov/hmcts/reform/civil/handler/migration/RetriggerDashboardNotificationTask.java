package uk.gov.hmcts.reform.civil.handler.migration;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.DashboardNotificationTaskCaseReference;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardCaseType;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardNotificationRegistry;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContext;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardWorkflowTask;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.UserService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;

@Component
@Slf4j
public class RetriggerDashboardNotificationTask extends MigrationTask<DashboardNotificationTaskCaseReference> {

    private final DashboardNotificationRegistry registry;
    private final UserService userService;
    private final SystemUpdateUserConfiguration userConfig;

    public RetriggerDashboardNotificationTask(
        DashboardNotificationRegistry registry,
        UserService userService,
        SystemUpdateUserConfiguration userConfig
    ) {
        super(DashboardNotificationTaskCaseReference.class);
        this.registry = registry;
        this.userService = userService;
        this.userConfig = userConfig;
    }

    @Override
    protected String getEventSummary() {
        return "Retrigger dashboard notification task via Migration Task";
    }

    @Override
    protected String getTaskName() {
        return "RetriggerDashboardNotificationTask";
    }

    @Override
    protected String getEventDescription() {
        return "This task runs the dashboard notification workflow for a Camunda dashboard task id";
    }

    @Override
    protected CaseData migrateCaseData(CaseData caseData, DashboardNotificationTaskCaseReference caseReference) {
        if (caseReference == null
            || StringUtils.isBlank(caseReference.getCaseReference())
            || StringUtils.isBlank(caseReference.getDashboardTaskId())) {
            throw new IllegalArgumentException("Case reference and dashboardTaskId must not be blank");
        }

        CaseData dashboardCaseData = caseData.toBuilder().build();
        dashboardCaseData.setBusinessProcess(businessProcessForDashboardTask(caseData, caseReference));

        String dashboardTaskId = caseReference.getDashboardTaskId();
        List<DashboardWorkflowTask> workflows = registry.workflowsFor(dashboardTaskId, DashboardCaseType.CIVIL);

        if (workflows.isEmpty()) {
            throw new IllegalArgumentException("No dashboard notification handlers registered for: " + dashboardTaskId);
        }

        String authToken = userService.getAccessToken(userConfig.getUserName(), userConfig.getPassword());
        DashboardTaskContext context = DashboardTaskContext.from(new CallbackParams()
            .caseData(dashboardCaseData)
            .isCivilCaseType(true)
            .params(Map.of(BEARER_TOKEN, authToken)));

        log.info("Retriggering dashboard task {} for case {}", dashboardTaskId, caseReference.getCaseReference());
        workflows.forEach(task -> task.execute(context));
        return caseData;
    }

    private BusinessProcess businessProcessForDashboardTask(
        CaseData caseData,
        DashboardNotificationTaskCaseReference caseReference
    ) {
        BusinessProcess businessProcess = caseData.getBusinessProcess() != null
            ? caseData.getBusinessProcess().copy()
            : new BusinessProcess();

        businessProcess.updateActivityId(caseReference.getDashboardTaskId());
        if (StringUtils.isNotBlank(caseReference.getDashboardProcessInstanceId())) {
            businessProcess.updateProcessInstanceId(caseReference.getDashboardProcessInstanceId());
        }

        return businessProcess;
    }
}
