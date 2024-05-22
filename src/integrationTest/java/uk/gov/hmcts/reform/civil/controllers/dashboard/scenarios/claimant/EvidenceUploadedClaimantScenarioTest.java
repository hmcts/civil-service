package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.sdo.ClaimsTrack;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderType;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant.EvidenceUploadedClaimantNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;

public class EvidenceUploadedClaimantScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private EvidenceUploadedClaimantNotificationHandler handler;

    @Test
    void should_enable_view_documents_task_list_when_claimant_uploads_document_scenario() throws Exception {

        String caseId = "14323241";
        LocalDate hearingDueDate = LocalDate.now().minusDays(1);
        CaseData caseData = CaseDataBuilder.builder().atStateHearingFeeDueUnpaid().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .applicant1Represented(NO)
            .caseDocumentUploadDate(LocalDateTime.now())
            .drawDirectionsOrderRequired(YesOrNo.YES)
            .drawDirectionsOrderSmallClaims(NO)
            .claimsTrack(ClaimsTrack.fastTrack)
            .orderType(OrderType.DECIDE_DAMAGES)
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Task List

        doGet(BEARER_TOKEN, GET_TASKS_ITEMS_URL, caseId, "CLAIMANT")
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].reference").value(caseId.toString()),
                jsonPath("$[0].taskNameEn").value("<a href=\"{VIEW_EVIDENCE_UPLOAD_DOCUMENTS}\" class=\"govuk-link\">View documents</a>"),
                jsonPath("$[0].currentStatusEn").value("Available"),
                jsonPath("$[0].categoryEn").value("Hearing"),
                jsonPath("$[0].role").value("CLAIMANT"),
                jsonPath("$[0].taskOrder").value("11"),
                jsonPath("$[0].taskNameCy").value("<a href=\"{VIEW_EVIDENCE_UPLOAD_DOCUMENTS}\" class=\"govuk-link\">View documents</a>"),
                jsonPath("$[0].currentStatusCy").value("Ar gael"),
                jsonPath("$[0].categoryCy").value("Hearing")
            );
    }

    @Test
    void should_enable_view_documents_task_list_when_defendant_uploads_document_scenario() throws Exception {

        String caseId = "14323241";
        LocalDate hearingDueDate = LocalDate.now().minusDays(1);
        CaseData caseData = CaseDataBuilder.builder().atStateHearingFeeDueUnpaid().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .applicant1Represented(NO)
            .drawDirectionsOrderRequired(YesOrNo.YES)
            .drawDirectionsOrderSmallClaims(NO)
            .claimsTrack(ClaimsTrack.fastTrack)
            .orderType(OrderType.DECIDE_DAMAGES)
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Task List

        doGet(BEARER_TOKEN, GET_TASKS_ITEMS_URL, caseId, "CLAIMANT")
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].reference").value(caseId.toString()),
                jsonPath("$[0].taskNameEn").value("<a href=\"{VIEW_EVIDENCE_UPLOAD_DOCUMENTS}\" class=\"govuk-link\">View documents</a>"),
                jsonPath("$[0].currentStatusEn").value("Available"),
                jsonPath("$[0].categoryEn").value("Hearing"),
                jsonPath("$[0].role").value("CLAIMANT"),
                jsonPath("$[0].taskOrder").value("11"),
                jsonPath("$[0].taskNameCy").value("<a href=\"{VIEW_EVIDENCE_UPLOAD_DOCUMENTS}\" class=\"govuk-link\">View documents</a>"),
                jsonPath("$[0].currentStatusCy").value("Ar gael"),
                jsonPath("$[0].categoryCy").value("Hearing")
            );
    }

}
