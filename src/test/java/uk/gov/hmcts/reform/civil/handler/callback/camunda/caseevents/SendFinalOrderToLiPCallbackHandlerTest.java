package uk.gov.hmcts.reform.civil.handler.callback.camunda.caseevents;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.SendFinalOrderBulkPrintService;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentDownloadService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SEND_FINAL_ORDER_TO_LIP_CLAIMANT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SEND_FINAL_ORDER_TO_LIP_DEFENDANT;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.JUDGE_FINAL_ORDER;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@SpringBootTest(classes = {
    SendFinalOrderToLiPCallbackHandler.class,
    JacksonAutoConfiguration.class
})
public class SendFinalOrderToLiPCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private SendFinalOrderToLiPCallbackHandler handler;
    @MockBean
    private SendFinalOrderBulkPrintService sendFinalOrderBulkPrintService;
    @MockBean
    private DocumentDownloadService documentDownloadService;

    public static final String TASK_ID_DEFENDANT = "SendFinalOrderToDefendantLIP";
    public static final String TASK_ID_CLAIMANT = "SendFinalOrderToClaimantLIP";

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(SEND_FINAL_ORDER_TO_LIP_DEFENDANT);
        assertThat(handler.handledEvents()).contains(SEND_FINAL_ORDER_TO_LIP_CLAIMANT);
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
                SEND_FINAL_ORDER_TO_LIP_DEFENDANT.name()).build())
                                                 .build())).isEqualTo(TASK_ID_DEFENDANT);
        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
                SEND_FINAL_ORDER_TO_LIP_CLAIMANT.name()).build())
                                                 .build())).isEqualTo(TASK_ID_CLAIMANT);
    }

    @Test
    void shouldDownloadDocumentAndPrintLetterSuccessfully() {
        // given
        CaseData caseData = CaseDataBuilder.builder()
            .systemGeneratedCaseDocuments(wrapElements(CaseDocument.builder().documentType(JUDGE_FINAL_ORDER).build())).build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        params.getRequest().setEventId(SEND_FINAL_ORDER_TO_LIP_DEFENDANT.name());
        // when
        var response = (AboutToStartOrSubmitCallbackResponse)handler.handle(params);

        // then
        assertThat(response.getErrors()).isNull();
        verify(sendFinalOrderBulkPrintService).sendFinalOrderToLIP(any(), any(), eq(TASK_ID_DEFENDANT));
    }

    @Test
    void shouldDownloadDocumentAndPrintLetterSuccessfullyWhenIsClaimant() {
        // given
        CaseData caseData = CaseDataBuilder.builder()
            .systemGeneratedCaseDocuments(wrapElements(CaseDocument.builder().documentType(JUDGE_FINAL_ORDER).build())).build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        params.getRequest().setEventId(SEND_FINAL_ORDER_TO_LIP_CLAIMANT.name());
        // when
        var response = (AboutToStartOrSubmitCallbackResponse)handler.handle(params);

        // then
        assertThat(response.getErrors()).isNull();
        verify(sendFinalOrderBulkPrintService).sendFinalOrderToLIP(any(), any(), eq(TASK_ID_CLAIMANT));
    }
}
