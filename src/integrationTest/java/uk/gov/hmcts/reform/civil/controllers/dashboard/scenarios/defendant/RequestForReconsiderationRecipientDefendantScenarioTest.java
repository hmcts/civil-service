package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.defendant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant.RequestForReconsiderationRequestedByOtherPartyDefendantNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.utils.DateUtils;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class RequestForReconsiderationRecipientDefendantScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private RequestForReconsiderationRequestedByOtherPartyDefendantNotificationHandler handler;

    @Test
    @DirtiesContext
    void should_create_request_for_reconsideration_defendant_scenario() throws Exception {

        String caseId = "7201415645434333";

        CaseData caseData = CaseDataBuilder.builder().atStateRespondentPartAdmissionSpec().build()
            .toBuilder()
            .responseClaimTrack("SMALL_CLAIM")
            .totalClaimAmount(BigDecimal.valueOf(500))
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .respondent1Represented(YesOrNo.NO)
            .finalOrderDocumentCollection(List.of(ElementUtils.element(
                CaseDocument.builder().documentLink(Document.builder().documentBinaryUrl("url").build()).build())))
            .requestForReconsiderationDeadline(LocalDateTime.of(2024, 6, 14, 16, 0))
            .build();
        when(featureToggleService.isCaseProgressionEnabled()).thenReturn(true);

        handler.handle(callbackParams(caseData));

        String requestForReconsiderationDeadlineEn = DateUtils.formatDate(LocalDate.of(2024, 6, 14));
        String requestForReconsiderationDeadlineCy = DateUtils.formatDateInWelsh(LocalDate.of(2024, 6, 14));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "DEFENDANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Review has been requested"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">A review of an order has been requested by the other parties. You can <a href=\"{VIEW_REQUEST_FOR_RECONSIDERATION_DOCUMENT}\" rel=\"noopener noreferrer\" target=\"_blank\" class=\"govuk-link\">view their request</a> and <a href=\"{REQUEST_FOR_RECONSIDERATION_COMMENTS}\" rel=\"noopener noreferrer\" class=\"govuk-link\">add comments of your own</a> by " + requestForReconsiderationDeadlineEn + ". A judge will review the request and your comments and you will be contacted if the judge makes a new order. Continue doing what the current order asks of you unless you're informed a judge has made a new order.</p>"),
                jsonPath("$[0].titleCy").value("Gofynnwyd am adolygiad"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Mae'r partïon eraill wedi gofyn am adolygiad o orchymyn. Gallwch <a href=\"{VIEW_REQUEST_FOR_RECONSIDERATION_DOCUMENT}\" rel=\"noopener noreferrer\" target=\"_blank\" class=\"govuk-link\">weld eu cais</a> ac <a href=\"{REQUEST_FOR_RECONSIDERATION_COMMENTS}\" rel=\"noopener noreferrer\" class=\"govuk-link\">ychwanegu sylwadau eich hun</a> erbyn " + requestForReconsiderationDeadlineCy + ". Bydd barnwr yn adolygu'r cais a'ch sylwadau a chysylltir â chi os bydd y barnwr yn gwneud gorchymyn newydd. Parhewch i wneud yr hyn y mae'r gorchymyn presennol yn ei ofyn oni bai eich bod yn cael gwybod bod barnwr wedi gwneud gorchymyn newydd.</p>")
            );
    }
}
