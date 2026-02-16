package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.handler.GeneralApplicationBaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationException;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.testutils.ObjectMapperBuilder;
import uk.gov.hmcts.reform.civil.service.GeneralAppFeesService;
import uk.gov.hmcts.reform.civil.ga.service.GeneralApplicationCreationNotificationService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_GENERAL_APPLICATION_RESPONDENT;

@ExtendWith(MockitoExtension.class)
public class GeneralApplicationCreationNotificationHandlerTest extends GeneralApplicationBaseCallbackHandlerTest {

    @InjectMocks
    private GeneralApplicationCreationNotificationHandler handler;

    @Spy
    private ObjectMapper objectMapper = ObjectMapperBuilder.instance();

    @Mock
    private GeneralApplicationCreationNotificationService gaNotificationService;

    @Mock
    private GeneralAppFeesService generalAppFeesService;
    private CallbackParams params;

    @Test
    public void shouldReturnCorrectEvent() {

        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().judicialOrderMadeWithUncloakApplication(YesOrNo.YES).build();
        params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        assertThat(handler.handledEvents()).contains(NOTIFY_GENERAL_APPLICATION_RESPONDENT);
    }

    @Test
    void shouldThrowException_whenNotificationSendingFails() {
        var caseData = GeneralApplicationCaseDataBuilder.builder().generalOrderApplication()
                .ccdState(CaseState.AWAITING_APPLICATION_PAYMENT)
                .build();
        when(generalAppFeesService.isFreeApplication(any())).thenReturn(false);
        doThrow(buildNotificationException())
            .when(gaNotificationService)
            .sendNotification(caseData);

        params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        assertThrows(NotificationException.class, () -> handler.handle(params));
    }

    @Test
    void shouldNotSendNotice_whenPendingGaNotFree() {
        var caseData = GeneralApplicationCaseDataBuilder.builder().generalOrderApplication()
                .ccdState(CaseState.PENDING_APPLICATION_ISSUED)
                .build();
        when(generalAppFeesService.isFreeApplication(any())).thenReturn(false);

        params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        handler.handle(params);
        verify(gaNotificationService, never()).sendNotification(caseData);
    }

    @Test
    void shouldSendNotice_whenPendingGaFree() {
        var caseData = GeneralApplicationCaseDataBuilder.builder().generalOrderApplication()
                .ccdState(CaseState.PENDING_APPLICATION_ISSUED)
                .build();
        when(generalAppFeesService.isFreeApplication(any())).thenReturn(true);
        when(gaNotificationService.sendNotification(any())).thenAnswer(i -> i.getArguments()[0]);
        params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        handler.handle(params);
        verify(gaNotificationService).sendNotification(caseData);
    }

    @Test
    void shouldSendNotice_whenNotPendingGaNotFree() {
        var caseData = GeneralApplicationCaseDataBuilder.builder().generalOrderApplication()
                .ccdState(CaseState.AWAITING_APPLICATION_PAYMENT)
                .build();
        when(generalAppFeesService.isFreeApplication(any())).thenReturn(false);
        when(gaNotificationService.sendNotification(any())).thenAnswer(i -> i.getArguments()[0]);
        params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        handler.handle(params);
        verify(gaNotificationService).sendNotification(caseData);
    }

    private NotificationException buildNotificationException() {
        return new NotificationException(new Exception("Notification Exception"));
    }

}

