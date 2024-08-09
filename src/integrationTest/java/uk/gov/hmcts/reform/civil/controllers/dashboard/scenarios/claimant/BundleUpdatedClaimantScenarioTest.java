package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.civil.controllers.CaseEventsDashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.sdo.ClaimsTrack;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderType;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant.BundleUpdatedClaimantNotificationHandler;
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

public class BundleUpdatedClaimantScenarioTest extends CaseEventsDashboardBaseIntegrationTest {

    @Autowired
    private BundleUpdatedClaimantNotificationHandler handler;

    @Test
    void should_create_bundle_update_scenario() throws Exception {
        CaseData caseData = createCaseData(YesOrNo.NO);
        handler.handle(callbackParams(caseData));

        verifyNotification(caseData.getCcdCaseReference().toString(), "CLAIMANT", true);
    }

    @Test
    void should_not_create_bundle_updated_scenario() throws Exception {
        CaseData caseData = createCaseData(YesOrNo.YES);
        handler.handle(callbackParams(caseData));

        verifyNotification(caseData.getCcdCaseReference().toString(), "CLAIMANT", false);
    }

    private CaseData createCaseData(YesOrNo applicantRepresented) {
        LocalDateTime march = LocalDateTime.of(2024, 3, 1, 0, 0);
        LocalDateTime april = LocalDateTime.of(2024, 4, 1, 0, 0);
        List<IdValue<Bundle>> bundles = List.of(
            new IdValue<>("1", Bundle.builder().createdOn(Optional.of(march)).build()),
            new IdValue<>("2", Bundle.builder().createdOn(Optional.of(april)).build())
        );
        return CaseDataBuilder.builder().atStateTrialReadyCheck().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(12325480L)
            .applicant1Represented(applicantRepresented)
            .drawDirectionsOrderRequired(YesOrNo.YES)
            .drawDirectionsOrderSmallClaims(YesOrNo.NO)
            .claimsTrack(ClaimsTrack.fastTrack)
            .orderType(OrderType.DECIDE_DAMAGES)
            .caseBundles(bundles)
            .build();
    }

    private void verifyNotification(String caseId, String role, boolean isCreated) throws Exception {
        LocalDateTime april = LocalDateTime.of(2024, 4, 1, 0, 0);
        var resultActions = doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, role)
            .andExpect(status().isOk());

        if (isCreated) {
            resultActions.andExpectAll(
                jsonPath("$[0].titleEn").value("The case bundle has been updated"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">The case bundle was changed and re-uploaded on " +
                        DateUtils.formatDate(april) +
                        ". <a href=\"{VIEW_BUNDLE_REDIRECT}\" class=\"govuk-link\">Review the new bundle</a>.</p>"),
                jsonPath("$[0].titleCy").value("Mae bwndel yr achos wedi'i ddiweddaru"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Cafodd bwndel yr achos ei newid a'i ail-uwchlwytho ar " +
                        DateUtils.formatDateInWelsh(april.toLocalDate()) +
                        ". <a href=\"{VIEW_BUNDLE_REDIRECT}\" class=\"govuk-link\">Adolygu’r bwndel newydd</a>.</p>")
            );
        } else {
            resultActions.andExpect(jsonPath("$").isEmpty());
        }
    }
}