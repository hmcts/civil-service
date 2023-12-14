package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.flowstate.StateFlowEngine;

import java.time.LocalDateTime;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.LEGACY_CASE_REFERENCE;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.buildPartiesReferences;

@SpringBootTest(classes = {
    ClaimDismissedApplicantNotificationHandler.class,
    NotificationsProperties.class,
    CaseDetailsConverter.class,
    StateFlowEngine.class,
    JacksonAutoConfiguration.class
})
class ClaimDismissedApplicantNotificationHandlerTest {

    public static final String TEMPLATE_ID_1 = "template-id-1";
    public static final String TEMPLATE_ID_2 = "template-id-2";
    public static final String TEMPLATE_ID_3 = "template-id-3";
    @MockBean
    private NotificationService notificationService;
    @MockBean
    private FeatureToggleService featureToggleService;
    @MockBean
    private NotificationsProperties notificationsProperties;

    @Autowired
    private ClaimDismissedApplicantNotificationHandler handler;

    @Nested
    class AboutToSubmitCallback {

        @BeforeEach
        void setup() {
            when(notificationsProperties.getSolicitorClaimDismissedWithinDeadline()).thenReturn(TEMPLATE_ID_1);
            when(notificationsProperties.getSolicitorClaimDismissedWithin14Days()).thenReturn(TEMPLATE_ID_3);
            when(notificationsProperties.getSolicitorClaimDismissedWithin4Months()).thenReturn(TEMPLATE_ID_2);

        }

        @Test
        void shouldNotifyApplicantSolicitor_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();
            handler.handle(params);

            verify(notificationService).sendMail(
                "applicantsolicitor@example.com",
                TEMPLATE_ID_1,
                getNotificationDataMap(caseData),
                "claim-dismissed-applicant-notification-000DC001"
            );
        }
    }

    @Test
    void shouldNotifyApplicantSolicitor_whenCaseDataAtStateClaimAcknowledgeAndCcdStateIsDismissed() {
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .claimDismissedDate(LocalDateTime.now())
            .claimDismissedDeadline(LocalDateTime.now().minusHours(4))
            .build();

        CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();
        when(notificationsProperties.getSolicitorClaimDismissedWithinDeadline()).thenReturn(TEMPLATE_ID_1);
        handler.handle(params);

        verify(notificationService).sendMail(
            "applicantsolicitor@example.com",
            TEMPLATE_ID_1,
            getNotificationDataMap(caseData),
            "claim-dismissed-applicant-notification-000DC001"
        );
    }

    @Test
    void shouldNotifyApplicantSolicitor_whenPastClaimNotificationDeadline() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimDismissedPastClaimNotificationDeadline()
            .build();

        CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();
        when(notificationsProperties.getSolicitorClaimDismissedWithin4Months()).thenReturn(TEMPLATE_ID_2);

        handler.handle(params);

        verify(notificationService).sendMail(
            "applicantsolicitor@example.com",
            TEMPLATE_ID_2,
            getNotificationDataMap(caseData),
            "claim-dismissed-applicant-notification-000DC001"
        );
    }

    @Test
    void shouldNotifyApplicantSolicitor_whenCaseDataIsPastClaimDetailsNotification() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimDismissedPastClaimDetailsNotificationDeadline()
            .build();

        CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();
        when(notificationsProperties.getSolicitorClaimDismissedWithin14Days()).thenReturn(TEMPLATE_ID_3);

        handler.handle(params);

        verify(notificationService).sendMail(
            "applicantsolicitor@example.com",
            TEMPLATE_ID_3,
            getNotificationDataMap(caseData),
            "claim-dismissed-applicant-notification-000DC001"
        );
    }

    @NotNull
    private Map<String, String> getNotificationDataMap(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, LEGACY_CASE_REFERENCE,
            PARTY_REFERENCES, buildPartiesReferences(caseData)
        );
    }

}
