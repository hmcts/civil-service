package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.defendant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant.RecordJudgementDefendantNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DefendantRecordJudgementScenarioTest extends  DashboardBaseIntegrationTest {

    public static final String DEFENDANT = "DEFENDANT";

    @Autowired
    private RecordJudgementDefendantNotificationHandler handler;

    @Test
    void should_create_scenario_for_record_judgement() throws Exception {

        CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .legacyCaseReference("reference")
                .respondent1Represented(YesOrNo.NO)
                .buildJudgmentOnlineCaseDataWithDeterminationMeans();

        when(featureToggleService.isGeneralApplicationsEnabled()).thenReturn(true);
        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);

        handler.handle(callbackParams(caseData));
        String caseId = "720111";

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, DEFENDANT)
            .andExpect(status().isOk());

    }
}
