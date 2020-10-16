package uk.gov.hmcts.reform.unspec.handler.callback.notification;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.unspec.callback.CallbackParams;
import uk.gov.hmcts.reform.unspec.config.properties.notification.NotificationsProperties;
import uk.gov.hmcts.reform.unspec.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.unspec.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.unspec.service.NotificationService;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.unspec.sampledata.ServiceMethodBuilder.SERVICE_EMAIL;

@SpringBootTest(classes = {
    ClaimIssueNotificationHandler.class,
    NotificationsProperties.class,
    JacksonAutoConfiguration.class
})
class ClaimIssueNotificationHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private NotificationService notificationService;
    @Autowired
    private NotificationsProperties notificationsProperties;

    @Autowired
    private ClaimIssueNotificationHandler handler;

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldNotifyDefendantSolicitor_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateServiceConfirmed().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                eq(SERVICE_EMAIL),
                eq(notificationsProperties.getDefendantSolicitorClaimIssueEmailTemplate()),
                anyMap(),
                anyString()
            );
        }
    }
}
