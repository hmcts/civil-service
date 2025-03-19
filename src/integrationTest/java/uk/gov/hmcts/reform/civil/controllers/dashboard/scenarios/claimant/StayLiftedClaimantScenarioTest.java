package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant.StayLiftedClaimantNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.dashboard.data.TaskStatus;

import java.time.LocalDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class StayLiftedClaimantScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private StayLiftedClaimantNotificationHandler handler;

    @Test
    void should_create_stay_lifted_claimant_scenario() throws Exception {

        String caseId = "7546454456455896";

        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued1v1LiP().build().toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .ccdState(CaseState.CASE_PROGRESSION)
            .preStayState(CaseState.HEARING_READINESS.toString())
            .caseDocumentUploadDate(LocalDateTime.now())
            .applicant1Represented(YesOrNo.NO)
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("The stay has been lifted"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">The stay of these proceedings has been lifted.</p>"),
                jsonPath("$[0].titleCy").value("Mae'r ataliad wedi'i godi"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Mae'r ataliad ar gyfer yr achos hwn wedi'i godi.</p>")
            );

        doGet(BEARER_TOKEN, GET_TASKS_ITEMS_URL, caseId, "CLAIMANT")
            .andExpect(status().isOk()).andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].reference").value(caseId),
                jsonPath("$[0].taskNameEn").value("<a>View the hearing</a>"),
                jsonPath("$[0].taskNameCy").value("<a>Gweld y gwrandawiad</a>"),
                jsonPath("$[0].currentStatusEn").value(TaskStatus.NOT_AVAILABLE_YET.getName()),
                jsonPath("$[0].currentStatusCy").value(TaskStatus.NOT_AVAILABLE_YET.getWelshName()),
                jsonPath("$[1].reference").value(caseId),
                jsonPath("$[1].taskNameEn").value("<a>Pay the hearing fee</a>"),
                jsonPath("$[1].taskNameCy").value("<a>Talu ffi'r gwrandawiad</a>"),
                jsonPath("$[1].currentStatusEn").value(TaskStatus.NOT_AVAILABLE_YET.getName()),
                jsonPath("$[1].currentStatusCy").value(TaskStatus.NOT_AVAILABLE_YET.getWelshName()),
                jsonPath("$[2].reference").value(caseId),
                jsonPath("$[2].taskNameEn").value("<a href=\"{UPLOAD_HEARING_DOCUMENTS}\" class=\"govuk-link\">Upload hearing documents</a>"),
                jsonPath("$[2].taskNameCy").value("<a href=\"{UPLOAD_HEARING_DOCUMENTS}\" class=\"govuk-link\">Llwytho dogfennau'r gwrandawiad</a>"),
                jsonPath("$[2].currentStatusEn").value(TaskStatus.IN_PROGRESS.getName()),
                jsonPath("$[2].currentStatusCy").value(TaskStatus.IN_PROGRESS.getWelshName()),
                jsonPath("$[3].reference").value(caseId),
                jsonPath("$[3].taskNameEn").value("<a href=\"{VIEW_EVIDENCE_UPLOAD_DOCUMENTS}\" class=\"govuk-link\">View documents</a>"),
                jsonPath("$[3].taskNameCy").value("<a href=\"{VIEW_EVIDENCE_UPLOAD_DOCUMENTS}\" class=\"govuk-link\">Gweld y dogfennau</a>"),
                jsonPath("$[3].currentStatusEn").value(TaskStatus.AVAILABLE.getName()),
                jsonPath("$[3].currentStatusCy").value(TaskStatus.AVAILABLE.getWelshName()),
                jsonPath("$[4].reference").value(caseId),
                jsonPath("$[4].taskNameEn").value("<a>Add the trial arrangements</a>"),
                jsonPath("$[4].taskNameCy").value("<a>Ychwanegu trefniadau'r treial</a>"),
                jsonPath("$[4].currentStatusEn").value(TaskStatus.NOT_AVAILABLE_YET.getName()),
                jsonPath("$[4].currentStatusCy").value(TaskStatus.NOT_AVAILABLE_YET.getWelshName()),
                jsonPath("$[5].reference").value(caseId),
                jsonPath("$[5].taskNameEn").value("<a>View the bundle</a>"),
                jsonPath("$[5].taskNameCy").value("<a>Gweld y bwndel</a>"),
                jsonPath("$[5].currentStatusEn").value(TaskStatus.NOT_AVAILABLE_YET.getName()),
                jsonPath("$[5].currentStatusCy").value(TaskStatus.NOT_AVAILABLE_YET.getWelshName())
            );
    }
}
