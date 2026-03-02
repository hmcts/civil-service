package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimantresponse;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContext;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardWorkflowTask;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.caseproceedsoffline.ApplicationsProceedOfflineClaimantDashboardTask;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.caseproceedsoffline.ApplicationsProceedOfflineDefendantDashboardTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.StateFlowDTO;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;

@Component
public class ClaimantResponseCuiDashboardNotificationsTask extends DashboardWorkflowTask {

    private final SimpleStateFlowEngine stateFlowEngine;
    private final ClaimantResponseClaimantDashboardTask claimantTask;
    private final ClaimantResponseDefendantDashboardTask defendantTask;
    private final ClaimantCcjResponseClaimantDashboardTask claimantCcjTask;
    private final ClaimantCcjResponseDefendantDashboardTask defendantCcjTask;
    private final ApplicationsProceedOfflineClaimantDashboardTask claimantOfflineTask;
    private final ApplicationsProceedOfflineDefendantDashboardTask defendantOfflineTask;
    private final JudgmentByAdmissionIssuedClaimantDashboardTask judgmentByAdmissionClaimantTask;
    private final JudgmentByAdmissionIssuedDefendantDashboardTask judgmentByAdmissionDefendantTask;

    public ClaimantResponseCuiDashboardNotificationsTask(
        SimpleStateFlowEngine stateFlowEngine,
        ClaimantResponseClaimantDashboardTask claimantTask,
        ClaimantResponseDefendantDashboardTask defendantTask,
        ClaimantCcjResponseClaimantDashboardTask claimantCcjTask,
        ClaimantCcjResponseDefendantDashboardTask defendantCcjTask,
        ApplicationsProceedOfflineClaimantDashboardTask claimantOfflineTask,
        ApplicationsProceedOfflineDefendantDashboardTask defendantOfflineTask,
        JudgmentByAdmissionIssuedClaimantDashboardTask judgmentByAdmissionClaimantTask,
        JudgmentByAdmissionIssuedDefendantDashboardTask judgmentByAdmissionDefendantTask
    ) {
        this.stateFlowEngine = stateFlowEngine;
        this.claimantTask = claimantTask;
        this.defendantTask = defendantTask;
        this.claimantCcjTask = claimantCcjTask;
        this.defendantCcjTask = defendantCcjTask;
        this.claimantOfflineTask = claimantOfflineTask;
        this.defendantOfflineTask = defendantOfflineTask;
        this.judgmentByAdmissionClaimantTask = judgmentByAdmissionClaimantTask;
        this.judgmentByAdmissionDefendantTask = judgmentByAdmissionDefendantTask;
    }

    @Override
    public void execute(DashboardTaskContext context) {
        CaseData caseData = context.caseData();
        if (caseData == null) {
            return;
        }

        StateFlowDTO stateFlow = stateFlowEngine.getStateFlow(caseData);
        boolean lipJudgmentAdmission = isFlagSet(stateFlow, FlowFlag.LIP_JUDGMENT_ADMISSION);
        boolean joOnlineLiveEnabled = isFlagSet(stateFlow, FlowFlag.JO_ONLINE_LIVE_ENABLED);

        if (!lipJudgmentAdmission) {
            claimantTask.execute(context);
            defendantTask.execute(context);
            return;
        }

        if (!joOnlineLiveEnabled) {
            claimantOfflineTask.execute(context);
            defendantOfflineTask.execute(context);
            claimantCcjTask.execute(context);
            defendantCcjTask.execute(context);
            return;
        }

        judgmentByAdmissionClaimantTask.execute(context);
        judgmentByAdmissionDefendantTask.execute(context);
    }

    private boolean isFlagSet(StateFlowDTO stateFlow, FlowFlag flowFlag) {
        return stateFlow != null && stateFlow.isFlagSet(flowFlag);
    }
}
