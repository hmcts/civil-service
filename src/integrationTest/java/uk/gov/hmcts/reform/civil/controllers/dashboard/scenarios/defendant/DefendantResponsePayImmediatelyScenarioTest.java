package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.defendant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant.DefendantResponseDefendantNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.utils.DateUtils;
import uk.gov.hmcts.reform.dashboard.data.TaskStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DefendantResponsePayImmediatelyScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private DefendantResponseDefendantNotificationHandler handler;

    @Test
    @DirtiesContext
    void should_create_part_admit_pay_immediately_scenario() throws Exception {

        String caseId = "123451432";
        LocalDate responseDeadline = OffsetDateTime.now().toLocalDate();

        CaseData caseData = CaseDataBuilder.builder().atStateRespondentPartAdmissionSpec().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .respondent1Represented(YesOrNo.NO)
            .applicant1(Party.builder().type(Party.Type.INDIVIDUAL)
                    .individualFirstName("Claimant")
                    .individualLastName("John")
                    .build())
            .respondToClaimAdmitPartLRspec(RespondToClaimAdmitPartLRspec
                                               .builder()
                                               .whenWillThisAmountBePaid(responseDeadline)
                                               .build())
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
            .respondToAdmittedClaimOwingAmountPounds(new BigDecimal(1000))
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "DEFENDANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Response to the claim"),
                jsonPath("$[0].titleCy").value("Ymateb i’r hawliad"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">You have offered to pay £1000 by " +
                        DateUtils.formatDate(responseDeadline) + ". " +
                        "The payment must be received in Claimant John's account by then, if not they can request a county court judgment.</p>" +
                        "<p class=\"govuk-body\"><a href=\"{VIEW_RESPONSE_TO_CLAIM}\" class=\"govuk-link\">View your response</a></p>"
                ),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Rydych wedi cynnig talu £1000 erbyn " +
                        DateUtils.formatDateInWelsh(responseDeadline) + ". " +
                        "Rhaid i’r taliad fod yng nghyfrif Claimant John erbyn y dyddiad hwnnw. Os nad yw, yna gallant wneud cais am ddyfarniad llys sirol.</p>" +
                        "<p class=\"govuk-body\"><a href=\"{VIEW_RESPONSE_TO_CLAIM}\" class=\"govuk-link\">Gweld eich ymateb</a></p>"
                )

            );

        //Verify task Item is created
        doGet(BEARER_TOKEN, GET_TASKS_ITEMS_URL, caseId, "DEFENDANT")
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].reference").value(caseId.toString()),
                jsonPath("$[0].taskNameEn").value(
                    "<a href={VIEW_RESPONSE_TO_CLAIM} class=\"govuk-link\">View the response to the claim</a>"),
                jsonPath("$[0].currentStatusEn").value(TaskStatus.AVAILABLE.getName())
            );
    }

    @Test
    @DirtiesContext
    void should_create_full_admit_pay_immediately_scenario() throws Exception {

        String caseId = "123499";
        LocalDate admitPaymentDeadline = OffsetDateTime.now().toLocalDate();
        CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullAdmissionSpec().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .respondent1Represented(YesOrNo.NO)
            .applicant1(Party.builder().type(Party.Type.INDIVIDUAL)
                    .individualFirstName("Claimant")
                    .individualLastName("John")
                    .build())
            .respondToClaimAdmitPartLRspec(RespondToClaimAdmitPartLRspec
                                               .builder()
                                               .whenWillThisAmountBePaid(admitPaymentDeadline)
                                               .build())
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
            .totalClaimAmount(new BigDecimal(1000))
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "DEFENDANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Response to the claim"),
                jsonPath("$[0].titleCy").value("Ymateb i’r hawliad"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">You have offered to pay £1001 by " +
                        DateUtils.formatDate(admitPaymentDeadline) + ". " +
                        "The payment must be received in Claimant John's account by then, if not they can request a county court judgment.</p>" +
                        "<p class=\"govuk-body\"><a href=\"{VIEW_RESPONSE_TO_CLAIM}\" class=\"govuk-link\">View your response</a></p>"
                ),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Rydych wedi cynnig talu £1001 erbyn " +
                        DateUtils.formatDateInWelsh(admitPaymentDeadline) + ". " +
                        "Rhaid i’r taliad fod yng nghyfrif Claimant John erbyn y dyddiad hwnnw. Os nad yw, yna gallant wneud cais am ddyfarniad llys sirol.</p>" +
                        "<p class=\"govuk-body\"><a href=\"{VIEW_RESPONSE_TO_CLAIM}\" class=\"govuk-link\">Gweld eich ymateb</a></p>"
                )
            );

        //Verify task Item is created
        doGet(BEARER_TOKEN, GET_TASKS_ITEMS_URL, caseId, "DEFENDANT")
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].reference").value(caseId.toString()),
                jsonPath("$[0].taskNameEn").value(
                    "<a href={VIEW_RESPONSE_TO_CLAIM} class=\"govuk-link\">View the response to the claim</a>"),
                jsonPath("$[0].currentStatusEn").value(TaskStatus.AVAILABLE.getName())
            );
    }
}
