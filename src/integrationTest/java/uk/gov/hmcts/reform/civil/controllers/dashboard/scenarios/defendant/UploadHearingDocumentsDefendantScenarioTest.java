package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.defendant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant.UploadHearingDocumentsDefendantHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UploadHearingDocumentsDefendantScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private UploadHearingDocumentsDefendantHandler handler;

    @Test
    @DirtiesContext
    void shouldCreateUploadDocumentCaseProgresionScenario() throws Exception {

        String caseId = "1234518843299";

        when(featureToggleService.isCaseProgressionEnabled()).thenReturn(true);
        when(featureToggleService.isCaseProgressionEnabledAndLocationWhiteListed(any())).thenReturn(true);

        DynamicListElement selectedCourt = DynamicListElement.builder()
            .code("00002").label("court 2 - 2 address - Y02 7RB").build();

        CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullAdmissionSpec().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .respondent1Represented(YesOrNo.NO)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .responseClaimMediationSpecRequired(YesOrNo.NO)
            .totalClaimAmount(new BigDecimal(1000))
            .ccdState(CaseState.CASE_PROGRESSION)
            .caseManagementLocation(CaseLocationCivil.builder().baseLocation(selectedCourt.getCode()).build())
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "DEFENDANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Upload documents"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">You can <a href=\"{UPLOAD_HEARING_DOCUMENTS}\" " +
                        "class=\"govuk-link\">upload and submit documents</a> to support your defence. Follow the " +
                        "instructions set out in the <a href=\"{VIEW_ORDERS_AND_NOTICES}\" class=\"govuk-link\">directions order</a>. Any documents submitted after the deadlines in the directions order may not be " +
                        "considered by the judge.</p>"
                ),
                jsonPath("$[0].titleCy").value("Llwytho dogfennau"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Gallwch <a href=\"{UPLOAD_HEARING_DOCUMENTS}\" " +
                        "class=\"govuk-link\">lwytho a chyflwyno dogfennau</a> i gefnogi eich amddiffyniad. Dilynwch y cyfarwyddiadau a nodir yn y " +
                        "<a href=\"{VIEW_ORDERS_AND_NOTICES}\" class=\"govuk-link\">gorchymyn cyfarwyddiadau</a>. Ni chaiff y barnwr ystyried unrhyw ddogfennau a gyflwynir ar ôl y dyddiadau cau yn y gorchymyn cyfarwyddiadau.</p>"
                )
            );
    }
}
