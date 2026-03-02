package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimantresponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContext;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.caseproceedsoffline.ApplicationsProceedOfflineClaimantDashboardTask;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.caseproceedsoffline.ApplicationsProceedOfflineDefendantDashboardTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.StateFlowDTO;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClaimantResponseCuiDashboardNotificationsTaskTest {

    @Mock
    private SimpleStateFlowEngine stateFlowEngine;
    @Mock
    private ClaimantResponseClaimantDashboardTask claimantTask;
    @Mock
    private ClaimantResponseDefendantDashboardTask defendantTask;
    @Mock
    private ClaimantCcjResponseClaimantDashboardTask claimantCcjTask;
    @Mock
    private ClaimantCcjResponseDefendantDashboardTask defendantCcjTask;
    @Mock
    private ApplicationsProceedOfflineClaimantDashboardTask claimantOfflineTask;
    @Mock
    private ApplicationsProceedOfflineDefendantDashboardTask defendantOfflineTask;
    @Mock
    private JudgmentByAdmissionIssuedClaimantDashboardTask judgmentByAdmissionClaimantTask;
    @Mock
    private JudgmentByAdmissionIssuedDefendantDashboardTask judgmentByAdmissionDefendantTask;
    @Mock
    private DashboardTaskContext context;

    private ClaimantResponseCuiDashboardNotificationsTask task;
    private CaseData caseData;

    @BeforeEach
    void setUp() {
        task = new ClaimantResponseCuiDashboardNotificationsTask(
            stateFlowEngine,
            claimantTask,
            defendantTask,
            claimantCcjTask,
            defendantCcjTask,
            claimantOfflineTask,
            defendantOfflineTask,
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
            claimantCcjTask,
            defendantCcjTask,
            claimantOfflineTask,
            defendantOfflineTask,
            judgmentByAdmissionClaimantTask,
            judgmentByAdmissionDefendantTask
        );
    }

    @Test
    void shouldRunClaimantResponseNotificationsWhenNoJudgmentAdmission() {
        when(stateFlowEngine.getStateFlow(caseData)).thenReturn(stateFlow(false, false));

        task.execute(context);

        verify(claimantTask).execute(context);
        verify(defendantTask).execute(context);
        verifyNoInteractions(
            claimantCcjTask,
            defendantCcjTask,
            claimantOfflineTask,
            defendantOfflineTask,
            judgmentByAdmissionClaimantTask,
            judgmentByAdmissionDefendantTask
        );
    }

    @Test
    void shouldRunOfflineAndCcjNotificationsWhenJudgmentAdmissionWithJoDisabled() {
        when(stateFlowEngine.getStateFlow(caseData)).thenReturn(stateFlow(true, false));

        task.execute(context);

        verify(claimantOfflineTask).execute(context);
        verify(defendantOfflineTask).execute(context);
        verify(claimantCcjTask).execute(context);
        verify(defendantCcjTask).execute(context);
        verifyNoInteractions(
            claimantTask,
            defendantTask,
            judgmentByAdmissionClaimantTask,
            judgmentByAdmissionDefendantTask
        );
    }

    @Test
    void shouldRunJudgmentByAdmissionNotificationsWhenJudgmentAdmissionWithJoEnabled() {
        when(stateFlowEngine.getStateFlow(caseData)).thenReturn(stateFlow(true, true));

        task.execute(context);

        verify(judgmentByAdmissionClaimantTask).execute(context);
        verify(judgmentByAdmissionDefendantTask).execute(context);
        verifyNoInteractions(
            claimantTask,
            defendantTask,
            claimantCcjTask,
            defendantCcjTask,
            claimantOfflineTask,
            defendantOfflineTask
        );
    }

    private StateFlowDTO stateFlow(boolean lipJudgmentAdmission, boolean joOnlineLiveEnabled) {
        Map<String, Boolean> flags = new HashMap<>();
        flags.put(FlowFlag.LIP_JUDGMENT_ADMISSION.name(), lipJudgmentAdmission);
        flags.put(FlowFlag.JO_ONLINE_LIVE_ENABLED.name(), joOnlineLiveEnabled);

        return new StateFlowDTO()
            .setFlags(flags);
    }
}
