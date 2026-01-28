package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.handler.GeneralApplicationBaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.HearingScheduledNotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationException;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_HEARING_NOTICE_CLAIMANT;

@SpringBootTest(classes = {
    NotifyHearingNoticeClaimantHandler.class,
    JacksonAutoConfiguration.class,
})
public class NotifyHearingNoticeClaimantHandlerTest extends GeneralApplicationBaseCallbackHandlerTest {

    @Autowired
    private NotifyHearingNoticeClaimantHandler handler;
    @MockBean
    HearingScheduledNotificationService hearingNotifyService;
    @Autowired
    ObjectMapper objectMapper;
    private CallbackParams params;

    @Test
    public void shouldReturnCorrectEvent() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().hearingScheduledApplication(YesOrNo.NO).build();
        params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        assertThat(handler.handledEvents()).contains(NOTIFY_HEARING_NOTICE_CLAIMANT);
    }

    @Test
    void shouldThrowException_whenNotificationSendingFails() {
        var caseData = GeneralApplicationCaseDataBuilder.builder().hearingScheduledApplication(YesOrNo.NO)
            .build();

        doThrow(buildNotificationException())
            .when(hearingNotifyService)
            .sendNotificationForClaimant(caseData);

        params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        assertThrows(NotificationException.class, () -> handler.handle(params));

    }

    private NotificationException buildNotificationException() {
        return new NotificationException(new Exception("Notification Exception"));
    }

    @Test
    void shouldSendNotificationToClaimantSuccessfully() {
        var caseData = GeneralApplicationCaseDataBuilder.builder().hearingScheduledApplication(YesOrNo.NO)
            .build();
        when(hearingNotifyService.sendNotificationForClaimant(any())).thenReturn(caseData);
        params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response).isNotNull();
    }

}
