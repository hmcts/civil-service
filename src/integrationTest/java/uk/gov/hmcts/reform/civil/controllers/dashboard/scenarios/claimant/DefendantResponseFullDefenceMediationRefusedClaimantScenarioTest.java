package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant.DefendantResponseClaimantNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.SMALL_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;

public class DefendantResponseFullDefenceMediationRefusedClaimantScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private DefendantResponseClaimantNotificationHandler handler;

    @Test
    void should_create_full_defence_mediation_refused_response_scenario() throws Exception {

        String caseId = "11234949493";
        LocalDate responseDeadline = OffsetDateTime.now().toLocalDate();
        CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .responseClaimTrack(SMALL_CLAIM.name())
            .respondent1(Party.builder().type(Party.Type.INDIVIDUAL)
                    .individualFirstName("James")
                    .individualLastName("John")
                    .build())
            .respondent1ResponseDeadline(responseDeadline.atStartOfDay())
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .defenceRouteRequired(SpecJourneyConstantLRSpec.DISPUTES_THE_CLAIM)
            .applicant1ResponseDeadline(LocalDateTime.of(2024, 3, 21, 16, 0))
            .responseClaimMediationSpecRequired(NO)
            .applicant1Represented(NO)
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Response to the claim"),
                jsonPath("$[0].titleCy").value("Ymateb i’r hawliad"),
                jsonPath("$[0].descriptionEn").value("<p class=\"govuk-body\">James John has rejected the claim and refused mediation. You need to respond by 21 March 2024.</p><p class=\"govuk-body\"><a href=\"{CLAIMANT_RESPONSE_TASK_LIST}\" class=\"govuk-link\">View and respond</a></p>"),
                jsonPath("$[0].descriptionCy").value("<p class=\"govuk-body\">Mae James John wedi gwrthod yr hawliad ac wedi gwrthod cyfryngu. Mae angen i chi ymateb erbyn 21 Mawrth 2024.</p><p class=\"govuk-body\"><a href=\"{CLAIMANT_RESPONSE_TASK_LIST}\" class=\"govuk-link\">Gweld ac ymateb</a></p>")
            );
    }
}
