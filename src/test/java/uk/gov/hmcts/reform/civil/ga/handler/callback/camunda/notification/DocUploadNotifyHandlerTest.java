package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.ga.handler.GeneralApplicationBaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.DocUploadNotificationService;
import uk.gov.hmcts.reform.civil.ga.service.GaNotificationHistoryService;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.testutils.ObjectMapperFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GA_EVIDENCE_UPLOAD_CHECK;

@ExtendWith(MockitoExtension.class)
class DocUploadNotifyHandlerTest extends GeneralApplicationBaseCallbackHandlerTest {

    @InjectMocks
    private DocUploadNotifyHandler handler;
    @Spy
    private ObjectMapper objectMapper = ObjectMapperFactory.instance();
    @Mock
    private DocUploadNotificationService notificationService;
    @Mock
    private GaNotificationHistoryService historyService;

    @Test
    void shouldHandleTheEvidenceEvent() {
        assertThat(handler.handledEvents()).containsExactly(GA_EVIDENCE_UPLOAD_CHECK);
    }

    @Test
    void shouldPersistTheSendSummaryWithoutUsingTheBusinessProcess() {
        var data = GeneralApplicationCaseDataBuilder.builder().withNoticeCaseData().build().copy().businessProcess(null).build();
        String summary = "Attempted: a@example.com : ref : template || Errors: failed";
        when(notificationService.notifyEvidenceUploadAndGetSummary(data)).thenReturn(summary);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParamsOf(data, ABOUT_TO_SUBMIT));
        var saved = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);
        assertThat(saved.getNotificationSummary()).isEqualTo(summary);
        assertThat(saved.getBusinessProcess()).isNull();
        verifyNoInteractions(historyService);
    }

    @Test
    void shouldRecordHistoryAfterTheEvidenceEventHasSubmittedWithoutSendingAgain() {
        var data = GeneralApplicationCaseDataBuilder.builder().withNoticeCaseData().build().copy()
            .notificationSummary("Attempted: a@example.com : ref : template || Errors: ").build();
        handler.handle(callbackParamsOf(data, SUBMITTED));
        verify(historyService).recordNotifications(data.getCcdCaseReference().toString(), data.getNotificationSummary());
        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldSkipHistoryForAnOlderCallbackWithoutASummary() {
        var data = GeneralApplicationCaseDataBuilder.builder().withNoticeCaseData().build();
        handler.handle(callbackParamsOf(data, SUBMITTED));
        verifyNoInteractions(historyService, notificationService);
    }
}
