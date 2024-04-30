package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.defendant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant.DefendantResponseClaimantNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.FAST_CLAIM;

public class DefendantResponseFullDefenceFastTrackClaimantScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private DefendantResponseClaimantNotificationHandler handler;

    @Test
    void shouldCreateFullDefenceFastTrackResponseScenario() throws Exception {

        String caseId = "1234987";
        LocalDate responseDeadline = OffsetDateTime.now().toLocalDate();
        CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefenceSpec().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .applicant1Represented(YesOrNo.NO)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .applicant1ResponseDeadline(LocalDateTime.of(2024, 8, 6, 12, 0))
            .respondent1(Party.builder().individualFirstName("James")
                    .individualLastName("John").type(Party.Type.INDIVIDUAL).build())
            .responseClaimTrack(FAST_CLAIM.name())
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Response to the claim"),
                jsonPath("$[0].descriptionEn").value("<p class=\"govuk-body\">James John has rejected the claim.<br>You need to respond by 6 August 2024.<br><a href=\"{CLAIMANT_RESPONSE_TASK_LIST}\" class=\"govuk-link\">View and respond</a>.</p>"),
                jsonPath("$[0].descriptionCy").value("<p class=\"govuk-body\">James John has rejected the claim.<br>You need to respond by 6 Awst 2024.<br><a href=\"{CLAIMANT_RESPONSE_TASK_LIST}\" class=\"govuk-link\">View and respond</a>.</p>")
            );
    }
}
