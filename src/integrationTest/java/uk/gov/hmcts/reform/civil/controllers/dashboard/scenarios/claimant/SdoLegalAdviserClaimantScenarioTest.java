package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.controllers.CaseProgressionDashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant.OrderMadeClaimantNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.utils.DateUtils;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;

public class SdoLegalAdviserClaimantScenarioTest extends CaseProgressionDashboardBaseIntegrationTest {

    @Autowired
    private OrderMadeClaimantNotificationHandler handler;

    @Test
    void should_create_order_made_claimant_scenario() throws Exception {

        String caseId = "72014112265";
        DynamicListElement selectedCourt = DynamicListElement.builder()
            .code("00002").label("court 2 - 2 address - Y02 7RB").build();

        when(featureToggleService.isCaseProgressionEnabled()).thenReturn(true);
        when(featureToggleService.isCaseProgressionEnabledAndLocationWhiteListed(any())).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().atStateRespondentPartAdmissionSpec().build()
            .toBuilder()
            .responseClaimTrack("SMALL_CLAIM")
            .totalClaimAmount(BigDecimal.valueOf(500))
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .applicant1Represented(YesOrNo.NO)
            .caseManagementLocation(CaseLocationCivil.builder().baseLocation(selectedCourt.getCode()).build())
            .finalOrderDocumentCollection(List.of(ElementUtils.element(
                CaseDocument.builder().documentLink(Document.builder().documentBinaryUrl("url").build()).build())))
            .build();

        handler.handle(callbackParamsTestSDO(caseData));

        String requestForReconsiderationDeadlineEn = DateUtils.formatDate(LocalDate.now().plusDays(7));
        String requestForReconsiderationDeadlineCy = DateUtils.formatDateInWelsh(LocalDate.now().plusDays(7), false);

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("An order has been made on this claim"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">You need to carefully <a href=\"{VIEW_SDO_DOCUMENT}\" rel=\"noopener noreferrer\" target=\"_blank\" class=\"govuk-link\">read and review this order</a>. If you don't agree with something in the order you can <a href=\"{REQUEST_FOR_RECONSIDERATION}\" rel=\"noopener noreferrer\" class=\"govuk-link\">ask the court to review it</a>. You can only do this once. You will have to provide details about what changes you want made and these will be reviewed by a judge. This must be done before " + requestForReconsiderationDeadlineEn + ".</p>"),
                jsonPath("$[0].titleCy").value("Mae gorchymyn wedi'i wneud ar yr hawliad hwn"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Mae angen i chi <a href=\"{VIEW_SDO_DOCUMENT}\" rel=\"noopener noreferrer\" target=\"_blank\" class=\"govuk-link\">ddarllen ac adolygu'r gorchymyn hwn yn ofalus</a>. Os nad ydych yn cytuno â rhywbeth yn y gorchymyn, <a href=\"{REQUEST_FOR_RECONSIDERATION}\" rel=\"noopener noreferrer\" class=\"govuk-link\">gallwch ofyn i'r llys ei adolygu</a>. Dim ond unwaith y gallwch wneud hyn. Bydd yn rhaid i chi roi manylion am y newidiadau rydych eisiau gweld yn cael eu gwneud a bydd y rhain yn cael eu hadolygu gan farnwr. Rhaid gwneud hyn cyn " + requestForReconsiderationDeadlineCy + ".</p>")
            );
    }

    private static CallbackParams callbackParamsTestSDO(CaseData caseData) {
        return CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .request(CallbackRequest.builder().eventId(
                CaseEvent.CREATE_DASHBOARD_NOTIFICATION_SDO_CLAIMANT.name()).build())
            .params(Map.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .build();
    }
}
