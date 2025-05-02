package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASEMAN_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_V_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HMCTS_SIGNATURE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.OPENING_HOURS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PHONE_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.SPEC_UNSPEC_CONTACT;
import static uk.gov.hmcts.reform.civil.utils.HearingUtils.getClaimantVDefendant;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;

@ExtendWith(MockitoExtension.class)
class ClaimReconsiderationUpheldDefendantNotificationHandlerTest extends BaseCallbackHandlerTest {

    public static final String TEMPLATE_ID = "template-id";

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private NotificationsSignatureConfiguration configuration;

    @Captor
    private ArgumentCaptor<String> targetEmail;

    @Captor
    private ArgumentCaptor<String> emailTemplate;

    @Captor
    private ArgumentCaptor<Map<String, String>> notificationDataMap;

    @Captor
    private ArgumentCaptor<String> reference;

    @InjectMocks
    private ClaimReconsiderationUpheldDefendantNotificationHandler handler;

    @Nested
    class AboutToSubmitCallback {

        @BeforeEach
        void setUp() {
            when(configuration.getHmctsSignature()).thenReturn("Online Civil Claims \n HM Courts & Tribunal Service");
            when(configuration.getPhoneContact()).thenReturn("For anything related to hearings, call 0300 123 5577 "
                                                                 + "\n For all other matters, call 0300 123 7050");
            when(configuration.getOpeningHours()).thenReturn("Monday to Friday, 8.30am to 5pm");
            when(configuration.getSpecUnspecContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk "
                                                                      + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");
        }

        @Test
        void shouldNotifyDefendantSolicitor_whenInvoked() {
            when(notificationsProperties.getNotifyClaimReconsiderationLRTemplate()).thenReturn(TEMPLATE_ID);

            CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified_1v1().build();

            CallbackParams params = CallbackParams.builder()
                .caseData(caseData)
                .type(ABOUT_TO_SUBMIT)
                .request(CallbackRequest.builder()
                             .eventId(CaseEvent.NOTIFY_CLAIM_RECONSIDERATION_UPHELD_DEFENDANT.name())
                             .build())
                .build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor@example.com",
                TEMPLATE_ID,
                getNotificationDataMap(caseData),
                "reconsideration-upheld-applicant-notification-1594901956117591"
            );
        }

        @Test
        void shouldNotifyDefendantBothSolicitors_whenInvoked() {
            when(notificationsProperties.getNotifyClaimReconsiderationLRTemplate()).thenReturn(TEMPLATE_ID);

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified_1v2_andNotifyBothSolicitors().build();

            CallbackParams params = CallbackParams.builder()
                .caseData(caseData)
                .type(ABOUT_TO_SUBMIT)
                .request(CallbackRequest.builder()
                             .eventId(CaseEvent.NOTIFY_CLAIM_RECONSIDERATION_UPHELD_DEFENDANT.name())
                             .build())
                .build();

            handler.handle(params);

            verify(notificationService, times(2)).sendMail(
                targetEmail.capture(),
                emailTemplate.capture(),
                notificationDataMap.capture(),
                reference.capture()
            );
            //Email to respondent1
            assertThat(targetEmail.getAllValues().get(0)).isEqualTo("respondentsolicitor@example.com");
            assertThat(emailTemplate.getAllValues().get(0)).isEqualTo("template-id");
            assertThat(notificationDataMap.getAllValues().get(0)).isEqualTo(getNotificationDataMap(caseData));
            assertThat(reference.getAllValues().get(0)).isEqualTo("reconsideration-upheld-applicant-notification-1594901956117591");
            //Email to respondent2
            assertThat(targetEmail.getAllValues().get(1)).isEqualTo("respondentsolicitor2@example.com");
            assertThat(emailTemplate.getAllValues().get(1)).isEqualTo("template-id");
            assertThat(notificationDataMap.getAllValues().get(1)).isEqualTo(getNotificationDataMap2(caseData));
            assertThat(reference.getAllValues().get(1)).isEqualTo("reconsideration-upheld-applicant-notification-1594901956117591");

        }

        @Test
        void shouldNotifyDefendant_whenInvoked() {
            when(notificationsProperties.getNotifyClaimReconsiderationLRTemplate()).thenReturn(TEMPLATE_ID);

            CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified_1v1()
                .respondent1Represented(NO)
                .build();

            CallbackParams params = CallbackParams.builder()
                .caseData(caseData)
                .type(ABOUT_TO_SUBMIT)
                .request(CallbackRequest.builder()
                             .eventId(CaseEvent.NOTIFY_CLAIM_RECONSIDERATION_UPHELD_DEFENDANT.name())
                             .build())
                .build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "sole.trader@email.com",
                TEMPLATE_ID,
                getNotificationDataMap(caseData),
                "reconsideration-upheld-applicant-notification-1594901956117591"
            );
        }

    }

    @NotNull
    private Map<String, String> getNotificationDataMap(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            CLAIMANT_V_DEFENDANT, getClaimantVDefendant(caseData),
            PARTY_NAME, caseData.getRespondent1().getPartyName(),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference(),
            PHONE_CONTACT, "For anything related to hearings, call 0300 123 5577 \n For all other matters, call 0300 123 7050",
            OPENING_HOURS, "Monday to Friday, 8.30am to 5pm",
            SPEC_UNSPEC_CONTACT, "Email for Specified Claims: contactocmc@justice.gov.uk \n Email for Damages Claims: damagesclaims@justice.gov.uk",
            HMCTS_SIGNATURE, "Online Civil Claims \n HM Courts & Tribunal Service"
        );
    }

    @NotNull
    private Map<String, String> getNotificationDataMap2(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            CLAIMANT_V_DEFENDANT, getClaimantVDefendant(caseData),
            PARTY_NAME, caseData.getRespondent2().getPartyName(),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference(),
            PHONE_CONTACT, "For anything related to hearings, call 0300 123 5577 \n For all other matters, call 0300 123 7050",
            OPENING_HOURS, "Monday to Friday, 8.30am to 5pm",
            SPEC_UNSPEC_CONTACT, "Email for Specified Claims: contactocmc@justice.gov.uk \n Email for Damages Claims: damagesclaims@justice.gov.uk",
            HMCTS_SIGNATURE, "Online Civil Claims \n HM Courts & Tribunal Service"
        );
    }

}
