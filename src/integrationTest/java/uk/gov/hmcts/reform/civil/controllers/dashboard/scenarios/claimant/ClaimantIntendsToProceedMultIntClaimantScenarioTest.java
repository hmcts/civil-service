package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant.ClaimantResponseNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ClaimantIntendsToProceedMultIntClaimantScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private ClaimantResponseNotificationHandler handler;

    @Test
    void should_create_claimant_intent_multi_int_claimant_int_track() throws Exception {

        String caseId = "1674364636586679";

        CaseData caseData = CaseDataBuilder.builder().atStateRespondentPartAdmissionSpec().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .applicant1Represented(YesOrNo.NO)
            .ccdState(CaseState.AWAITING_APPLICANT_INTENTION)
            .responseClaimTrack(AllocatedTrack.INTERMEDIATE_CLAIM.name())
            .build();

        when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Wait for the court to review the case"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">You have responded to Mr. Sole Trader, the court will now review the case, You will be contacted if a hearing is needed in this case.</p><p class=\"govuk-body\"><a href=\"{VIEW_RESPONSE_TO_CLAIM}\"  rel=\"noopener noreferrer\" class=\"govuk-link\">View the Defendant's response</a></p>"
                ),
                jsonPath("$[0].titleCy").value("Aros i'r llys adolygu'r achos"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Rydych wedi ymateb i Mr. Sole Trader, bydd y llys nawr yn adolygu'r achos. Cysylltir â chi os oes angen gwrandawiad yn yr achos hwn.</p><p class=\"govuk-body\"><a href=\"{VIEW_RESPONSE_TO_CLAIM}\"  rel=\"noopener noreferrer\" class=\"govuk-link\">Gweld ymateb y Diffynnydd</a></p>")
            );
    }

    @Test
    void should_create_claimant_intent_multi_int_claimant_multi_track() throws Exception {

        String caseId = "1674364636586679";

        CaseData caseData = CaseDataBuilder.builder().atStateRespondentPartAdmissionSpec().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .applicant1Represented(YesOrNo.NO)
            .ccdState(CaseState.AWAITING_APPLICANT_INTENTION)
            .responseClaimTrack(AllocatedTrack.MULTI_CLAIM.name())
            .build();

        when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Wait for the court to review the case"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">You have responded to Mr. Sole Trader, the court will now review the case, You will be contacted if a hearing is needed in this case.</p><p class=\"govuk-body\"><a href=\"{VIEW_RESPONSE_TO_CLAIM}\"  rel=\"noopener noreferrer\" class=\"govuk-link\">View the Defendant's response</a></p>"
                ),
                jsonPath("$[0].titleCy").value("Aros i'r llys adolygu'r achos"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Rydych wedi ymateb i Mr. Sole Trader, bydd y llys nawr yn adolygu'r achos. Cysylltir â chi os oes angen gwrandawiad yn yr achos hwn.</p><p class=\"govuk-body\"><a href=\"{VIEW_RESPONSE_TO_CLAIM}\"  rel=\"noopener noreferrer\" class=\"govuk-link\">Gweld ymateb y Diffynnydd</a></p>")
            );
    }
}
