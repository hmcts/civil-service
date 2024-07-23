package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import com.launchdarkly.shaded.org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_APPLICANT_SOLICITOR_FOR_OTHER_TRIAL_READY;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_FOR_OTHER_TRIAL_READY;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR2_FOR_OTHER_TRIAL_READY;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_V_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HEARING_DATE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.TrialReadyNotifyOthersHandler.TASK_ID_APPLICANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.TrialReadyNotifyOthersHandler.TASK_ID_RESPONDENT_ONE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.TrialReadyNotifyOthersHandler.TASK_ID_RESPONDENT_TWO;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getAllPartyNames;

@ExtendWith(MockitoExtension.class)
public class TrialReadyNotifyOthersHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private TrialReadyNotifyOthersHandler handler;

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationsProperties notificationsProperties;

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldNotifyApplicantSolicitor_whenInvoked() {
            when(notificationsProperties.getOtherPartyTrialReady()).thenReturn("template-id");

            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(NOTIFY_APPLICANT_SOLICITOR_FOR_OTHER_TRIAL_READY.name()).build()
            ).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "applicantsolicitor@example.com",
                "template-id",
                getNotificationDataMap(caseData),
                "other-party-trial-ready-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyApplicant_whenInvoked() {
            when(notificationsProperties.getNotifyLipUpdateTemplate()).thenReturn("cui-template-id");

            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheckLiP(true).build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(NOTIFY_APPLICANT_SOLICITOR_FOR_OTHER_TRIAL_READY.name()).build()
            ).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "rambo@email.com",
                "cui-template-id",
                getLiPNotificationDataMap(true, caseData),
                "other-party-trial-ready-notification-000MC001"
            );
        }

        @Test
        void shouldNotifyApplicant_whenInvokedBilingual() {
            when(notificationsProperties.getNotifyLipUpdateTemplateBilingual())
                .thenReturn("cui-template-id-bilingual");

            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheckLiP(true)
                .claimantBilingualLanguagePreference(Language.BOTH.toString()).build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(NOTIFY_APPLICANT_SOLICITOR_FOR_OTHER_TRIAL_READY.name()).build()
            ).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "rambo@email.com",
                "cui-template-id-bilingual",
                getLiPNotificationDataMap(true, caseData),
                "other-party-trial-ready-notification-000MC001"
            );
        }

        @Test
        void shouldNotifyApplicantWithNoEmail_whenInvoked() {
            when(notificationsProperties.getNotifyLipUpdateTemplate()).thenReturn("cui-template-id");

            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheckLiP(false)
                .claimantUserDetails(new IdamUserDetails().toBuilder().email("email@email.com").build()).build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(NOTIFY_APPLICANT_SOLICITOR_FOR_OTHER_TRIAL_READY.name()).build()
            ).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "email@email.com",
                "cui-template-id",
                getLiPNotificationDataMap(true, caseData),
                "other-party-trial-ready-notification-000MC001"
            );
        }

        @Test
        void shouldNotifyRespondentSolicitor1_whenInvoked() {
            when(notificationsProperties.getOtherPartyTrialReady()).thenReturn("template-id");

            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(NOTIFY_RESPONDENT_SOLICITOR1_FOR_OTHER_TRIAL_READY.name()).build()
            ).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor@example.com",
                "template-id",
                getNotificationDataMap(caseData),
                "other-party-trial-ready-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondent1_whenInvoked() {
            when(notificationsProperties.getNotifyLipUpdateTemplate()).thenReturn("cui-template-id");

            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheckLiP(true).build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(NOTIFY_RESPONDENT_SOLICITOR1_FOR_OTHER_TRIAL_READY.name()).build()
            ).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "sole.trader@email.com",
                "cui-template-id",
                getLiPNotificationDataMap(false, caseData),
                "other-party-trial-ready-notification-000MC001"
            );
        }

        @Test
        void shouldNotNotifyRespondent1_whenInvokedAndTheEmailAddressIsNotProvided() {
            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheckLiP(false).build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(NOTIFY_RESPONDENT_SOLICITOR1_FOR_OTHER_TRIAL_READY.name()).build()
            ).build();

            handler.handle(params);

            verify(notificationService, never()).sendMail(
                "sole.trader@email.com",
                "cui-template-id",
                getLiPNotificationDataMap(false, caseData),
                "other-party-trial-ready-notification-000MC001"
            );
        }

        @Test
        void shouldNotifyRespondentSolicitor2_whenInvoked() {
            when(notificationsProperties.getOtherPartyTrialReady()).thenReturn("template-id");

            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(NOTIFY_RESPONDENT_SOLICITOR2_FOR_OTHER_TRIAL_READY.name()).build()
            ).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor2@example.com",
                "template-id",
                getNotificationDataMap(caseData),
                "other-party-trial-ready-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondent2_whenInvoked() {
            when(notificationsProperties.getNotifyLipUpdateTemplate()).thenReturn("cui-template-id");

            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheckLiP(true).build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(NOTIFY_RESPONDENT_SOLICITOR2_FOR_OTHER_TRIAL_READY.name()).build()
            ).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "rambo@email.com",
                "cui-template-id",
                getLiPNotificationDataMap(false, caseData),
                "other-party-trial-ready-notification-000MC001"
            );
        }

        @Test
        void shouldReturnCorrectCamundaActivityId_whenInvoked() {
            assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(
                CallbackRequest.builder().eventId(
                    NOTIFY_APPLICANT_SOLICITOR_FOR_OTHER_TRIAL_READY.name()).build()).build()))
                .isEqualTo(TASK_ID_APPLICANT);

            assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(
                CallbackRequest.builder().eventId(
                    NOTIFY_RESPONDENT_SOLICITOR1_FOR_OTHER_TRIAL_READY.name()).build()).build()))
                .isEqualTo(TASK_ID_RESPONDENT_ONE);

            assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(
                CallbackRequest.builder().eventId(
                    NOTIFY_RESPONDENT_SOLICITOR2_FOR_OTHER_TRIAL_READY.name()).build()).build()))
                .isEqualTo(TASK_ID_RESPONDENT_TWO);
        }

        @NotNull
        private Map<String, String> getNotificationDataMap(CaseData caseData) {
            return Map.of(
                HEARING_DATE, formatLocalDate(caseData.getHearingDate(), DATE),
                CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference()
            );
        }

        @NotNull
        private Map<String, String> getLiPNotificationDataMap(boolean isApplicant, CaseData caseData) {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
                PARTY_NAME, isApplicant ? caseData.getApplicant1().getPartyName() : caseData.getRespondent1().getPartyName(),
                CLAIMANT_V_DEFENDANT, getAllPartyNames(caseData)
            );
        }
    }
}
