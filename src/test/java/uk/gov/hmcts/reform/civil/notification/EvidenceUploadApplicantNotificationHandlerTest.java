package uk.gov.hmcts.reform.civil.notification;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.notify.NotificationService;

import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;

@SpringBootTest(classes = {
    EvidenceUploadApplicantNotificationHandler.class,
    JacksonAutoConfiguration.class
})
class EvidenceUploadApplicantNotificationHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private NotificationService notificationService;
    @MockBean
    private NotificationsProperties notificationsProperties;
    @Autowired
    private EvidenceUploadApplicantNotificationHandler handler;

    @Nested
    class AboutToSubmitCallback {

        @BeforeEach
        void setup() {
            when(notificationsProperties.getEvidenceUploadTemplate()).thenReturn("template-id");
        }

        @Test
        void shouldNotifyApplicantSolicitor_whenInvoked() {
            //given: case where applicant solicitor has email as applicantsolicitor@example.com
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            //when: ApplicantNotificationhandler is called
            handler.notifyApplicantEvidenceUpload(caseData);
            //then: email should be sent to applicant solicitor
            verify(notificationService).sendMail(
                    "applicantsolicitor@example.com",
                    "template-id",
                    getNotificationDataMap(caseData),
                    "evidence-upload-notification-000DC001"
            );
        }

        @NotNull
        private Map<String, String> getNotificationDataMap(CaseData caseData) {
            return Map.of(
                    CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference()
            );
        }
    }
}
