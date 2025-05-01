package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.defendant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant.ClaimantResponseDefendantNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ClaimFullDefenceProceedDefendantNoMediationScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private ClaimantResponseDefendantNotificationHandler handler;

    @Test
    void should_create_scenario_for_claim_settled() throws Exception {

        String caseId = "12348991013";

        CaseData caseData = CaseDataBuilder.builder()
            .atStateRespondentPartAdmissionSpec().build().toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .ccdState(CaseState.JUDICIAL_REFERRAL)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .responseClaimMediationSpecRequired(YesOrNo.NO)
            .respondent1Represented(YesOrNo.NO)
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "DEFENDANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Wait for the court to review the case"),
                jsonPath("$[0].descriptionEn")
                    .value("<p class=\"govuk-body\">Mr. John Rambo wants to proceed with the claim." +
                               " The case will be referred to a judge who will decide what should happen next.</p>" +
                               "<p class=\"govuk-body\">You can <a href=\"{VIEW_RESPONSE_TO_CLAIM}\" class=\"govuk-link\">view your response</a> or " +
                               "<a href=\"{VIEW_CLAIMANT_HEARING_REQS}\" rel=\"noopener noreferrer\" class=\"govuk-link\" target=\"_blank\">view the claimant's hearing requirements (opens in a new tab)</a>.</p>"),
                jsonPath("$[0].titleCy").value("Aros i’r llys adolygu’r achos"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Mae Mr. John Rambo eisiau parhau â’r hawliad." +
                                     " Bydd yr achos yn cael ei gyfeirio at farnwr a fydd yn penderfynu beth ddylai ddigwydd nesaf.</p>" +
                                     "<p class=\"govuk-body\">Gallwch <a href=\"{VIEW_RESPONSE_TO_CLAIM}\" class=\"govuk-link\">weld eich ymateb</a> neu " +
                                     "<a href=\"{VIEW_CLAIMANT_HEARING_REQS}\" rel=\"noopener noreferrer\" class=\"govuk-link\" target=\"_blank\">weld y gofynion ar gyfer gwrandawiad yr hawlydd (yn agor mewn tab newydd)</a>.</p>")
            );
    }
}
