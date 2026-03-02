package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant.DefendantResponseClaimantNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.dashboard.data.TaskStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;

public class DefendantResponsePartAdmitPayImmediatelyClaimantScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private DefendantResponseClaimantNotificationHandler handler;

    @Test
    void should_create_part_admit_defendant_response_scenario() throws Exception {

        String caseId = "11234949494";
        LocalDate responseDeadline = OffsetDateTime.now().toLocalDate();
        CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .respondent1ResponseDeadline(responseDeadline.atStartOfDay())
            .respondToClaimAdmitPartLRspec(new RespondToClaimAdmitPartLRspec().setWhenWillThisAmountBePaid(
                LocalDate.of(2024, 3, 18)
            ))
            .respondent1(Party.builder().type(Party.Type.INDIVIDUAL)
                    .individualFirstName("James")
                    .individualLastName("John")
                    .build())
            .claimFee(new Fee().setCalculatedAmountInPence(BigDecimal.valueOf(30000)))
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
            .totalClaimAmount(BigDecimal.valueOf(300))
            .respondToAdmittedClaimOwingAmountPounds(BigDecimal.valueOf(300))
            .applicant1Represented(NO)
            .responseClaimTrack("SMALL_CLAIM")
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Response to the claim"),
                jsonPath("$[0].titleCy").value("Ymateb i’r hawliad"),
                jsonPath("$[0].descriptionEn").value("<p class=\"govuk-body\">James John has offered to pay £300, plus the claim fee, immediately.</p><p class=\"govuk-body\">If you accept, the payment must be received in your account within 5 working days, if not you can request a County Court Judgment.</p><p class=\"govuk-body\"><a href=\"{CLAIMANT_RESPONSE_TASK_LIST}\" class=\"govuk-link\">View and respond</a></p>"),
                jsonPath("$[0].descriptionCy").value("<p class=\"govuk-body\">Mae James John wedi cynnig talu £300, ynghyd â ffi’r hawliad, ar unwaith.</p><p class=\"govuk-body\">Os ydych yn derbyn, mae’n rhaid derbyn y taliad yn eich cyfrif o fewn 5 diwrnod gwaith, os na, gallwch ofyn am Ddyfarniad Llys Sirol.</p><p class=\"govuk-body\"><a href=\"{CLAIMANT_RESPONSE_TASK_LIST}\" class=\"govuk-link\">Gweld ac ymateb</a></p>")
            );

        //Verify task Item is created
        doGet(BEARER_TOKEN, GET_TASKS_ITEMS_URL, caseId, "CLAIMANT")
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].reference").value(caseId),
                jsonPath("$[0].taskNameEn").value(
                    "<a href={VIEW_RESPONSE_TO_CLAIM} rel=\"noopener noreferrer\" class=\"govuk-link\">View the response to the claim</a>"),
                jsonPath("$[0].currentStatusEn").value(TaskStatus.AVAILABLE.getName())
            );
    }
}
