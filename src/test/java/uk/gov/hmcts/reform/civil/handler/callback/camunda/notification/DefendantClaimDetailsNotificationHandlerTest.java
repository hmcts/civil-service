package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.config.properties.notification.NotificationsProperties;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.service.NotificationService;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_DETAILS;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_DETAILS_CC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.DefendantClaimDetailsNotificationHandler.TASK_ID_EMAIL_APP_SOL_CC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.DefendantClaimDetailsNotificationHandler.TASK_ID_EMAIL_FIRST_SOL;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONSE_DEADLINE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONSE_DEADLINE_PLUS_28;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.buildPartiesReferences;

@SpringBootTest(classes = {
    DefendantClaimDetailsNotificationHandler.class,
    JacksonAutoConfiguration.class,
})
class DefendantClaimDetailsNotificationHandlerTest extends BaseCallbackHandlerTest {

    public static final String REFERENCE = "claim-details-respondent-notification-000DC001";
    public static final String templateId = "17151a5a-00b1-48b7-8e45-38a5f20b6ec0";
    @MockBean
    private NotificationService notificationService;

    @MockBean
    private NotificationsProperties notificationsProperties;

    @Autowired
    private DefendantClaimDetailsNotificationHandler handler;

    @Nested
    class AboutToSubmitCallback {

        @BeforeEach
        void setup() {
            when(notificationsProperties.getRespondentSolicitorClaimDetailsEmailTemplate())
                .thenReturn(templateId);
        }

        private Map<String, String> getNotificationDataMap(CaseData caseData) {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
                RESPONSE_DEADLINE, formatLocalDate(caseData.getClaimDetailsNotificationDeadline().toLocalDate(), DATE),
                RESPONSE_DEADLINE_PLUS_28, formatLocalDate(caseData.getRespondent1ResponseDeadline().plusDays(28)
                                                                                 .toLocalDate(), DATE),
                PARTY_REFERENCES, buildPartiesReferences(caseData)
            );
        }

    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler
                       .camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
            NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_DETAILS.name()).build()).build()))
            .isEqualTo(TASK_ID_EMAIL_FIRST_SOL);

        assertThat(handler
                       .camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
            NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_DETAILS_CC.name()).build()).build()))
            .isEqualTo(TASK_ID_EMAIL_APP_SOL_CC);
    }
}
