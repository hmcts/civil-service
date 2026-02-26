package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant.DefendantResponseClaimantNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ChangeOfRepresentation;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.dashboard.data.TaskStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.SMALL_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;

public class DefendantResponseFullPartAdmitAfterNocClaimantScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private DefendantResponseClaimantNotificationHandler handler;

    @Test
    void should_create_part_admit_defendant_response_scenario() throws Exception {

        String caseId = "11234949494";
        LocalDate responseDeadline = OffsetDateTime.now().toLocalDate();
        CaseData caseData = CaseDataBuilder.builder().atStateRespondentPartAdmissionSpec().build()
            .toBuilder()
            .applicant1ResponseDeadline(LocalDateTime.of(2025, 5, 13, 16, 0))
            .ccdState(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM)
            .changeOfRepresentation(new ChangeOfRepresentation())
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .applicant1Represented(NO)
            .respondent1(Party.builder().type(Party.Type.INDIVIDUAL).individualFirstName("defendant").individualLastName("person").build())
            .responseClaimTrack(SMALL_CLAIM.name())
            .respondToClaim(new RespondToClaim()
                                .setHowMuchWasPaid(new BigDecimal(100000))
                                )
            .totalClaimAmount(new BigDecimal(1000))
            .build();
        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("defendant person has asked for a legal representative to act on their behalf"),
                jsonPath("$[0].titleCy").value("Mae defendant person wedi gofyn i gynrychiolydd cyfreithiol weithredu ar eu rhan"),
                jsonPath("$[0].descriptionEn").value("<p class=\"govuk-body\">defendant person has asked for a legal representative to act on their behalf. From now on you will need to liaise with their representative.<br><a href=\"{VIEW_INFO_ABOUT_DEFENDANT}\" class=\"govuk-link\">View the contact details of the defendant’s legal representative.</a><br>This claim will now move offline and you must submit your intention to proceed by using form <a href=\"https://www.gov.uk/government/publications/form-n225-request-for-judgment-and-reply-to-admission-specified-amount\" target=\"_blank\" class=\"govuk-link\">N225</a> (for a full admission) or <a href=\"https://www.gov.uk/government/publications/form-n225a-notice-of-part-admission-specified-amount\" target=\"_blank\" class=\"govuk-link\">N225A</a> (for a partial admission) by 13 May 2025.</p>"),
                jsonPath("$[0].descriptionCy").value("<p class=\"govuk-body\">Mae defendant person wedi gofyn i gynrychiolydd cyfreithiol weithredu ar eu rhan.  O hyn ymlaen bydd angen i chi gysylltu â'u cynrychiolydd.<br><a href=\"{VIEW_INFO_ABOUT_DEFENDANT}\" class=\"govuk-link\">Gweld manylion cyswllt cynrychiolydd cyfreithiol y diffynnydd.</a><br>Bydd yr hawliad hwn bellach yn symud all-lein ac mae'n rhaid i chi gyflwyno eich bwriad i fwrw ymlaen trwy ddefnyddio ffurflen <a href=\"https://www.gov.uk/government/publications/form-n225-request-for-judgment-and-reply-to-admission-specified-amount\" target=\"_blank\" class=\"govuk-link\">N225</a> (ar gyfer addefiad llawn) neu <a href=\"https://www.gov.uk/government/publications/form-n225a-notice-of-part-admission-specified-amount\" target=\"_blank\" class=\"govuk-link\">N225A</a> (ar gyfer addefiad rhannol) erbyn 13 Mai 2025.</p>")
            );

        //Verify task Item is created
        doGet(BEARER_TOKEN, GET_TASKS_ITEMS_URL, caseId, "CLAIMANT")
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].reference").value(caseId),
                jsonPath("$[0].taskNameEn").value(
                    "<a href={VIEW_RESPONSE_TO_CLAIM} rel=\"noopener noreferrer\" class=\"govuk-link\">View the response to the claim</a>"),
                jsonPath("$[0].currentStatusEn").value(TaskStatus.AVAILABLE.getName()),
                jsonPath("$[1].reference").value(caseId),
                jsonPath("$[1].taskNameEn").value(
                    "<a>Contact the court to request a change to my case</a>"),
                jsonPath("$[1].currentStatusEn").value(TaskStatus.INACTIVE.getName()),
                jsonPath("$[2].reference").value(caseId),
                jsonPath("$[2].taskNameEn").value(
                    "<a>View applications</a>"),
                jsonPath("$[2].currentStatusEn").value(TaskStatus.INACTIVE.getName())
            );
    }
}
