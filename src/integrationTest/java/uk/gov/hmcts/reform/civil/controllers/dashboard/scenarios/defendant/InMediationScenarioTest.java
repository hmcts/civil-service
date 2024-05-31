package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.defendant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant.ClaimantResponseDefendantNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.civil.enums.CaseState.IN_MEDIATION;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;

public class InMediationScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    ClaimantResponseDefendantNotificationHandler handler;

    @Test
    void should_create_in_mediation_scenario() throws Exception {

        String caseId = "4321234";
        LocalDate responseDeadline = OffsetDateTime.now().toLocalDate();
        String claimantFirstName = "Clay";
        String claimantLastName = "Mint";
        CaseData caseData = CaseDataBuilder.builder().atStateClaimantFullDefence().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .applicant1(Party.builder()
                            .individualFirstName(claimantFirstName)
                            .individualLastName(claimantLastName)
                            .type(Party.Type.INDIVIDUAL)
                            .build())
            .respondent1Represented(NO)
            .ccdState(IN_MEDIATION)
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "DEFENDANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value(claimantFirstName + " " + claimantLastName + " rejected your response"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">Your case will be referred for mediation. " +
                        "Your mediation appointment will be arranged within 28 days.</p><p class=\"govuk-body\"><a href=\"{MEDIATION}\" rel=\"noopener noreferrer\" class=\"govuk-link\" target=\"_blank\">" +
                        "Find out more about how mediation works (opens in a new tab)</a>.<p/>" +
                        "<p class=\"govuk-body\">They've also sent us their hearing requirements. <a href=\"{VIEW_CLAIMANT_HEARING_REQS}\" rel=\"noopener noreferrer\" class=\"govuk-link\" target=\"_blank\">" +
                        "You can view their hearing requirements here (opens in new tab)</a>.</p>"
                )
            );
    }
}
