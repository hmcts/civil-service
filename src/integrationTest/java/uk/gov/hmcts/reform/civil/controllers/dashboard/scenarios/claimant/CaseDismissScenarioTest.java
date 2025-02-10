package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant.CaseDismissClaimantDashboardNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CaseDismissScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private CaseDismissClaimantDashboardNotificationHandler handler;

    @Test
    @DirtiesContext
    void should_create_more_time_requested_scenario() throws Exception {
        Mockito.when(featureToggleService.isCaseEventsEnabled()).thenReturn(true);
        String caseId = "12349";
        CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullAdmissionSpec().build()
            .toBuilder()
            .respondent1ResponseDeadline(LocalDateTime.of(2024, 4, 1, 12, 0))
            .ccdCaseReference(Long.valueOf(caseId))
            .applicant1Represented(YesOrNo.NO)
            .totalClaimAmount(new BigDecimal(5000))
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("The case has been closed"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">The case has been closed as a result of a judge’s order.<br>" +
                        "You can view the case summary but not the case details. You cannot make any changes to a closed case.</p>"),
                jsonPath("$[0].titleCy").value("Mae’r achos wedi’i gau"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Mae’r achos wedi’i gau o ganlyniad i orchymyn  barnwr.<br>" +
                        "Gallwch weld y crynodeb o’r achos ond nid manylion yr achos. Ni allwch wneud unrhyw newidiadau i achos sydd wedi cau.</p>")
            );
    }
}
