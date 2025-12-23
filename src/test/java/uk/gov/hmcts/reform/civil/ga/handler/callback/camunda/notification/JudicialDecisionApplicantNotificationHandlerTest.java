package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.notification;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.handler.GeneralApplicationBaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.JudicialDecisionHelper;
import uk.gov.hmcts.reform.civil.ga.service.JudicialNotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationException;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.START_APPLICANT_NOTIFICATION_PROCESS_MAKE_DECISION;

@SpringBootTest(classes = {
    JudicialDecisionApplicantNotificationHandler.class,
    JacksonAutoConfiguration.class,
})
class JudicialDecisionApplicantNotificationHandlerTest extends GeneralApplicationBaseCallbackHandlerTest {

    @Autowired
    private JudicialDecisionApplicantNotificationHandler handler;
    @MockitoBean
    JudicialNotificationService judicialNotificationService;
    @MockitoBean
    JudicialDecisionHelper judicialDecisionHelper;
    private CallbackParams params;

    @Test
    public void shouldReturnCorrectEvent() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().judicialOrderMadeWithUncloakApplication(YesOrNo.NO).build();
        params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        assertThat(handler.handledEvents()).contains(START_APPLICANT_NOTIFICATION_PROCESS_MAKE_DECISION);
    }

    @Test
    void shouldThrowException_whenNotificationSendingFails() {
        var caseData = GeneralApplicationCaseDataBuilder.builder().judicialOrderMadeWithUncloakApplication(YesOrNo.NO)
            .build();

        doThrow(buildNotificationException())
            .when(judicialNotificationService)
            .sendNotification(caseData, "applicant");

        params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        assertThrows(NotificationException.class, () -> handler.handle(params));
    }

    private NotificationException buildNotificationException() {
        return new NotificationException(new Exception("Notification Exception"));
    }
}
