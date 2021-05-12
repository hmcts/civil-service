<<<<<<< HEAD:src/test/java/uk/gov/hmcts/reform/civil/handler/callback/camunda/notification/ClaimDismissedRespondentNotificationHandlerTest.java
package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;
=======
package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.claimdismissed;
>>>>>>> master:src/test/java/uk/gov/hmcts/reform/civil/handler/callback/camunda/notification/claimdismissed/ApplicantClaimDismissedNotificationHandlerTest.java

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
<<<<<<< HEAD:src/test/java/uk/gov/hmcts/reform/civil/handler/callback/camunda/notification/ClaimDismissedRespondentNotificationHandlerTest.java
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.config.properties.notification.NotificationsProperties;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.NotificationService;
=======
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.config.properties.notification.NotificationsProperties;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.NotificationService;
>>>>>>> master:src/test/java/uk/gov/hmcts/reform/civil/handler/callback/camunda/notification/claimdismissed/ApplicantClaimDismissedNotificationHandlerTest.java

import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
<<<<<<< HEAD:src/test/java/uk/gov/hmcts/reform/civil/handler/callback/camunda/notification/ClaimDismissedRespondentNotificationHandlerTest.java
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.LEGACY_CASE_REFERENCE;
=======
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
>>>>>>> master:src/test/java/uk/gov/hmcts/reform/civil/handler/callback/camunda/notification/claimdismissed/ApplicantClaimDismissedNotificationHandlerTest.java

@SpringBootTest(classes = {
    ClaimDismissedRespondentNotificationHandler.class,
    NotificationsProperties.class,
    JacksonAutoConfiguration.class
})
class ClaimDismissedRespondentNotificationHandlerTest {

    public static final String TEMPLATE_ID = "template-id";

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private NotificationsProperties notificationsProperties;

    @Autowired
    private ClaimDismissedRespondentNotificationHandler handler;

    @Nested
    class AboutToSubmitCallback {

        @BeforeEach
        void setup() {
            when(notificationsProperties.getSolicitorClaimDismissed()).thenReturn(TEMPLATE_ID);
        }

        @Test
        void shouldNotifyApplicantSolicitor_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor@example.com",
                TEMPLATE_ID,
                getNotificationDataMap(caseData),
                "claim-dismissed-respondent-notification-000DC001"
            );
        }
    }

    @NotNull
    private Map<String, String> getNotificationDataMap(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, LEGACY_CASE_REFERENCE,
            "frontendBaseUrl", "https://www.MyHMCTS.gov.uk"
        );
    }

}
