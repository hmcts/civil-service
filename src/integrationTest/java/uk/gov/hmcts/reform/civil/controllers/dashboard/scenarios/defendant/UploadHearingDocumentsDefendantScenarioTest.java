package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.defendant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant.UploadHearingDocumentsDefendantHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.dashboard.data.TaskStatus;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UploadHearingDocumentsDefendantScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private UploadHearingDocumentsDefendantHandler handler;

    @Test
    void shouldCreateUploadDocumentCaseProgresionScenario() throws Exception {

        String caseId = "1234518843299";

        CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullAdmissionSpec().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .respondent1Represented(YesOrNo.NO)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .responseClaimMediationSpecRequired(YesOrNo.NO)
            .totalClaimAmount(new BigDecimal(1000))
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "DEFENDANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("An order has been made"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">You can <a href=\"{UPLOAD_HEARING_DOCUMENTS}\" class=\"govuk-link\">" +
                        "upload and submit documents.</a> to support your defence. Follow the instructions set out in the directions order. " +
                        "You must submit all documents by ${sdoDocumentUploadRequestedDate}. " +
                        "Any documents submitted after the deadline may not be considered by the judge.</p>"
                ),
                jsonPath("$[0].titleCy").value("An order has been made"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">You can <a href=\"{UPLOAD_HEARING_DOCUMENTS}\" class=\"govuk-link\">" +
                        "upload and submit documents.</a> to support your defence. Follow the instructions set out in the directions order. " +
                        "You must submit all documents by ${sdoDocumentUploadRequestedDate}. " +
                        "Any documents submitted after the deadline may not be considered by the judge.</p>"
                )
            );
    }
}
