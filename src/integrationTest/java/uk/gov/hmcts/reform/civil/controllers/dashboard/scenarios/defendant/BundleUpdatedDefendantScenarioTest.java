package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.defendant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.civil.controllers.CaseEventsDashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.sdo.ClaimsTrack;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderType;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant.BundleUpdatedDefendantNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.utils.DateUtils;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class BundleUpdatedDefendantScenarioTest extends CaseEventsDashboardBaseIntegrationTest {

    @Autowired
    private BundleUpdatedDefendantNotificationHandler handler;

    @Test
    void should_create_bundle_update_scenario() throws Exception {
        CaseData caseData = createCaseData(YesOrNo.NO);
        handler.handle(callbackParams(caseData));

        verifyNotification(caseData.getCcdCaseReference().toString(), "DEFENDANT", true);
    }

    @Test
    void should_not_create_bundle_updated_scenario() throws Exception {
        CaseData caseData = createCaseData(YesOrNo.YES);
        handler.handle(callbackParams(caseData));

        verifyNotification(caseData.getCcdCaseReference().toString(), "DEFENDANT", false);
    }

    private CaseData createCaseData(YesOrNo respondentRepresented) {
        return CaseDataBuilder.builder().atStateTrialReadyCheck().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(12325480L)
            .respondent1Represented(respondentRepresented)
            .drawDirectionsOrderRequired(YesOrNo.YES)
            .drawDirectionsOrderSmallClaims(YesOrNo.NO)
            .claimsTrack(ClaimsTrack.fastTrack)
            .orderType(OrderType.DECIDE_DAMAGES)
            .build();
    }

    private void verifyNotification(String caseId, String role, boolean isCreated) throws Exception {
        var resultActions = doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, role)
            .andExpect(status().isOk());

        if (isCreated) {
            resultActions.andExpectAll(
                jsonPath("$[0].titleEn").value("The case bundle has been updated"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">The case bundle was changed and re-uploaded on " +
                        DateUtils.formatDate(LocalDate.now()) +
                        ". <a href=\"{VIEW_BUNDLE_REDIRECT}\" rel=\"noopener noreferrer\" target=\"_blank\" class=\"govuk-link\">Review the new bundle</a>.</p>"),
                jsonPath("$[0].titleCy").value("The case bundle has been updated"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">The case bundle was changed and re-uploaded on " +
                        DateUtils.formatDateInWelsh(LocalDate.now()) +
                        ". <a href=\"{VIEW_BUNDLE_REDIRECT}\" rel=\"noopener noreferrer\" target=\"_blank\" class=\"govuk-link\">Review the new bundle</a>.</p>")
            );
        } else {
            resultActions.andExpect(jsonPath("$").isEmpty());
        }
    }
}
