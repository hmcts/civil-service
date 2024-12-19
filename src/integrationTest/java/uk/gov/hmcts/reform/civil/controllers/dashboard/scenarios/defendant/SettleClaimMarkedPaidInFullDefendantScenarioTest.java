package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.defendant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.settlediscontinue.MarkPaidConsentList;
import uk.gov.hmcts.reform.civil.utils.DateUtils;
import java.time.LocalDate;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant.SettleClaimPaidInFullDefendantDashboardNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SettleClaimMarkedPaidInFullDefendantScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private SettleClaimPaidInFullDefendantDashboardNotificationHandler handler;

    @Test
    void should_create_scenario_for_claim_marked_paid_in_full() throws Exception {

        String caseId = "12348991013";
        LocalDate paidInFullDate = LocalDate.now();
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .markPaidConsent(MarkPaidConsentList.YES)
            .respondent1Represented(YesOrNo.NO)
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "DEFENDANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Claim marked as paid in full"),
                jsonPath("$[0].descriptionEn").value("<p class=\"govuk-body\">This claim has been marked as paid in full as of " + DateUtils.formatDate(paidInFullDate) + ".<br>" +
                               "You do not need to attend court and any hearings scheduled will not go ahead.</p>"),
                jsonPath("$[0].titleCy").value("Hawliad wedi’i nodi fel wedi ei dalu’n llawn"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Mae’r hawliad hwn wedi’i farcio fel un a dalwyd yn llawn ers "+ DateUtils.formatDateInWelsh(paidInFullDate) + ".<br>" +
                                     "Nid oes angen i chi fynychu'r llys ac ni fydd unrhyw wrandawiadau a drefnwyd yn cael eu cynnal.</p>")
            );
    }
}
