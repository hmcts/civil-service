package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant.CCJRequestedDashboardNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.dashboard.data.TaskStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CCJRequestedScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private CCJRequestedDashboardNotificationHandler handler;

    @Test
    void should_create_ccj_requested_scenario() throws Exception {

        String caseId = "12345678";
        LocalDateTime responseDeadline = LocalDateTime.now().minusDays(1);
        String defendantName = "Mr. Sole Trader";
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued1v1UnrepresentedDefendantSpec().build()
            .toBuilder().respondent1ResponseDeadline(responseDeadline)
            .applicant1Represented(YesOrNo.NO)
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .defaultJudgmentDocuments(List.of(
                Element.<CaseDocument>builder()
                    .value(CaseDocument.builder().documentType(DocumentType.DEFAULT_JUDGMENT)
                               .createdDatetime(LocalDateTime.now()).build()).build()))
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("County Court Judgment (CCJ) requested"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">We'll process your request and post a copy of the judgment to you and "
                        + defendantName
                        + ". We aim to do this as soon as possible.</p>"
                        + "<p class=\"govuk-body\">Your online account will not be updated, and "
                        + defendantName
                        + " will no longer be able to respond to your claim online. Any further updates will be by post.</p>"
                        + "<p class=\"govuk-body\">If a postal response is received before the judgment is issued, your request will be rejected.</p>"
                        + "<p class=\"govuk-body\"><a href=\"{enforceJudgementUrl}\" target=\"_blank\" rel=\"noopener noreferrer\" class=\"govuk-link\">Find out about actions you can take once a CCJ is issued (opens in a new tab)</a>.</p>"),
                jsonPath("$[0].titleCy").value("Cais am Ddyfarniad Llys Sirol (CCJ) wedi’i wneud"),
                 jsonPath("$[0].descriptionCy").value(
                "<p class=\"govuk-body\">Byddwn yn prosesu eich cais ac yn anfon copi o’r dyfarniad drwy’r post atoch chi a "
                    + defendantName + ". Fe ymdrechwn at wneud hyn cyn gynted â phosibl.</p>"
                    + "<p class=\"govuk-body\">Ni fydd eich cyfrif ar-lein yn cael ei ddiweddaru, ac ni fydd "
                    + defendantName + " yn gallu ymateb i’ch hawliad ar-lein mwyach. Byddwch yn cael eich hysbysu drwy’r post am unrhyw ddiweddariadau pellach.</p>"
                    + "<p class=\"govuk-body\">Os derbynnir ymateb drwy’r post cyn cyhoeddi’r dyfarniad, bydd eich cais yn cael ei wrthod.</p>"
                    + "<p class=\"govuk-body\"><a href=\"{enforceJudgementUrl}\" target=\"_blank\" rel=\"noopener noreferrer\" class=\"govuk-link\">Gwybodaeth am y camau y gallwch eu cymryd yn dilyn cyhoeddi CCJ (yn agor mewn tab newydd)</a>.</p>")
            );

        //Verify task Item is created
        doGet(BEARER_TOKEN, GET_TASKS_ITEMS_URL, caseId, "CLAIMANT")
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].reference").value(caseId.toString()),
                jsonPath("$[0].taskNameEn").value(
                    "<a href={VIEW_CLAIM_URL} rel=\"noopener noreferrer\" class=\"govuk-link\">View the claim</a>"),
                jsonPath("$[0].taskNameCy").value(
                    "<a href={VIEW_CLAIM_URL} rel=\"noopener noreferrer\" class=\"govuk-link\">Gweld yr hawliad</a>"),
                jsonPath("$[0].currentStatusEn").value(TaskStatus.AVAILABLE.getName()),
                jsonPath("$[1].taskNameEn")
                    .value(
                        "<a href={VIEW_INFO_ABOUT_CLAIMANT} rel=\"noopener noreferrer\" class=\"govuk-link\">View information about the claimant</a>"),
                jsonPath("$[1].currentStatusEn").value(TaskStatus.AVAILABLE.getName()),
                jsonPath("$[2].taskNameEn")
                    .value(
                        "<a href={VIEW_INFO_ABOUT_DEFENDANT} rel=\"noopener noreferrer\" class=\"govuk-link\">View information about the defendant</a>"),
                jsonPath("$[2].currentStatusEn").value(TaskStatus.AVAILABLE.getName()),
                jsonPath("$[3].taskNameEn")
                    .value(
                        "<a href={VIEW_ORDERS_AND_NOTICES} rel=\"noopener noreferrer\" class=\"govuk-link\">View orders and notices</a>"),
                jsonPath("$[3].currentStatusEn").value(TaskStatus.AVAILABLE.getName())

            );

    }

    @Test
    void should_create_ccj_requested_scenario_duringClaimant_intention() throws Exception {

        String caseId = "1234445678";
        String defendantName = "Mr. Sole Trader";
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued1v1UnrepresentedDefendantSpec().build()
            .toBuilder().respondent1ResponseDeadline(LocalDate.now().plusDays(10).atTime(16, 0, 0))
            .legacyCaseReference("reference")
            .applicant1Represented(YesOrNo.NO)
            .applicant1AcceptFullAdmitPaymentPlanSpec(YesOrNo.NO)
            .ccdCaseReference(Long.valueOf(caseId))
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT").andExpect(status().isOk()).andExpectAll(
            status().is(HttpStatus.OK.value()),
            jsonPath("$[0].titleEn").value(
                "You requested a County Court Judgment against Mr. Sole Trader"),
            jsonPath("$[0].descriptionEn").value(
                "<p class=\"govuk-body\">You rejected the <a href=\"{VIEW_CCJ_REPAYMENT_PLAN_CLAIMANT}\" class=\"govuk-link\">repayment plan</a>. When we've processed the request, we'll post a copy of the judgment to you.</p>"),
            jsonPath("$[0].titleCy").value(
                "Rydych wedi gwneud cais am Ddyfarniad Llys Sirol (CCJ) yn erbyn Mr. Sole Trader"),
            jsonPath("$[0].descriptionCy").value("<p class=\"govuk-body\">Rydych wedi gwrthod y <a href=\"{VIEW_CCJ_REPAYMENT_PLAN_CLAIMANT}\" class=\"govuk-link\">cynllun ad-dalu</a>." +
                                                     " Pan fyddwn wedi prosesu’r cais, byddwn yn anfon copi o’r dyfarniad drwy’r post atoch chi.</p>")
        );
    }
}
