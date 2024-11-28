package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant.JudgementByAdmissionIssuedClaimantDashboardNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentState;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentType;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.dashboard.data.TaskStatus;

import java.time.LocalDate;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class JudgementByAdmissionIssuedClaimantScenarioTest extends  DashboardBaseIntegrationTest {

    public static final String CLAIMANT = "CLAIMANT";

    @Autowired
    private JudgementByAdmissionIssuedClaimantDashboardNotificationHandler handler;

    @Test
    void should_create_scenario_jo_notification_for_claimant() throws Exception {

        String caseId = "6532987";
        CaseData caseData = getCaseData(caseId);
        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);
        handler.handle(callbackParams(caseData));
        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, CLAIMANT)
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("A judgment against the defendant has been made"),
                jsonPath("$[0].descriptionEn")
                    .value(
                        "<p class=\"govuk-body\">The defendant should now pay you according to the terms of the judgment. " +
                            "<br> Once they do, you should <a href=\"{CONFIRM_YOU_HAVE_BEEN_PAID_URL}\" class=\"govuk-link\">confirm that they’ve " +
                            "paid you the full amount that you’re owed</a>.<br>If they do not pay you by the date on the judgment, you can " +
                            "<u>ask for enforcement action to be taken against them</u>. <br>If you need to change the terms of payment within the " +
                            "judgment, such as the instalments you had previously agreed, you can <u>make an application to vary the judgment</u>.</p>"),
                jsonPath("$[0].titleCy").value("Mae dyfarniad wedi’i wneud yn erbyn y diffynnydd"),
                jsonPath("$[0].descriptionCy")
                    .value(
                        "<p class=\"govuk-body\">Dylai’r diffynnydd eich talu yn unol â thelerau’r dyfarniad. " +
                            "<br> Unwaith y byddant yn gwneud hynny, dylech <a href=\"{CONFIRM_YOU_HAVE_BEEN_PAID_URL}\" class=\"govuk-link\">gadarnhau eu " +
                            "bod wedi talu’r swm llawn sy’n ddyledus i chi</a>.<br>Os na fyddant yn eich talu erbyn y dyddiad ar y dyfarniad, gallwch " +
                            "<u>ofyn am gymryd camau  gorfodi yn eu herbyn</u>. <br>Os oes arnoch angen newid y telerau talu o fewn y dyfarniad, " +
                            "fel y rhandaliadau roeddech wedi cytuno arnynt yn flaenorol, gallwch <u>wneud cais i amrywio’r dyfarniad</u>.</p>")
            );
    }

    @Test
    void shouldUpdateJudgmentByAdmissionTaskList() throws Exception {

        String caseId = "720165773";
        CaseData caseData = getCaseData(caseId);
        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);
        handler.handle(callbackParams(caseData));
        handler.handle(callbackParams(caseData));
        //Verify task Item is created
        doGet(BEARER_TOKEN, GET_TASKS_ITEMS_URL, caseId, "CLAIMANT")
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].reference").value(caseId.toString()),
                jsonPath("$[0].taskNameEn").value("<a>View the hearing</a>"),
                jsonPath("$[0].currentStatusEn").value(TaskStatus.INACTIVE.getName()),
                jsonPath("$[0].taskNameCy").value("<a>Gweld y gwrandawiad</a>"),
                jsonPath("$[0].currentStatusCy").value(TaskStatus.INACTIVE.getWelshName()),
                jsonPath("$[1].reference").value(caseId.toString()),
                jsonPath("$[1].taskNameEn").value("<a>Upload hearing documents</a>"),
                jsonPath("$[1].currentStatusEn").value(TaskStatus.INACTIVE.getName()),
                jsonPath("$[1].taskNameCy").value("<a>Llwytho dogfennau'r gwrandawiad</a>"),
                jsonPath("$[1].currentStatusCy").value(TaskStatus.INACTIVE.getWelshName()),
                jsonPath("$[2].reference").value(caseId.toString()),
                jsonPath("$[2].taskNameEn").value("<a>View documents</a>"),
                jsonPath("$[2].currentStatusEn").value(TaskStatus.INACTIVE.getName()),
                jsonPath("$[2].taskNameCy").value("<a>Gweld y dogfennau</a>"),
                jsonPath("$[2].currentStatusCy").value(TaskStatus.INACTIVE.getWelshName()),
                jsonPath("$[3].reference").value(caseId.toString()),
                jsonPath("$[3].taskNameEn").value("<a>Add the trial arrangements</a>"),
                jsonPath("$[3].currentStatusEn").value(TaskStatus.INACTIVE.getName()),
                jsonPath("$[3].taskNameCy").value("<a>Ychwanegu trefniadau'r treial</a>"),
                jsonPath("$[3].currentStatusCy").value(TaskStatus.INACTIVE.getWelshName()),
                jsonPath("$[4].reference").value(caseId.toString()),
                jsonPath("$[4].taskNameEn").value("<a>View the bundle</a>"),
                jsonPath("$[4].currentStatusEn").value(TaskStatus.INACTIVE.getName()),
                jsonPath("$[4].taskNameCy").value("<a>Gweld y bwndel</a>"),
                jsonPath("$[4].currentStatusCy").value(TaskStatus.INACTIVE.getWelshName()),
                jsonPath("$[5].reference").value(caseId.toString()),
                jsonPath("$[5].taskNameEn")
                    .value("<a href={VIEW_ORDERS_AND_NOTICES} rel=\"noopener noreferrer\" class=\"govuk-link\">View orders and notices</a>"),
                jsonPath("$[5].currentStatusEn").value(TaskStatus.AVAILABLE.getName()),
                jsonPath("$[5].taskNameCy")
                    .value("<a href={VIEW_ORDERS_AND_NOTICES} rel=\"noopener noreferrer\" class=\"govuk-link\">Gweld gorchmynion a rhybuddion</a>"),
                jsonPath("$[5].currentStatusCy").value(TaskStatus.AVAILABLE.getWelshName()),
                jsonPath("$[6].reference").value(caseId.toString()),
                jsonPath("$[6].taskNameEn").value("<a href={VIEW_JUDGEMENT} class=\"govuk-link\">View the judgment</a>"),
                jsonPath("$[6].currentStatusEn").value(TaskStatus.AVAILABLE.getName()),
                jsonPath("$[6].taskNameCy").value("<a href={VIEW_JUDGEMENT} class=\"govuk-link\">Gweld y Dyfarniad</a>"),
                jsonPath("$[6].currentStatusCy").value(TaskStatus.AVAILABLE.getWelshName())
            );
    }

    private CaseData getCaseData(String caseId) {
        return CaseDataBuilder.builder().atStateClaimDetailsNotified().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .applicant1Represented(YesOrNo.NO)
            .respondent1Represented(YesOrNo.NO)
            .applicant1AcceptPartAdmitPaymentPlanSpec(YesOrNo.YES)
            .defendantDetailsSpec(DynamicList.builder()
                                      .value(DynamicListElement.builder()
                                                 .label("John Doe")
                                                 .build())
                                      .build())
            .respondent1(PartyBuilder.builder().organisation().build())
            .activeJudgment(JudgmentDetails.builder().issueDate(LocalDate.now())
                                .state(JudgmentState.ISSUED)
                                .type(JudgmentType.JUDGMENT_BY_ADMISSION)
                                .build())
            .build();
    }
}
