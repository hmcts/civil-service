package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant.UploadHearingDocumentsClaimantHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.dashboard.data.TaskStatus;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UploadHearingDocumentsClaimantScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private UploadHearingDocumentsClaimantHandler handler;

    @Test
    void shouldCreateUploadDocumentCaseProgresionScenario() throws Exception {

        String caseId = "12345188432991";

        CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullAdmissionSpec().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .applicant1Represented(YesOrNo.NO)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .responseClaimMediationSpecRequired(YesOrNo.NO)
            .totalClaimAmount(new BigDecimal(1000))
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("An order has been made"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">You can <a href=\"{UPLOAD_HEARING_DOCUMENTS}\" class=\"govuk-link\">" +
                        "upload and submit documents.</a> to support your claim. Follow the instructions set out in the directions order. " +
                        "You must submit all documents by ${sdoDocumentUploadRequestedDateEn}. " +
                        "Any documents submitted after the deadline may not be considered by the judge.</p>"
                ),
                jsonPath("$[0].titleCy").value("An order has been made"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">You can <a href=\"{UPLOAD_HEARING_DOCUMENTS}\" class=\"govuk-link\">" +
                        "upload and submit documents.</a> to support your claim. Follow the instructions set out in the directions order. " +
                        "You must submit all documents by ${sdoDocumentUploadRequestedDateCy}. " +
                        "Any documents submitted after the deadline may not be considered by the judge.</p>"
                )
            );
    }
}
