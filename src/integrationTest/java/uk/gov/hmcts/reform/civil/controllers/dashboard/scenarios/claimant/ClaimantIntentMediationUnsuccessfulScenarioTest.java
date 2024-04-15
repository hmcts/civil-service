package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.springframework.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulReason;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant.ClaimantIntentMediationUnsuccessfulHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Mediation;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulReason.APPOINTMENT_NO_AGREEMENT;
public class ClaimantIntentMediationUnsuccessfulScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private ClaimantIntentMediationUnsuccessfulHandler handler;

    @Test
    void shouldCreateMediationUnsuccessful() throws Exception {

        String caseId = "323491";
        Party respondent1 = new Party();
        respondent1.toBuilder().partyName("John Doe").build();
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued1v1LiP().build()
            .toBuilder()
            .ccdCaseReference(Long.valueOf(323491))
            .applicant1Represented(YesOrNo.NO)
            .respondent1(Party.builder().individualFirstName("John").individualLastName("Doe")
                             .type(Party.Type.INDIVIDUAL).build())
            .build();

        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(false);

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Mediation was unsuccessful"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">You weren't able to resolve your claim against John Doe using mediation. "
                        + "The court will review the case. We'll contact you to tell you what to do next.</p>"),
                jsonPath("$[0].titleCy").value("Mediation was unsuccessful"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">You weren't able to resolve your claim against John Doe using mediation. "
                        + "The court will review the case. We'll contact you to tell you what to do next.</p>"));

    }

    @Test
    void shouldCreateMediationUnsuccessfulForCarm() throws Exception {
        String caseId = "323491";
        Party respondent1 = new Party();
        MediationUnsuccessfulReason reason = APPOINTMENT_NO_AGREEMENT;
        respondent1.toBuilder().partyName("John Doe").build();
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued1v1LiP().build()
            .toBuilder()
            .ccdCaseReference(Long.valueOf(323491))
            .applicant1Represented(YesOrNo.NO)
            .respondent1(Party.builder().individualFirstName("John").individualLastName("Doe")
                             .type(Party.Type.INDIVIDUAL).build())
            .mediation(Mediation.builder()
                           .mediationUnsuccessfulReasonsMultiSelect(List.of(reason)).build())
            .build();

        handler.handle(callbackParams(caseData));
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Mediation was unsuccessful"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">You were not able to resolve this claim using mediation.</p> <p "
                        + "class=\"govuk-body\">This case will now be reviewed by the court.</p>"),
                jsonPath("$[0].titleCy").value("Mediation was unsuccessful"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">You were not able to resolve this claim using mediation.</p> <p "
                        + "class=\"govuk-body\">This case will now be reviewed by the court.</p>"));

    }
}
