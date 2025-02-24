package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.defendant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.CaseProgressionDashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.sdo.ClaimsTrack;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderType;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant.BundleUpdatedDefendantNotificationHandler;
import uk.gov.hmcts.reform.civil.model.Bundle;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdValue;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.utils.DateUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class BundleUpdatedDefendantScenarioTest extends CaseProgressionDashboardBaseIntegrationTest {

    @Autowired
    private BundleUpdatedDefendantNotificationHandler handler;

    @Test
    void should_create_bundle_update_scenario() throws Exception {
        CaseData caseData = createCaseData(YesOrNo.NO, false);
        handler.handle(callbackParams(caseData));

        verifyNotification(caseData.getCcdCaseReference().toString(), "DEFENDANT", true, false);
    }

    @Test
    void should_create_trial_ready_bundle_update_scenario() throws Exception {
        CaseData caseData = createCaseData(YesOrNo.NO, true);
        handler.handle(callbackParams(caseData));

        verifyNotification(caseData.getCcdCaseReference().toString(), "DEFENDANT", true, true);
    }

    @Test
    void should_not_create_bundle_updated_scenario() throws Exception {
        CaseData caseData = createCaseData(YesOrNo.YES, false);
        handler.handle(callbackParams(caseData));

        verifyNotification(caseData.getCcdCaseReference().toString(), "DEFENDANT", false, false);
    }

    private CaseData createCaseData(YesOrNo respondentRepresented, boolean isFastTrack) {
        List<IdValue<Bundle>> bundles = List.of(
            new IdValue<>("1", Bundle.builder().createdOn(Optional.of(LocalDateTime.of(2024, 3, 1, 0, 0))).build()),
            new IdValue<>("2", Bundle.builder().createdOn(Optional.of(LocalDateTime.of(2024, 4, 1, 0, 0))).build())
        );

        CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(12325480L)
            .respondent1Represented(respondentRepresented)
            .drawDirectionsOrderRequired(YesOrNo.YES)
            .drawDirectionsOrderSmallClaims(YesOrNo.NO)
            .claimsTrack(ClaimsTrack.fastTrack)
            .orderType(OrderType.DECIDE_DAMAGES)
            .caseBundles(bundles)
            .build();

        if (isFastTrack) {
            caseData = caseData.toBuilder().drawDirectionsOrderRequired(YesOrNo.YES)
                .drawDirectionsOrderSmallClaims(YesOrNo.NO)
                .claimsTrack(ClaimsTrack.fastTrack)
                .orderType(OrderType.DECIDE_DAMAGES)
                .build();
        }

        return caseData;
    }

    private void verifyNotification(String caseId, String role, boolean isCreated, boolean isFastTrack) throws Exception {
        var resultActions = doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, role)
            .andExpect(status().isOk());

        var taskListResultActions = doGet(BEARER_TOKEN, GET_TASKS_ITEMS_URL, caseId, role)
            .andExpect(status().isOk());

        if (isCreated) {
            resultActions.andExpectAll(
                jsonPath("$[0].titleEn").value("The case bundle has been updated"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">The case bundle was changed and re-uploaded on " +
                        DateUtils.formatDate(LocalDateTime.of(2024, 4, 1, 0, 0)) +
                        ". <a href=\"{VIEW_BUNDLE_REDIRECT}\" class=\"govuk-link\">Review the new bundle</a></p>"),
                jsonPath("$[0].titleCy").value("Mae bwndel yr achos wedi'i ddiweddaru"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Cafodd bwndel yr achos ei newid a'i ail-uwchlwytho ar " +
                        DateUtils.formatDateInWelsh(LocalDateTime.of(2024, 4, 1, 0, 0).toLocalDate(), false) +
                        ". <a href=\"{VIEW_BUNDLE_REDIRECT}\" class=\"govuk-link\">Adolygu’r bwndel newydd</a></p>")
            );
        } else {
            resultActions.andExpect(jsonPath("$").isEmpty());
        }

        if (isFastTrack) {
            taskListResultActions.andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].reference").value(caseId.toString()),
                jsonPath("$[0].taskNameEn").value(
                    "<a href={VIEW_BUNDLE} class=\"govuk-link\">View the bundle</a>")
            );
        }
    }
}
