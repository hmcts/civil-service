package uk.gov.hmcts.reform.unspec.handler.callback.notification;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.unspec.callback.CallbackParams;
import uk.gov.hmcts.reform.unspec.callback.CallbackType;
import uk.gov.hmcts.reform.unspec.config.properties.notification.NotificationsProperties;
import uk.gov.hmcts.reform.unspec.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.unspec.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.unspec.model.BusinessProcess;
import uk.gov.hmcts.reform.unspec.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.unspec.service.NotificationService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.unspec.handler.callback.notification.ClaimIssueNotificationHandler.NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_ISSUE_TASK_ID;

@SpringBootTest(classes = {
    ClaimIssueNotificationHandler.class,
    CaseDetailsConverter.class,
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
            String solicitorEmail = "solicitor@example.com";
            Map<String, Object> data = Map.of(
                "businessProcess",
                BusinessProcess.builder().activityId(NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_ISSUE_TASK_ID).build(),
                "serviceMethodToRespondentSolicitor1",
                Map.of("email", solicitorEmail),
                "legacyCaseReference", "000LR001",
                "applicant1", PartyBuilder.builder().individual().build(),
                "claimIssuedDate", LocalDate.now(),
                "respondentSolicitor1ResponseDeadline", LocalDateTime.now()
            );

            CallbackParams params = callbackParamsOf(data, CallbackType.ABOUT_TO_SUBMIT);

            handler.handle(params);

            verify(notificationService).sendMail(
                eq(solicitorEmail),
                eq(notificationsProperties.getDefendantSolicitorClaimIssueEmailTemplate()),
                anyMap(),
                anyString()
            );
        }
    }
}
