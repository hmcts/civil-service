package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant.DefendantResponseClaimantNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.utils.DateUtils;
import uk.gov.hmcts.reform.dashboard.data.TaskStatus;

public class DefendantFullDefenceFullDisputeMediationScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private DefendantResponseClaimantNotificationHandler handler;

    @Test
    void should_create_full_defence_pay_already_partial_claimant_scenario() throws Exception {

        String caseId = "13165";
        LocalDate paymentDate = OffsetDateTime.now().toLocalDate().plusDays(120);
        CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefenceSpec().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .applicant1Represented(YesOrNo.NO)
            .respondent1(Party.builder().type(Party.Type.INDIVIDUAL)
                    .individualFirstName("James")
                    .individualLastName("John")
                    .build())
            .responseClaimTrack("SMALL_CLAIM")
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .defenceRouteRequired(SpecJourneyConstantLRSpec.DISPUTES_THE_CLAIM)
            .responseClaimMediationSpecRequired(YesOrNo.YES)
            .responseClaimTrack(AllocatedTrack.SMALL_CLAIM.name())
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Response to the claim"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">James John has rejected the claim and suggested mediation. You can reject or agree to mediation." +
                        " You need to respond by " + DateUtils.formatDate(paymentDate) + ".</p>" +
                        "<p class=\"govuk-body\"><a href=\"{CLAIMANT_RESPONSE_TASK_LIST}\" rel=\"noopener noreferrer\" class=\"govuk-link\">View and respond</a></p>"
                ),
                jsonPath("$[0].titleCy").value("Ymateb i’r hawliad"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Mae James John wedi gwrthod yr hawliad ac wedi awgrymu cyfryngu. Gallwch wrthod neu gytuno i gyfryngu." +
                        " Mae angen i chi ymateb erbyn " + DateUtils.formatDateInWelsh(paymentDate) + ".</p>" +
                        "<p class=\"govuk-body\"><a href=\"{CLAIMANT_RESPONSE_TASK_LIST}\" rel=\"noopener noreferrer\" class=\"govuk-link\">Gweld ac ymateb</a></p>"
                )
            );

        //Verify task Item is created
        doGet(BEARER_TOKEN, GET_TASKS_ITEMS_URL, caseId, "CLAIMANT")
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].reference").value(caseId.toString()),
                jsonPath("$[0].taskNameEn").value(
                    "<a href={VIEW_RESPONSE_TO_CLAIM} class=\"govuk-link\">View the response to the claim</a>"),
                jsonPath("$[0].currentStatusEn").value(TaskStatus.AVAILABLE.getName())
            );
    }
}
