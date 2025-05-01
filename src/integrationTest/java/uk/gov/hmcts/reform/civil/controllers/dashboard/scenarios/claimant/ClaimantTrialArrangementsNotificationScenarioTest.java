package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.sdo.ClaimsTrack;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderType;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant.TrialArrangementsClaimantNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.dashboard.data.TaskStatus;

import java.time.LocalDate;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;

public class ClaimantTrialArrangementsNotificationScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private TrialArrangementsClaimantNotificationHandler handler;

    @Override
    @BeforeEach
    public void before() {
        when(featureToggleService.isCaseProgressionEnabled()).thenReturn(true);
    }

    @Test
    void shouldCreateAddTrialArrangementsForClaimant() throws Exception {

        String caseId = "1234987";
        CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .applicant1Represented(NO)
            .drawDirectionsOrderRequired(YesOrNo.YES)
            .drawDirectionsOrderSmallClaims(NO)
            .claimsTrack(ClaimsTrack.fastTrack)
            .orderType(OrderType.DECIDE_DAMAGES)
            .ccdCaseReference(Long.valueOf(caseId))
            .hearingDate(LocalDate.of(2024, 04, 1))
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created for CLAIMANT
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value(
                    "Confirm your trial arrangements"),
                jsonPath("$[0].descriptionEn").value(
                "<p class=\"govuk-body\">You must <a href=\"{ADD_TRIAL_ARRANGEMENTS}\" class=\"govuk-link\">confirm your trial arrangements</a> by 4 March 2024. This means that you'll need to confirm if the case is ready for trial or not. You'll also need to confirm whether circumstances have changed since you completed the directions questionnaire. Refer to the <a href=\"{VIEW_CLAIMANT_HEARING_REQS}\" rel=\"noopener noreferrer\" class=\"govuk-link\" target=\"_blank\">questionnaire you submitted</a> if you're not sure what you previously said.</p>"),
                jsonPath("$[0].titleCy").value(
                "Cadarnhau eich trefniadau treial"),
                jsonPath("$[0].descriptionCy").value(
                "<p class=\"govuk-body\">Rhaid i chi <a href=\"{ADD_TRIAL_ARRANGEMENTS}\" class=\"govuk-link\">gadarnhau eich trefniadau treial</a> erbyn 4 Mawrth 2024. Mae hyn yn golygu y bydd angen i chi gadarnhau a yw'r achos yn barod ar gyfer treial ai peidio. Bydd angen i chi hefyd gadarnhau a yw'r amgylchiadau wedi newid ers i chi gwblhau'r holiadur cyfarwyddiadau. Cyfeiriwch at yr <a href=\"{VIEW_CLAIMANT_HEARING_REQS}\" rel=\"noopener noreferrer\" class=\"govuk-link\" target=\"_blank\">holiadur a gyflwynwyd gennych</a> os nad ydych yn siŵr beth ddywedoch chi o'r blaen.</p>")
            );

        doGet(BEARER_TOKEN, GET_TASKS_ITEMS_URL, caseId, "CLAIMANT")
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].reference").value(caseId.toString()),
                jsonPath("$[0].taskNameEn").value(
                    "<a href={ADD_TRIAL_ARRANGEMENTS} class=\"govuk-link\">Add the trial arrangements</a>"),
                jsonPath("$[0].currentStatusEn").value(TaskStatus.ACTION_NEEDED.getName()),
                jsonPath("$[0].taskNameCy").value(
                    "<a href={ADD_TRIAL_ARRANGEMENTS} class=\"govuk-link\">Ychwanegu trefniadau'r treial</a>"),
                jsonPath("$[0].currentStatusCy").value(TaskStatus.ACTION_NEEDED.getWelshName()),
                jsonPath("$[0].hintTextEn").value("Deadline is 12am on 4 March 2024"),
                jsonPath("$[0].hintTextCy").value("y dyddiad cau yw 12am ar 4 Mawrth 2024")
            );
    }
}

