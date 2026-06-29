package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.uploadtranslated;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContext;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimantresponse.ClaimantResponseClaimantDashboardTask;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimantresponse.ClaimantResponseDefendantDashboardTask;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimantresponse.JudgmentByAdmissionIssuedClaimantDashboardTask;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimantresponse.JudgmentByAdmissionIssuedDefendantDashboardTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.StateFlowDTO;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;
import uk.gov.hmcts.reform.civil.stateflow.model.State;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UploadTranslatedDocumentClaimantRejectsRepaymentPlanDashboardNotificationsTaskTest {

    @Mock
    private SimpleStateFlowEngine stateFlowEngine;
    @Mock
    private ClaimantResponseClaimantDashboardTask claimantTask;
    @Mock
    private ClaimantResponseDefendantDashboardTask defendantTask;
    @Mock
    private JudgmentByAdmissionIssuedClaimantDashboardTask judgmentByAdmissionClaimantTask;
    @Mock
    private JudgmentByAdmissionIssuedDefendantDashboardTask judgmentByAdmissionDefendantTask;
    @Mock
    private DashboardTaskContext context;

    private UploadTranslatedDocumentClaimantRejectsRepaymentPlanDashboardNotificationsTask task;
    private CaseData caseData;

    @BeforeEach
    void setUp() {
        task = new UploadTranslatedDocumentClaimantRejectsRepaymentPlanDashboardNotificationsTask(
            stateFlowEngine,
            claimantTask,
            defendantTask,
            judgmentByAdmissionClaimantTask,
            judgmentByAdmissionDefendantTask
        );

        caseData = CaseDataBuilder.builder().build();
        when(context.caseData()).thenReturn(caseData);
    }

    @Test
    void shouldSkipWhenCaseDataMissing() {
        when(context.caseData()).thenReturn(null);

        task.execute(context);

        verifyNoInteractions(
            stateFlowEngine,
            claimantTask,
            defendantTask,
            judgmentByAdmissionClaimantTask,
            judgmentByAdmissionDefendantTask
        );
    }

    @Test
    void shouldRunClaimantResponseNotificationsWhenNotInMediationAndNoJudgmentAdmission() {
        StateFlowDTO stateFlow = stateFlow(
            FlowState.Main.DRAFT.fullName(),
            false
        );
        when(stateFlowEngine.getStateFlow(caseData)).thenReturn(stateFlow);

        task.execute(context);

        verify(claimantTask).execute(context);
        verify(defendantTask).execute(context);
        verifyNoInteractions(
            judgmentByAdmissionClaimantTask,
            judgmentByAdmissionDefendantTask
        );
    }

    @Test
    void shouldRunJudgmentByAdmissionNotificationsWhenJudgmentAdmission() {
        StateFlowDTO stateFlow = stateFlow(
            FlowState.Main.IN_MEDIATION.fullName(),
            true
        );
        when(stateFlowEngine.getStateFlow(caseData)).thenReturn(stateFlow);

        task.execute(context);

        verify(judgmentByAdmissionClaimantTask).execute(context);
        verify(judgmentByAdmissionDefendantTask).execute(context);
        verifyNoInteractions(
            claimantTask,
            defendantTask
        );
    }

    @Test
    void shouldDoNothingWhenInMediationWithoutJudgmentAdmission() {
        StateFlowDTO stateFlow = stateFlow(
            FlowState.Main.IN_MEDIATION.fullName(),
            false
        );
        when(stateFlowEngine.getStateFlow(caseData)).thenReturn(stateFlow);

        task.execute(context);

        verifyNoInteractions(
            claimantTask,
            defendantTask,
            judgmentByAdmissionClaimantTask,
            judgmentByAdmissionDefendantTask
        );
    }

    private StateFlowDTO stateFlow(String stateName, boolean lipJudgmentAdmission) {
        Map<String, Boolean> flags = new HashMap<>();
        flags.put(FlowFlag.LIP_JUDGMENT_ADMISSION.name(), lipJudgmentAdmission);

        return new StateFlowDTO()
            .setState(stateName == null ? null : State.from(stateName))
            .setFlags(flags);
    }
}
