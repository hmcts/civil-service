package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant.DefendantResponseWelshClaimantDashboardNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.dashboard.data.TaskStatus;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DefendantResponseWelshTranslationScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private DefendantResponseWelshClaimantDashboardNotificationHandler handler;

    @Test
    void should_create_defendant_response_claimant_dashboard_welsh_scenario() throws Exception {
        String caseId = "123452";
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued1v1LiP().build()
            .toBuilder()
            .applicant1Represented(YesOrNo.NO)
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .caseDataLiP(CaseDataLiP.builder()
                        .respondent1LiPResponse(RespondentLiPResponse.builder()
                                                    .respondent1ResponseLanguage("WELSH").build()).build())
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("The defendant's response is being translated"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">The defendant has chosen to respond to the claim in Welsh. " +
                        "Their response is paused for translation into English. We will send it to you when it has been translated.</p>"),
                jsonPath("$[0].titleCy").value("Mae ymateb y diffynnydd yn cael ei gyfieithu"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Mae'r diffynnydd wedi dewis ymateb i'r cais yn Gymraeg. " +
                        "Mae ei ymateb yn cael ei gyfieithu i'r Saesneg. Byddwn yn ei anfon atoch pan fydd wedi’i gyfieithu.</p>")
            );

        doGet(BEARER_TOKEN, GET_TASKS_ITEMS_URL, caseId, "CLAIMANT")
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].reference").value(caseId),
                jsonPath("$[0].taskNameEn").value(
                    "<a href={VIEW_RESPONSE_TO_CLAIM} class=\"govuk-link\">View the response to the claim</a>"),
                jsonPath("$[0].currentStatusEn").value(TaskStatus.AVAILABLE.getName()),
                jsonPath("$[0].taskNameCy").value(
                    "<a href={VIEW_RESPONSE_TO_CLAIM} class=\"govuk-link\">Gweld yr ymateb i'r hawliad</a>"),
                jsonPath("$[0].currentStatusCy").value(TaskStatus.AVAILABLE.getWelshName())
            );
    }

    @Test
    void should_create_defendant_response_claimant_dashboard_welsh_scenario_welsh_toggle_on() throws Exception {
        String caseId = "123452";
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued1v1LiP().build()
            .toBuilder()
            .applicant1Represented(YesOrNo.NO)
            .respondent1ResponseDeadline(LocalDateTime.MAX)
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .caseDataLiP(CaseDataLiP.builder()
                             .respondent1LiPResponse(RespondentLiPResponse.builder()
                                                         .respondent1ResponseLanguage("WELSH").build()).build())
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("The defendant's response is being translated"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">The defendant has chosen to respond to the claim in Welsh. " +
                        "Their response is paused for translation into English. We will send it to you when it has been translated.</p>"),
                jsonPath("$[0].titleCy").value("Mae ymateb y diffynnydd yn cael ei gyfieithu"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Mae'r diffynnydd wedi dewis ymateb i'r cais yn Gymraeg. " +
                        "Mae ei ymateb yn cael ei gyfieithu i'r Saesneg. Byddwn yn ei anfon atoch pan fydd wedi’i gyfieithu.</p>")
            );

        doGet(BEARER_TOKEN, GET_TASKS_ITEMS_URL, caseId, "CLAIMANT")
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].reference").doesNotExist(),
                jsonPath("$[0].taskNameEn").doesNotExist(),
                jsonPath("$[0].currentStatusEn").doesNotExist(),
                jsonPath("$[0].taskNameCy").doesNotExist(),
                jsonPath("$[0].currentStatusCy").doesNotExist()
            );
    }
}
