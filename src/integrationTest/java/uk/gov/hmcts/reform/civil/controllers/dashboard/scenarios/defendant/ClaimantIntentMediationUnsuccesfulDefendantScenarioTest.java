package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.defendant;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulReason.APPOINTMENT_NO_AGREEMENT;
import static uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulReason.NOT_CONTACTABLE_DEFENDANT_ONE;

import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.controllers.dashboard.mock.MockTaskList;
import uk.gov.hmcts.reform.civil.controllers.dashboard.util.Evaluations;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulReason;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant.MediationUnsuccessfulDashboardNotificationDefendantHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Mediation;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.dashboard.data.TaskList;

import java.util.List;

public class ClaimantIntentMediationUnsuccesfulDefendantScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private MediationUnsuccessfulDashboardNotificationDefendantHandler handler;

    @Test
    void should_create_mediation_unsuccessful_scenario() throws Exception {
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(false);

        String caseId = "32341";
        Party respondent1 = new Party();
        respondent1.toBuilder().partyName("John Doe").build();
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued1v1LiP().build()
            .toBuilder()
            .ccdCaseReference(Long.valueOf(32341))
            .respondent1Represented(YesOrNo.NO)
            .applicant1(Party.builder().individualFirstName("John").individualLastName("Doe")
                             .type(Party.Type.INDIVIDUAL).build())
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "DEFENDANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Mediation was unsuccessful"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">You weren\'t able to resolve John Doe\'s claim against you using mediation. The court will review the case. We\'ll contact you to tell you what to do next. <a href={VIEW_CLAIMANT_HEARING_REQS} target=\"_blank\" class=\"govuk-link\">View John Doe\'s hearing requirements</a>.</p>"),
                jsonPath("$[0].titleCy").value("Nid oedd y cyfryngu yn llwyddiannus"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Nid oeddech yn gallu datrys hawliad John Doe yn eich erbyn drwy gyfryngu. Bydd y llys yn adolygu’r achos. Mi wnawn gysylltu â chi i ddweud wrthych beth i’w wneud nesaf. <a href={VIEW_CLAIMANT_HEARING_REQS} target=\"_blank\" class=\"govuk-link\">Gweld gofynion ar gyfer y gwrandawiad John Doe</a>.</p>"));

    }

    @Test
    void should_create_mediation_unsuccessful_scenario_for_carm() throws Exception {
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);

        String caseId = "32341";
        Party respondent1 = new Party();
        respondent1.toBuilder().partyName("John Doe").build();
        MediationUnsuccessfulReason reason = APPOINTMENT_NO_AGREEMENT;
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued1v1LiP().build()
            .toBuilder()
            .ccdCaseReference(Long.valueOf(32341))
            .respondent1Represented(YesOrNo.NO)
            .applicant1(Party.builder().individualFirstName("John").individualLastName("Doe")
                            .type(Party.Type.INDIVIDUAL).build())
            .mediation(Mediation.builder()
                           .mediationUnsuccessfulReasonsMultiSelect(List.of(reason)).build())
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "DEFENDANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Mediation appointment unsuccessful"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">You were not able to resolve this claim using mediation.</p> "
                        + "<p class=\"govuk-body\">This case will now be reviewed by the court.</p>"),
                jsonPath("$[0].titleCy").value("Mediation appointment unsuccessful"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">You were not able to resolve this claim using mediation.</p> "
                        + "<p class=\"govuk-body\">This case will now be reviewed by the court.</p>"));

    }

    @Test
    void should_create_mediation_unsuccessful_scenario_for_carm_defandant_nonattendance() throws Exception {
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);

        String caseId = "32341";
        final List<TaskList> taskListExpected = MockTaskList.getMediationUnsuccessfulTaskListMock("DEFENDANT", caseId);
        Party respondent1 = new Party();
        respondent1.toBuilder().partyName("John Doe").build();
        MediationUnsuccessfulReason reason = NOT_CONTACTABLE_DEFENDANT_ONE;
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued1v1LiP().build()
            .toBuilder()
            .ccdCaseReference(Long.valueOf(32341))
            .respondent1Represented(YesOrNo.NO)
            .applicant1(Party.builder().individualFirstName("John").individualLastName("Doe")
                            .type(Party.Type.INDIVIDUAL).build())
            .mediation(Mediation.builder()
                           .mediationUnsuccessfulReasonsMultiSelect(List.of(reason)).build())
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "DEFENDANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("You did not attend mediation"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">You did not attend your mediation appointment, and the judge may issue "
                        + "a penalty against you. Your case will not be reviewed by the court. "
                        + "<a href=\"{UPLOAD_MEDIATION_DOCUMENTS}\" class=\"govuk-link\">Explain why you did not "
                        + "attend your appointment.</a></p>"),
                jsonPath("$[0].titleCy").value("You did not attend mediation"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">You did not attend your mediation appointment, and the judge may issue "
                        + "a penalty against you. Your case will not be reviewed by the court. "
                        + "<a href=\"{UPLOAD_MEDIATION_DOCUMENTS}\" class=\"govuk-link\">Explain why you did not "
                        + "attend your appointment.</a></p>"));

        //Verify dashboard information
        String result = doGet(BEARER_TOKEN, GET_TASKS_ITEMS_URL, caseId, "DEFENDANT")
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        List<TaskList> response = toTaskList(result);
        Evaluations.evaluateSizeOfTasklist(response.size(), taskListExpected.size());
        Evaluations.evaluateMediationTasklist(response, taskListExpected);
    }
}
