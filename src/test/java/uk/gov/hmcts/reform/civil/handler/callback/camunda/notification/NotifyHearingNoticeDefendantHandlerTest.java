package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.genapplication.GAInformOtherParty;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.HearingScheduledNotificationService;
import uk.gov.hmcts.reform.civil.service.NotificationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_HEARING_NOTICE_DEFENDANT;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@SpringBootTest(classes = {
    NotifyHearingNoticeDefendantHandler.class,
    JacksonAutoConfiguration.class,
})
public class NotifyHearingNoticeDefendantHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private NotifyHearingNoticeDefendantHandler handler;
    @MockBean
    HearingScheduledNotificationService hearingScheduledNotificationService;
    private CallbackParams params;

    @Test
    public void shouldReturnCorrectEventWhenCalled() {
        CaseData caseData = CaseDataBuilder.builder().hearingScheduledApplication(YesOrNo.NO).build();
        params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        assertThat(handler.handledEvents()).contains(NOTIFY_HEARING_NOTICE_DEFENDANT);
    }

    @Test
    void shouldThrowException_whenNotificationSendingFails() {
        var caseData = CaseDataBuilder.builder().hearingScheduledApplication(YesOrNo.NO)
            .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(YES).build())
            .build();

        doThrow(buildNotificationException())
            .when(hearingScheduledNotificationService)
            .sendNotificationForDefendant(caseData);

        params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        assertThrows(NotificationException.class, () -> handler.handle(params));
    }

    private NotificationException buildNotificationException() {
        return new NotificationException(new Exception("Notification Exception"));
    }

    @Test
    void shouldSendNotificationToDefendantSuccessfully() {
        var caseData = CaseDataBuilder.builder().hearingScheduledApplication(YesOrNo.NO)
            .build();
        when(hearingScheduledNotificationService.sendNotificationForDefendant(any())).thenReturn(caseData);
        params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response).isNotNull();
    }

    @Test
    void shouldSendNotificationToDefendantSuccessfullyWhenWithoutNotice() {
        var caseData = CaseDataBuilder.builder().hearingScheduledApplication(YesOrNo.YES)
            .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(NO).build())
            .build();
        when(hearingScheduledNotificationService.sendNotificationForDefendant(any())).thenReturn(caseData);
        params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response).isNotNull();
        verify(hearingScheduledNotificationService).sendNotificationForDefendant(any());
    }
}
