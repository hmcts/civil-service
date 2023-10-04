package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_FOR_TRIAL_READY;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR2_FOR_TRIAL_READY;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_V_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HEARING_DATE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.TrialReadyRespondentNotificationHandler.TASK_ID_RESPONDENT_ONE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.TrialReadyRespondentNotificationHandler.TASK_ID_RESPONDENT_TWO;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getAllPartyNames;

@SpringBootTest(classes = {
    TrialReadyRespondentNotificationHandler.class,
    JacksonAutoConfiguration.class
})
class TrialReadyRespondentNotificationHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private NotificationService notificationService;
    @MockBean
    private NotificationsProperties notificationsProperties;
    @Autowired
    private TrialReadyRespondentNotificationHandler handler;

    private boolean isRespondentSolicitor1;

    @Nested
    class AboutToSubmitCallback {

        @BeforeEach
        void setup() {
            when(notificationsProperties.getSolicitorTrialReady()).thenReturn("template-id");
            when(notificationsProperties.getNotifyLipUpdateTemplate()).thenReturn("cui-template-id");
        }

        @Test
        void shouldNotifyRespondentSolicitorOne_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(NOTIFY_RESPONDENT_SOLICITOR1_FOR_TRIAL_READY.name()).build()
            ).build();

            handler.handle(params);
            isRespondentSolicitor1 = true;

            verify(notificationService).sendMail(
                "respondentsolicitor@example.com",
                "template-id",
                getNotificationDataMap(caseData),
                "trial-ready-respondent-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondentSolicitorTwo_whenInvokedWithDiffSol() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateTrialReadyCheck(ONE_V_TWO_TWO_LEGAL_REP)
                .respondent2SameLegalRepresentative(YesOrNo.NO)
                .respondentSolicitor2EmailAddress(null).build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(NOTIFY_RESPONDENT_SOLICITOR2_FOR_TRIAL_READY.name()).build()
            ).build();

            handler.handle(params);
            isRespondentSolicitor1 = false;

            verify(notificationService).sendMail(
                null,
                "template-id",
                getNotificationDataMap(caseData),
                "trial-ready-respondent-notification-000DC001"
            );
        }

        @Test
        void shouldNotNotifyRespondent_whenInvokedWithNoSolicitorRepresentedAndNoEmail() {
            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck()
                .respondent1(PartyBuilder.builder().company().partyEmail(null).build())
                .respondent1Represented(null)
                .specRespondent1Represented(YesOrNo.NO)
                .caseAccessCategory(CaseCategory.SPEC_CLAIM)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId("NOTIFY_RESPONDENT_SOLICITOR1_FOR_TRIAL_READY")
                    .build()).build();

            handler.handle(params);

            verify(notificationService, never()).sendMail(any(), any(), any(), any());
        }

        @Test
        void shouldNotNotifyRespondent_whenInvokedWithNoSolicitorRepresentedWithEmail() {
            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck()
                .respondent1Represented(null)
                .specRespondent1Represented(YesOrNo.NO)
                .caseAccessCategory(CaseCategory.SPEC_CLAIM)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId("NOTIFY_RESPONDENT_SOLICITOR1_FOR_TRIAL_READY")
                    .build()).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "sole.trader@email.com",
                "cui-template-id",
                getNotificationDataMapLRvLiP(caseData),
                "trial-ready-respondent-notification-000DC001"
            );
        }

        private Map<String, String> getNotificationDataMapLRvLiP(CaseData caseData) {

            return Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
                PARTY_NAME, caseData.getRespondent1().getPartyName(),
                CLAIMANT_V_DEFENDANT, getAllPartyNames(caseData)
            );
        }

        @Test
        void shouldReturnCorrectCamundaActivityId_whenInvoked() {
            assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(
                CallbackRequest.builder().eventId(
                NOTIFY_RESPONDENT_SOLICITOR1_FOR_TRIAL_READY.name()).build()).build()))
                .isEqualTo(TASK_ID_RESPONDENT_ONE);

            assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(
                CallbackRequest.builder().eventId(
                NOTIFY_RESPONDENT_SOLICITOR2_FOR_TRIAL_READY.name()).build()).build()))
                .isEqualTo(TASK_ID_RESPONDENT_TWO);
        }

        @NotNull
        private Map<String, String> getNotificationDataMap(CaseData caseData) {
            if (isRespondentSolicitor1 == false) {
                return Map.of(
                    HEARING_DATE, formatLocalDate(caseData.getHearingDate(), DATE),
                    CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference()
                );
            } else {
                return Map.of(
                    HEARING_DATE, formatLocalDate(caseData.getHearingDate(), DATE),
                    CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference()
                );
            }
        }
    }
}
