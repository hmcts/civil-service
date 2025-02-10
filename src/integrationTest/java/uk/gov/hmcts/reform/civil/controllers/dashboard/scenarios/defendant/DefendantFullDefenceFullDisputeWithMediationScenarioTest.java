package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.defendant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant.DefendantResponseDefendantNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.dashboard.data.TaskStatus;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec.DISPUTES_THE_CLAIM;

public class DefendantFullDefenceFullDisputeWithMediationScenarioTest extends  DashboardBaseIntegrationTest {

    @Autowired
    private DefendantResponseDefendantNotificationHandler handler;

    @Test
    @DirtiesContext
    void should_create_scenario_for_fullDefence_DisputeAll_With_Mediation() throws Exception {

        String caseId = "720111";
        CaseData caseData = CaseDataBuilder.builder().atStateRespondentPartAdmissionSpec().build()
                .toBuilder()
                .legacyCaseReference("reference")
                .ccdCaseReference(Long.valueOf(caseId))
                .respondent1Represented(YesOrNo.NO)
                .applicant1(Party.builder().type(Party.Type.INDIVIDUAL)
                        .individualFirstName("Claimant")
                        .individualLastName("John")
                        .build())
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
                .defenceRouteRequired(DISPUTES_THE_CLAIM)
                .responseClaimMediationSpecRequired(YesOrNo.YES)
                .responseClaimTrack("SMALL_CLAIM")
                .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "DEFENDANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Response to the claim"),
                jsonPath("$[0].descriptionEn")
                    .value("<p class=\"govuk-body\">You have rejected the claim and suggested mediation. Claimant John can reject or agree to mediation." +
                              " The court will contact you when they respond.</p>"
                               + "<p class=\"govuk-body\"><a href=\"{VIEW_RESPONSE_TO_CLAIM}\" class=\"govuk-link\">View your response</a></p>"),
                jsonPath("$[0].titleCy").value("Ymateb i’r hawliad"),
                jsonPath("$[0].descriptionCy")
                    .value("<p class=\"govuk-body\">Rydych wedi gwrthod yr hawliad ac wedi awgrymu cyfryngu. Gall Claimant John wrthod neu gytuno i gyfryngu." +
                        " Bydd y llys yn cysylltu â chi pan fyddant yn ymateb.</p>"
                         + "<p class=\"govuk-body\"><a href=\"{VIEW_RESPONSE_TO_CLAIM}\" class=\"govuk-link\">Gweld eich ymateb</a></p>")

            );

        //Verify task Item is created
        doGet(BEARER_TOKEN, GET_TASKS_ITEMS_URL, caseId, "DEFENDANT")
                .andExpectAll(
                        status().is(HttpStatus.OK.value()),
                        jsonPath("$[0].reference").value(caseId.toString()),
                        jsonPath("$[0].taskNameEn").value(
                                "<a href={VIEW_RESPONSE_TO_CLAIM} class=\"govuk-link\">View the response to the claim</a>"),
                        jsonPath("$[0].currentStatusEn").value(TaskStatus.AVAILABLE.getName()),
                        jsonPath("$[0].taskNameCy").value(
                                "<a href={VIEW_RESPONSE_TO_CLAIM} class=\"govuk-link\">Gweld yr ymateb i'r hawliad</a>"),
                        jsonPath("$[0].currentStatusCy").value(TaskStatus.AVAILABLE.getWelshName()));
    }
}
