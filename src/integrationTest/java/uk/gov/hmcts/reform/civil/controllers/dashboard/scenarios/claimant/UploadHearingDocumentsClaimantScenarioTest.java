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
                jsonPath("$[0].titleEn").value("Upload documents"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">You can <a href=\"{UPLOAD_HEARING_DOCUMENTS}\" " +
                        "class=\"govuk-link\">upload and submit documents</a> to support your claim. Follow the " +
                        "instructions set out in the <a href=\"{VIEW_ORDERS_AND_NOTICES}\" class=\"govuk-link\">directions order</a>. You must submit all documents by " +
                        "${sdoDocumentUploadRequestedDateEn}. Any documents submitted after the deadline may not be " +
                        "considered by the judge.</p>"
                ),
                jsonPath("$[0].titleCy").value("Llwytho dogfennau"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Gallwch <a href=\"{UPLOAD_HEARING_DOCUMENTS}\" " +
                        "class=\"govuk-link\">lwytho a chyflwyno dogfennau</a> i gefnogi eich hawliad. Dilynwch y cyfarwyddiadau a nodir yn y " +
                        "<a href=\"{VIEW_ORDERS_AND_NOTICES}\" class=\"govuk-link\">gorchymyn cyfarwyddiadau</a>. Rhaid i chi gyflwyno’r holl ddogfennau erbyn " +
                        "${sdoDocumentUploadRequestedDateCy}. Mae'n bosib na fydd y barnwr yn ystyried unrhyw ddogfennau a gyflwynir ar ôl y dyddiad hwn.</p>"
                )
            );
    }
}
