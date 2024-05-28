package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.defendant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.controllers.dashboard.mock.MockTaskList;
import uk.gov.hmcts.reform.civil.controllers.dashboard.util.Evaluations;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.user.CuiUploadMediationDocumentsCallbackHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.dashboard.data.TaskList;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UploadMediationDocumentsScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private CuiUploadMediationDocumentsCallbackHandler handler;

    @Test
    void should_create_upload_mediation_scenario_for_carm_as_respondent_solicitor_one() throws Exception {
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);
        when(coreCaseUserService.getUserCaseRoles(any(), any()))
            .thenReturn(List.of(CaseRole.RESPONDENTSOLICITORONE.getFormattedName()));
        String caseId = String.valueOf(System.currentTimeMillis());
        final List<TaskList> taskListExpected = MockTaskList.getUploadMediationTaskListMock("DEFENDANT", caseId);

        final List<TaskList> taskListClaimantExpected = MockTaskList.getUploadMediationTaskListViewMediationAvailableMock("CLAIMANT", caseId);

        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued1v1LiP().build()
            .toBuilder()
            .ccdCaseReference(Long.valueOf(caseId))
            .applicant1Represented(YesOrNo.NO)
            .respondent1(Party.builder().individualFirstName("John").individualLastName("Doe")
                             .type(Party.Type.INDIVIDUAL).build())
            .build();

        handler.handle(callbackParams(caseData));

        //Verify dashboard information
        String result = doGet(BEARER_TOKEN, GET_TASKS_ITEMS_URL, caseId, "DEFENDANT")
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        List<TaskList> response = toTaskList(result);
        Evaluations.evaluateSizeOfTasklist(response.size(), taskListExpected.size());
        Evaluations.evaluateMediationTasklist(response, taskListExpected);

        //Verify claimant's dashboard information
        String resultClaimant = doGet(BEARER_TOKEN, GET_TASKS_ITEMS_URL, caseId, "CLAIMANT")
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        List<TaskList> responseClaimant = toTaskList(resultClaimant);
        Evaluations.evaluateSizeOfTasklist(responseClaimant.size(), taskListClaimantExpected.size());
        Evaluations.evaluateMediationTasklist(responseClaimant, taskListClaimantExpected);
    }

    @Test
    void should_create_upload_mediation_scenario_for_carm_as_defendant() throws Exception {
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);
        when(coreCaseUserService.getUserCaseRoles(any(), any()))
            .thenReturn(List.of(CaseRole.DEFENDANT.getFormattedName()));
        String caseId = String.valueOf(System.currentTimeMillis());
        final List<TaskList> taskListExpected = MockTaskList.getUploadMediationTaskListMock("DEFENDANT", caseId);

        final List<TaskList> taskListClaimantExpected = MockTaskList.getUploadMediationTaskListViewMediationAvailableMock("CLAIMANT", caseId);

        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued1v1LiP().build()
            .toBuilder()
            .ccdCaseReference(Long.valueOf(caseId))
            .applicant1Represented(YesOrNo.NO)
            .respondent1(Party.builder().individualFirstName("John").individualLastName("Doe")
                             .type(Party.Type.INDIVIDUAL).build())
            .build();

        handler.handle(callbackParams(caseData));

        //Verify dashboard information
        String result = doGet(BEARER_TOKEN, GET_TASKS_ITEMS_URL, caseId, "DEFENDANT")
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        List<TaskList> response = toTaskList(result);
        Evaluations.evaluateSizeOfTasklist(response.size(), taskListExpected.size());
        Evaluations.evaluateMediationTasklist(response, taskListExpected);

        //Verify claimant's dashboard information
        String resultClaimant = doGet(BEARER_TOKEN, GET_TASKS_ITEMS_URL, caseId, "CLAIMANT")
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        List<TaskList> responseClaimant = toTaskList(resultClaimant);
        Evaluations.evaluateSizeOfTasklist(responseClaimant.size(), taskListClaimantExpected.size());
        Evaluations.evaluateMediationTasklist(responseClaimant, taskListClaimantExpected);
    }
}
