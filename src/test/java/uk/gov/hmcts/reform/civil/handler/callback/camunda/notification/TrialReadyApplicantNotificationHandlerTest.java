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
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.*;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_V_DEFENDANT;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getAllPartyNames;

@SpringBootTest(classes = {
    TrialReadyApplicantNotificationHandler.class,
    JacksonAutoConfiguration.class
})
class TrialReadyApplicantNotificationHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private NotificationService notificationService;
    @MockBean
    private NotificationsProperties notificationsProperties;
    @Autowired
    private TrialReadyApplicantNotificationHandler handler;

    @Nested
    class AboutToSubmitCallback {

        @BeforeEach
        void setup() {
            when(notificationsProperties.getSolicitorTrialReady()).thenReturn("template-id");
            when(notificationsProperties.getNotifyLipUpdateTemplate()).thenReturn("cui-template-id");
        }

        @Test
        void shouldNotifyApplicantSolicitor_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "applicantsolicitor@example.com",
                "template-id",
                getNotificationDataMap(caseData),
                "trial-ready-applicant-notification-000DC001"
            );
        }

        @Test
        void shouldNotNotifyApplicant_whenInvokedWithNoSolicitorRepresentedWithNoEmail() {
            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck()
                .applicant1(PartyBuilder.builder().company().partyEmail(null).build())
                .claimantUserDetails(new IdamUserDetails().toBuilder().email("email@email.com").build())
                .applicant1Represented(YesOrNo.NO)
                .caseAccessCategory(CaseCategory.SPEC_CLAIM)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "email@email.com",
                "cui-template-id",
                getNotificationDataMapLiPvLiP(caseData),
                "trial-ready-applicant-notification-000DC001"
            );
        }

        @Test
        void shouldNotNotifyApplicant_whenInvokedWithNoSolicitorRepresentedWithEmail() {
            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck()
                .applicant1Represented(YesOrNo.NO)
                .caseAccessCategory(CaseCategory.SPEC_CLAIM)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "rambo@email.com",
                "cui-template-id",
                getNotificationDataMapLiPvLiP(caseData),
                "trial-ready-applicant-notification-000DC001"
            );
        }

        private Map<String, String> getNotificationDataMapLiPvLiP(CaseData caseData) {

            return Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
                PARTY_NAME, caseData.getApplicant1().getPartyName(),
                CLAIMANT_V_DEFENDANT, getAllPartyNames(caseData)
            );
        }

        @NotNull
        private Map<String, String> getNotificationDataMap(CaseData caseData) {
            return Map.of(
                HEARING_DATE, formatLocalDate(caseData.getHearingDate(), DATE),
                CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference()
            );
        }
    }
}
