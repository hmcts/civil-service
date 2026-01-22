package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.uploadtranslated;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContext;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardWorkflowTask;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.caseproceedsoffline.ApplicationsProceedOfflineClaimantDashboardTask;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.caseproceedsoffline.ApplicationsProceedOfflineDefendantDashboardTask;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimantresponse.ClaimantCcjResponseClaimantDashboardTask;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimantresponse.ClaimantCcjResponseDefendantDashboardTask;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimantresponse.ClaimantResponseClaimantDashboardTask;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimantresponse.ClaimantResponseDefendantDashboardTask;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimantresponse.JudgmentByAdmissionIssuedClaimantDashboardTask;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimantresponse.JudgmentByAdmissionIssuedDefendantDashboardTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.StateFlowDTO;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;

@Component
public class UploadTranslatedDocumentClaimantRejectsRepaymentPlanDashboardNotificationsTask extends DashboardWorkflowTask {

    private final SimpleStateFlowEngine stateFlowEngine;
    private final ClaimantResponseClaimantDashboardTask claimantTask;
    private final ClaimantResponseDefendantDashboardTask defendantTask;
    private final ClaimantCcjResponseClaimantDashboardTask claimantCcjTask;
    private final ClaimantCcjResponseDefendantDashboardTask defendantCcjTask;
    private final ApplicationsProceedOfflineClaimantDashboardTask claimantOfflineTask;
    private final ApplicationsProceedOfflineDefendantDashboardTask defendantOfflineTask;
    private final JudgmentByAdmissionIssuedClaimantDashboardTask judgmentByAdmissionClaimantTask;
    private final JudgmentByAdmissionIssuedDefendantDashboardTask judgmentByAdmissionDefendantTask;

    public UploadTranslatedDocumentClaimantRejectsRepaymentPlanDashboardNotificationsTask(
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
        boolean inMediation = isInMediation(stateFlow);
        boolean lipJudgmentAdmission = stateFlow.isFlagSet(FlowFlag.LIP_JUDGMENT_ADMISSION);
        boolean joOnlineLiveEnabled = stateFlow.isFlagSet(FlowFlag.JO_ONLINE_LIVE_ENABLED);

        if (!inMediation && !lipJudgmentAdmission) {
            claimantTask.execute(context);
            defendantTask.execute(context);
            return;
        }

        if (!inMediation && lipJudgmentAdmission && !joOnlineLiveEnabled) {
            claimantOfflineTask.execute(context);
            defendantOfflineTask.execute(context);
            claimantCcjTask.execute(context);
            defendantCcjTask.execute(context);
            return;
        }

        if (lipJudgmentAdmission && joOnlineLiveEnabled) {
            judgmentByAdmissionClaimantTask.execute(context);
            judgmentByAdmissionDefendantTask.execute(context);
        }
    }

    private boolean isInMediation(StateFlowDTO stateFlow) {
        return stateFlow != null
            && stateFlow.getState() != null
            && FlowState.Main.IN_MEDIATION.fullName().equals(stateFlow.getState().getName());
    }
}
