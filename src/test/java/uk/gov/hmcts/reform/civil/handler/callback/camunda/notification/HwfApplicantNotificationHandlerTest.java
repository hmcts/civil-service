package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.HwfNotificationService;
import uk.gov.hmcts.reform.civil.service.NotificationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_APPLICANT_LIP_HWF;

@SpringBootTest(classes = {
    HwfApplicantNotificationHandler.class,
    JacksonAutoConfiguration.class,
})
public class HwfApplicantNotificationHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private HwfApplicantNotificationHandler handler;
    @MockBean
    HwfNotificationService notificationService;
    private CallbackParams params;

    @Test
    public void shouldReturnCorrectEvent() {
        CaseData caseData = CaseDataBuilder.builder().judicialOrderMadeWithUncloakApplication(YesOrNo.NO).build();
        params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        assertThat(handler.handledEvents()).contains(NOTIFY_APPLICANT_LIP_HWF);
    }

    @Test
    void shouldThrowException_whenNotificationSendingFails() {
        var caseData = CaseDataBuilder.builder().judicialOrderMadeWithUncloakApplication(YesOrNo.NO)
                .build();

        doThrow(buildNotificationException())
                .when(notificationService)
                .sendNotification(caseData);

        params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        assertThrows(NotificationException.class, () -> handler.handle(params));
    }

    private NotificationException buildNotificationException() {
        return new NotificationException(new Exception("Notification Exception"));
    }
}
