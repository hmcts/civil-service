package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_V_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;

@ExtendWith(MockitoExtension.class)
public class NotifyClaimantLipHelpWithFeesNotificationHandlerTest {

    @Mock
    private NotificationService notificationService;
    @Mock
    NotificationsProperties notificationsProperties;
    @InjectMocks
    NotifyClaimantLipHelpWithFeesNotificationHandler handler;

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldNotifyClaimant_whenInvokedAnd1v1() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build()
                .toBuilder().claimantUserDetails(IdamUserDetails.builder().email("claimant@hmcts.net").build()).build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            when(notificationsProperties.getNotifyClaimantLipHelpWithFees())
                .thenReturn("test-template-received-id");
            handler.handle(params);

            verify(notificationService).sendMail(
                "claimant@hmcts.net",
                "test-template-received-id",
                getNotificationDataMap(caseData),
                "notify-claimant-lip-help-with-fees-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyClaimantInWelsh_whenInvokedAnd1v1() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build()
                .toBuilder()
                .claimantUserDetails(IdamUserDetails.builder().email("claimant@hmcts.net").build())
                .claimantBilingualLanguagePreference(Language.BOTH.toString())
                .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            when(notificationsProperties.getNotifyClaimantLipHelpWithFeesWelsh())
                .thenReturn("test-template-received-id-welsh");
            handler.handle(params);

            verify(notificationService).sendMail(
                "claimant@hmcts.net",
                "test-template-received-id-welsh",
                getNotificationDataMap(caseData),
                "notify-claimant-lip-help-with-fees-notification-000DC001"
            );
        }

        @Test
        void shouldReturnCamundaTask() {
            String response = handler.camundaActivityId(CallbackParamsBuilder.builder().build());

            assertThat(response).isEqualTo("NotifyClaimantHwf");
        }

        @NotNull
        private Map<String, String> getNotificationDataMap(CaseData caseData) {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
                CLAIMANT_NAME, "Mr. John Rambo",
                CLAIMANT_V_DEFENDANT, "Mr. John Rambo V Mr. Sole Trader"
            );
        }
    }
}
