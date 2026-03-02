package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.defendant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant.ClaimantResponseDefendantNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.FULL_DEFENCE;

public class JudicialReferralFullDefencePartAdmitPaidPartialScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private ClaimantResponseDefendantNotificationHandler handler;

    @Test
    void should_create_scenario_for_judicial_referral_full_defence_or_part_admit_paid_partial_amount() throws Exception {

        String caseId = "90123456782";
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued1v1LiP().build().toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .ccdState(CaseState.JUDICIAL_REFERRAL)
            .respondent1ClaimResponseTypeForSpec(FULL_DEFENCE)
            .respondToClaim(new RespondToClaim().setHowMuchWasPaid(new BigDecimal("300000")))
            .applicant1PartAdmitConfirmAmountPaidSpec(YesOrNo.YES)
            .responseClaimMediationSpecRequired(YesOrNo.NO)
            .specRespondent1Represented(YesOrNo.NO)
            .respondent1Represented(YesOrNo.NO)
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "DEFENDANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Wait for the court to review the case"),
                jsonPath("$[0].titleCy").value("Aros i’r llys adolygu’r achos"),
                jsonPath("$[0].descriptionEn")
                    .value("<p class=\"govuk-body\">Mr. John Rambo wants to proceed with the claim."
                               + " They rejected your admission of £3000 although they accept you have already paid it."
                               + " The case will be referred to a judge who will decide what should happen next.</p>"
                               + "<p class=\"govuk-body\">You can <a href=\"{VIEW_RESPONSE_TO_CLAIM}\"  rel=\"noopener noreferrer\" class=\"govuk-link\">view your response</a> or "
                               + "<a target=\"_blank\" href=\"{VIEW_CLAIMANT_HEARING_REQS}\"  rel=\"noopener noreferrer\" "
                               + "class=\"govuk-link\">view the claimant's hearing requirements (opens in a new tab)</a>.</p>"),
                jsonPath("$[0].descriptionCy")
                    .value("<p class=\"govuk-body\">Mae Mr. John Rambo eisiau parhau â’r hawliad."
                               + " Maent wedi gwrthod eich addefiad o £3000 ond yn derbyn eich bod wedi’i dalu’n barod."
                               + " Bydd yr achos yn cael ei gyfeirio at farnwr a fydd yn penderfynu beth ddylai ddigwydd nesaf.</p>"
                               + "<p class=\"govuk-body\">Gallwch <a href=\"{VIEW_RESPONSE_TO_CLAIM}\"  rel=\"noopener noreferrer\" class=\"govuk-link\">weld eich ymateb</a> neu "
                               + "<a target=\"_blank\" href=\"{VIEW_CLAIMANT_HEARING_REQS}\"  rel=\"noopener noreferrer\" "
                               + "class=\"govuk-link\">weld y gofynion ar gyfer gwrandawiad yr hawlydd (yn agor mewn tab newydd)</a>.</p>")
            );
    }
}
