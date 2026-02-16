package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.handler.GeneralApplicationBaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.model.genapplication.GAInformOtherParty;
import uk.gov.hmcts.reform.civil.ga.service.HearingScheduledNotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationException;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.testutils.ObjectMapperBuilder;

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

@ExtendWith(MockitoExtension.class)
public class NotifyHearingNoticeDefendantHandlerTest extends GeneralApplicationBaseCallbackHandlerTest {

    @InjectMocks
    private NotifyHearingNoticeDefendantHandler handler;

    @Mock
    HearingScheduledNotificationService hearingScheduledNotificationService;

    @Spy
    private ObjectMapper objectMapper = ObjectMapperBuilder.instance();
    private CallbackParams params;

    @Test
    public void shouldReturnCorrectEventWhenCalled() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().hearingScheduledApplication(YesOrNo.NO).build();
        params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        assertThat(handler.handledEvents()).contains(NOTIFY_HEARING_NOTICE_DEFENDANT);
    }

    @Test
    void shouldThrowException_whenNotificationSendingFails() {
        var caseData = GeneralApplicationCaseDataBuilder.builder().hearingScheduledApplication(YesOrNo.NO)
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
        var caseData = GeneralApplicationCaseDataBuilder.builder().hearingScheduledApplication(YesOrNo.NO)
            .build();
        when(hearingScheduledNotificationService.sendNotificationForDefendant(any())).thenReturn(caseData);
        params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response).isNotNull();
    }

    @Test
    void shouldSendNotificationToDefendantSuccessfullyWhenWithoutNotice() {
        var caseData = GeneralApplicationCaseDataBuilder.builder().hearingScheduledApplication(YesOrNo.YES)
            .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(NO).build())
            .build();
        when(hearingScheduledNotificationService.sendNotificationForDefendant(any())).thenReturn(caseData);
        params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response).isNotNull();
        verify(hearingScheduledNotificationService).sendNotificationForDefendant(any());
    }
}
