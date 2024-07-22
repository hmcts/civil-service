package uk.gov.hmcts.reform.civil.handler.callback.camunda.caseevents;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocument;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.SendFinalOrderBulkPrintService;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentDownloadService;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SEND_FINAL_ORDER_TO_LIP_CLAIMANT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SEND_FINAL_ORDER_TO_LIP_DEFENDANT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SEND_TRANSLATED_ORDER_TO_LIP_CLAIMANT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SEND_TRANSLATED_ORDER_TO_LIP_DEFENDANT;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.JUDGE_FINAL_ORDER;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.ORDER_NOTICE;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
public class SendFinalOrderToLiPCallbackHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private SendFinalOrderToLiPCallbackHandler handler;
    @Mock
    private SendFinalOrderBulkPrintService sendFinalOrderBulkPrintService;
    @Mock
    private DocumentDownloadService documentDownloadService;
    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private DashboardApiClient dashboardApiClient;

    public static final String TASK_ID_DEFENDANT = "SendFinalOrderToDefendantLIP";
    public static final String TASK_ID_CLAIMANT = "SendFinalOrderToClaimantLIP";

    @Test
    void shouldNotCallRecordScenario_whenCaseProgressionIsDisabled() {
        when(featureToggleService.isCaseProgressionEnabled()).thenReturn(false);

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, CaseData.builder().build())
            .build();

        handler.handle(callbackParams);
        verify(dashboardApiClient, never())
            .recordScenario(anyString(), anyString(), anyString(), any(ScenarioRequestParams.class));

    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(SEND_FINAL_ORDER_TO_LIP_DEFENDANT);
        assertThat(handler.handledEvents()).contains(SEND_FINAL_ORDER_TO_LIP_CLAIMANT);
        assertThat(handler.handledEvents()).contains(SEND_TRANSLATED_ORDER_TO_LIP_CLAIMANT);
        assertThat(handler.handledEvents()).contains(SEND_TRANSLATED_ORDER_TO_LIP_DEFENDANT);
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
        when(featureToggleService.isCaseProgressionEnabled()).thenReturn(true);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

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
        when(featureToggleService.isCaseProgressionEnabled()).thenReturn(true);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // then
        assertThat(response.getErrors()).isNull();
        verify(sendFinalOrderBulkPrintService).sendFinalOrderToLIP(any(), any(), eq(TASK_ID_CLAIMANT));
    }

    @Test
    void shouldDownloadTranslatedDocumentAndPrintLetterSuccessfullyWhenIsClaimant() {
        // given
        CaseData caseData = CaseDataBuilder.builder().caseDataLip(
            CaseDataLiP.builder().translatedDocuments(
                wrapElements(TranslatedDocument.builder().documentType(ORDER_NOTICE).build())).build()).build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        params.getRequest().setEventId(SEND_TRANSLATED_ORDER_TO_LIP_CLAIMANT.name());
        // when
        when(featureToggleService.isCaseProgressionEnabled()).thenReturn(true);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // then
        assertThat(response.getErrors()).isNull();
        verify(sendFinalOrderBulkPrintService).sendTranslatedFinalOrderToLIP(any(), any(), eq(TASK_ID_CLAIMANT));
    }

    @Test
    void shouldDownloadTranslatedDocumentAndPrintLetterSuccessfullyWhenIsDefendant() {
        // given
        CaseData caseData = CaseDataBuilder.builder().caseDataLip(
            CaseDataLiP.builder().translatedDocuments(
                wrapElements(TranslatedDocument.builder().documentType(ORDER_NOTICE).build())).build()).build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        params.getRequest().setEventId(SEND_TRANSLATED_ORDER_TO_LIP_DEFENDANT.name());
        // when
        when(featureToggleService.isCaseProgressionEnabled()).thenReturn(true);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // then
        assertThat(response.getErrors()).isNull();
        verify(sendFinalOrderBulkPrintService).sendTranslatedFinalOrderToLIP(any(), any(), eq(TASK_ID_DEFENDANT));
    }
}
