package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.defendant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.sdo.ClaimsTrack;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant.OrderMadeDefendantNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Mediation;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.FAST_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseState.All_FINAL_ORDERS_ISSUED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PREPARE_FOR_HEARING_CONDUCT_HEARING;
import static uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulReason.NOT_CONTACTABLE_CLAIMANT_ONE;
import static uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulReason.NOT_CONTACTABLE_DEFENDANT_ONE;

public class OrderMadeDefendantScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private OrderMadeDefendantNotificationHandler handler;

    DynamicListElement selectedCourt = DynamicListElement.builder()
        .code("00002").label("court 2 - 2 address - Y02 7RB").build();

    @Test
    void should_create_order_made_defendant_scenario() throws Exception {

        String caseId = "72014545416";

        CaseData caseData = CaseDataBuilder.builder().atStateRespondentPartAdmissionSpec().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .respondent1Represented(YesOrNo.NO)
            .caseManagementLocation(CaseLocationCivil.builder().baseLocation(selectedCourt.getCode()).build())
            .finalOrderDocumentCollection(List.of(ElementUtils.element(
                CaseDocument.builder().documentLink(Document.builder().documentBinaryUrl("url").build()).build())))
            .build();

        when(featureToggleService.isCaseProgressionEnabledAndLocationWhiteListed(any())).thenReturn(true);
        handler.handle(callbackParamsTest(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "DEFENDANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("An order has been made"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">The judge has made an order on your claim.</p><p class=\"govuk-body\">"
                        + "<a href=\"{VIEW_FINAL_ORDER}\" rel=\"noopener noreferrer\" target=\"_blank\""
                        + " class=\"govuk-link\">View the order</a></p>"),
                jsonPath("$[0].titleCy").value("Mae gorchymyn wedi’i wneud"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Mae'r Barnwr wedi gwneud gorchymyn ar eich hawliad.</p><p class=\"govuk-body\">"
                        + "<a href=\"{VIEW_FINAL_ORDER}\" rel=\"noopener noreferrer\" target=\"_blank\""
                        + " class=\"govuk-link\">Gweld y gorchymyn</a></p>")
            );
    }

    @Test
    void should_create_order_made_defendant_scenario_mediation_unsuccessful_sdo_carm() throws Exception {

        String caseId = "72014545416";

        CaseData caseData = CaseDataBuilder.builder().atStateRespondentPartAdmissionSpec().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .respondent1Represented(YesOrNo.NO)
            .finalOrderDocumentCollection(List.of(ElementUtils.element(
                CaseDocument.builder().documentLink(Document.builder().documentBinaryUrl("url").build()).build())))
            .responseClaimTrack(FAST_CLAIM.name())
            .totalClaimAmount(BigDecimal.valueOf(999))
            .caseManagementLocation(CaseLocationCivil.builder().baseLocation(selectedCourt.getCode()).build())
            .mediation(Mediation.builder()
                           .mediationUnsuccessfulReasonsMultiSelect(List.of(NOT_CONTACTABLE_CLAIMANT_ONE, NOT_CONTACTABLE_DEFENDANT_ONE))
                           .build())
            .build();

        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);

        handler.handle(callbackParamsTestSDO(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "DEFENDANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("You are no longer required to submit documents relating to mediation non-attendance"),
                jsonPath("$[0].descriptionEn").value("<p class=\"govuk-body\">A judge has reviewed your case and you are no "
                                                         + "longer required to submit documents relating to non-attendance of a mediation appointment. "
                                                         + "There will be no penalties issued for your non-attendance.</p>"),
                jsonPath("$[0].titleCy").value("Nid yw'n ofynnol bellach i chi gyflwyno dogfennau sy'n ymwneud â pheidio â mynychu cyfryngu"),
                jsonPath("$[0].descriptionCy").value("<p class=\"govuk-body\">Mae barnwr wedi adolygu " +
                                                         "eich achos ac nid yw'n ofynnol i chi gyflwyno dogfennau bellach yn " +
                                                         "ymwneud â pheidio â mynychu apwyntiad cyfryngu. Ni fydd unrhyw gosbau yn cael eu gosod am eich diffyg presenoldeb.</p>"));
    }

    @Test
    void should_create_order_made_claimant_scenario_pre_cp_release() throws Exception {

        String caseId = "72014545416";

        CaseData caseData = CaseDataBuilder.builder().atStateRespondentPartAdmissionSpec().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .respondent1Represented(YesOrNo.NO)
            .caseManagementLocation(CaseLocationCivil.builder().baseLocation(selectedCourt.getCode()).build())
            .finalOrderDocumentCollection(List.of(ElementUtils.element(
                CaseDocument.builder().documentLink(Document.builder().documentBinaryUrl("url").build()).build())))
            .build();

        when(featureToggleService.isCaseProgressionEnabledAndLocationWhiteListed(any())).thenReturn(false);
        handler.handle(callbackParamsTest(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "DEFENDANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("An order has been issued by the court"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">Please follow instructions in the order and comply with the deadlines. " +
                        "Please send any documents to the court named in the order if required. " +
                        "The claim will now proceed offline, you will receive further updates by post.</p>"),
                jsonPath("$[0].titleCy").value("Mae gorchymyn wedi’i gyhoeddi gan y llys"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Dilynwch y cyfarwyddiadau sydd yn y gorchymyn a chydymffurfiwch " +
                        "â’r dyddiadau terfyn. Anfonwch unrhyw ddogfennau i’r llys a enwir yn y gorchymyn os oes angen. " +
                        "Bydd yr hawliad nawr yn parhau all-lein a byddwch yn cael unrhyw ddiweddariadau pellach drwy’r post.</p>")
            );
    }

    @Test
    void should_create_order_made_defendant_all_orders_issued_scenario() throws Exception {

        String caseId = "720134354545416";

        CaseData caseData = CaseDataBuilder.builder().atStateRespondentPartAdmissionSpec().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .respondent1Represented(YesOrNo.NO)
            .caseManagementLocation(CaseLocationCivil.builder().baseLocation(selectedCourt.getCode()).build())
            .finalOrderDocumentCollection(List.of(ElementUtils.element(
                CaseDocument.builder().documentLink(Document.builder().documentBinaryUrl("url").build()).build())))
            .build();

        when(featureToggleService.isCaseProgressionEnabledAndLocationWhiteListed(any())).thenReturn(true);
        handler.handle(callbackParamsTestFinalOrders(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "DEFENDANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("An order has been made"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">The judge has made an order on your claim.</p>" +
                        "<p class=\"govuk-body\"><a href=\"{VIEW_FINAL_ORDER}\" rel=\"noopener noreferrer\" " +
                        "target=\"_blank\" class=\"govuk-link\">View the order</a></p>"),
                jsonPath("$[0].titleCy").value("Mae gorchymyn wedi’i wneud"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Mae'r Barnwr wedi gwneud gorchymyn ar eich hawliad.</p>" +
                        "<p class=\"govuk-body\"><a href=\"{VIEW_FINAL_ORDER}\" rel=\"noopener noreferrer\" " +
                        "target=\"_blank\" class=\"govuk-link\">Gweld y gorchymyn</a></p>")
            );

        doGet(BEARER_TOKEN, GET_TASKS_ITEMS_URL, caseId, "DEFENDANT")
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].reference").value(caseId.toString()),
                jsonPath("$[0].taskNameEn").value("<a>Upload hearing documents</a>"),
                jsonPath("$[0].currentStatusEn").value("Inactive")
            );
    }

    @Test
    void should_create_order_made_defendant_all_orders_issued_fast_track_not_ready_trial_scenario() throws Exception {

        String caseId = "725984154548966";

        CaseData caseData = CaseDataBuilder.builder().atStateRespondentPartAdmissionSpec().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .respondent1Represented(YesOrNo.NO)
            .claimsTrack(ClaimsTrack.fastTrack)
            .drawDirectionsOrderRequired(YesOrNo.NO)
            .caseManagementLocation(CaseLocationCivil.builder().baseLocation(selectedCourt.getCode()).build())
            .finalOrderDocumentCollection(List.of(ElementUtils.element(
                CaseDocument.builder().documentLink(Document.builder().documentBinaryUrl("url").build()).build())))
            .build();

        when(featureToggleService.isCaseProgressionEnabledAndLocationWhiteListed(any())).thenReturn(true);
        handler.handle(callbackParamsTestFinalOrders(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "DEFENDANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("An order has been made"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">The judge has made an order on your claim.</p>" +
                        "<p class=\"govuk-body\"><a href=\"{VIEW_FINAL_ORDER}\" rel=\"noopener noreferrer\" " +
                        "target=\"_blank\" class=\"govuk-link\">View the order</a></p>"),
                jsonPath("$[0].titleCy").value("Mae gorchymyn wedi’i wneud"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Mae'r Barnwr wedi gwneud gorchymyn ar eich hawliad.</p>" +
                        "<p class=\"govuk-body\"><a href=\"{VIEW_FINAL_ORDER}\" rel=\"noopener noreferrer\" " +
                        "target=\"_blank\" class=\"govuk-link\">Gweld y gorchymyn</a></p>")
            );

        doGet(BEARER_TOKEN, GET_TASKS_ITEMS_URL, caseId, "DEFENDANT")
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[1].reference").value(caseId.toString()),
                jsonPath("$[1].taskNameEn").value("<a>Add the trial arrangements</a>"),
                jsonPath("$[1].currentStatusEn").value("Inactive")
            );
    }

    private static CallbackParams callbackParamsTest(CaseData caseData) {
        return CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .request(CallbackRequest.builder().eventId(
                CaseEvent.CREATE_DASHBOARD_NOTIFICATION_FINAL_ORDER_DEFENDANT.name())
                         .caseDetails(CaseDetails.builder().state(PREPARE_FOR_HEARING_CONDUCT_HEARING.toString()).build()).build())
            .params(Map.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .build();
    }

    private static CallbackParams callbackParamsTestSDO(CaseData caseData) {
        return CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .request(CallbackRequest.builder().eventId(
                CaseEvent.CREATE_DASHBOARD_NOTIFICATION_SDO_DEFENDANT.name()).build())
            .params(Map.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .build();
    }

    private static CallbackParams callbackParamsTestFinalOrders(CaseData caseData) {
        return CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .request(CallbackRequest.builder()
                         .eventId(CaseEvent.CREATE_DASHBOARD_NOTIFICATION_FINAL_ORDER_DEFENDANT.name())
                         .caseDetails(CaseDetails.builder().state(All_FINAL_ORDERS_ISSUED.toString()).build())
                         .build())
            .params(Map.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .build();
    }
}
