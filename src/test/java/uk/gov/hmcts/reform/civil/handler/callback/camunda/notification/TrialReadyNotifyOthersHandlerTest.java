package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import com.launchdarkly.shaded.org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_APPLICANT_SOLICITOR_FOR_OTHER_TRIAL_READY;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_FOR_OTHER_TRIAL_READY;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR2_FOR_OTHER_TRIAL_READY;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASEMAN_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_V_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CNBC_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HEARING_DATE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HMCTS_SIGNATURE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LIP_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LIP_CONTACT_WELSH;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.OPENING_HOURS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PHONE_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.SPEC_UNSPEC_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_HMCTS_SIGNATURE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_OPENING_HOURS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_PHONE_CONTACT;
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

    @Mock
    private OrganisationService organisationService;

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private NotificationsSignatureConfiguration configuration;

    @Nested
    class AboutToSubmitCallback {

        @BeforeEach
        void setUp() {
            Map<String, Object> configMap = YamlNotificationTestUtil.loadNotificationsConfig();
            when(configuration.getHmctsSignature()).thenReturn((String) configMap.get("hmctsSignature"));
            when(configuration.getPhoneContact()).thenReturn((String) configMap.get("phoneContact"));
            when(configuration.getOpeningHours()).thenReturn((String) configMap.get("openingHours"));
            when(configuration.getWelshHmctsSignature()).thenReturn((String) configMap.get("welshHmctsSignature"));
            when(configuration.getWelshPhoneContact()).thenReturn((String) configMap.get("welshPhoneContact"));
            when(configuration.getWelshOpeningHours()).thenReturn((String) configMap.get("welshOpeningHours"));
            when(configuration.getLipContactEmail()).thenReturn((String) configMap.get("lipContactEmail"));
            when(configuration.getLipContactEmailWelsh()).thenReturn((String) configMap.get("lipContactEmailWelsh"));
        }

        @Test
        void shouldNotifyApplicantSolicitor_whenInvoked() {
            when(notificationsProperties.getOtherPartyTrialReady()).thenReturn("template-id");
            Map<String, Object> configMap = YamlNotificationTestUtil.loadNotificationsConfig();
            when(configuration.getRaiseQueryLr()).thenReturn((String) configMap.get("raiseQueryLr"));
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
            Map<String, Object> configMap = YamlNotificationTestUtil.loadNotificationsConfig();
            when(configuration.getCnbcContact()).thenReturn((String) configMap.get("cnbcContact"));
            when(configuration.getSpecUnspecContact()).thenReturn((String) configMap.get("specUnspecContact"));
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
            Map<String, Object> configMap = YamlNotificationTestUtil.loadNotificationsConfig();
            when(configuration.getCnbcContact()).thenReturn((String) configMap.get("cnbcContact"));
            when(configuration.getSpecUnspecContact()).thenReturn((String) configMap.get("specUnspecContact"));
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
            Map<String, Object> configMap = YamlNotificationTestUtil.loadNotificationsConfig();
            when(configuration.getCnbcContact()).thenReturn((String) configMap.get("cnbcContact"));
            when(configuration.getSpecUnspecContact()).thenReturn((String) configMap.get("specUnspecContact"));
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
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name("Signer Name").build()));
            Map<String, Object> configMap = YamlNotificationTestUtil.loadNotificationsConfig();
            when(configuration.getRaiseQueryLr()).thenReturn((String) configMap.get("raiseQueryLr"));
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
            Map<String, Object> configMap = YamlNotificationTestUtil.loadNotificationsConfig();
            when(configuration.getCnbcContact()).thenReturn((String) configMap.get("cnbcContact"));
            when(configuration.getSpecUnspecContact()).thenReturn((String) configMap.get("specUnspecContact"));
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
            Map<String, Object> configMap = YamlNotificationTestUtil.loadNotificationsConfig();
            when(configuration.getCnbcContact()).thenReturn((String) configMap.get("cnbcContact"));
            when(configuration.getSpecUnspecContact()).thenReturn((String) configMap.get("specUnspecContact"));
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
            Map<String, Object> configMap = YamlNotificationTestUtil.loadNotificationsConfig();
            when(configuration.getRaiseQueryLr()).thenReturn((String) configMap.get("raiseQueryLr"));
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name("Signer Name").build()));

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
            Map<String, Object> configMap = YamlNotificationTestUtil.loadNotificationsConfig();
            when(configuration.getCnbcContact()).thenReturn((String) configMap.get("cnbcContact"));
            when(configuration.getSpecUnspecContact()).thenReturn((String) configMap.get("specUnspecContact"));
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

        @NotNull
        private Map<String, String> getNotificationDataMap(CaseData caseData) {
            Map<String, String> properties = new HashMap<>(addCommonProperties());
            properties.put(HEARING_DATE, formatLocalDate(caseData.getHearingDate(), DATE));
            properties.put(CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString());
            properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, "Signer Name");
            properties.put(PARTY_REFERENCES, "Claimant reference: 12345 - Defendant reference: 6789");
            properties.put(CASEMAN_REF, "000DC001");
            properties.put(SPEC_UNSPEC_CONTACT, configuration.getRaiseQueryLr());
            properties.put(CNBC_CONTACT, configuration.getRaiseQueryLr());
            return properties;
        }

        @NotNull
        private Map<String, String> getLiPNotificationDataMap(boolean isApplicant, CaseData caseData) {
            Map<String, String> properties = new HashMap<>(addCommonProperties());
            properties.put(CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString());
            properties.put(PARTY_NAME, isApplicant ? caseData.getApplicant1().getPartyName() : caseData.getRespondent1().getPartyName());
            properties.put(CLAIMANT_V_DEFENDANT, getAllPartyNames(caseData));
            properties.put(CASEMAN_REF, "000MC001");
            properties.put(SPEC_UNSPEC_CONTACT, configuration.getSpecUnspecContact());
            properties.put(CNBC_CONTACT, configuration.getCnbcContact());
            return properties;
        }

        @NotNull
        public Map<String, String> addCommonProperties() {
            Map<String, String> expectedProperties = new HashMap<>();
            expectedProperties.put(PHONE_CONTACT, configuration.getPhoneContact());
            expectedProperties.put(OPENING_HOURS, configuration.getOpeningHours());
            expectedProperties.put(HMCTS_SIGNATURE, configuration.getHmctsSignature());
            expectedProperties.put(WELSH_PHONE_CONTACT, configuration.getWelshPhoneContact());
            expectedProperties.put(WELSH_OPENING_HOURS, configuration.getWelshOpeningHours());
            expectedProperties.put(WELSH_HMCTS_SIGNATURE, configuration.getWelshHmctsSignature());
            expectedProperties.put(LIP_CONTACT, configuration.getLipContactEmail());
            expectedProperties.put(LIP_CONTACT_WELSH, configuration.getLipContactEmailWelsh());
            return expectedProperties;
        }

    }

    private static Stream<Arguments> provideCaseData() {
        return Stream.of(
            Arguments.of(
                CaseDataBuilder.builder().atStateTrialReadyCheckLiP(true).build()
                    .toBuilder().trialReadyApplicant(YesOrNo.YES).build(),
                NOTIFY_APPLICANT_SOLICITOR_FOR_OTHER_TRIAL_READY.name()
            ),
            Arguments.of(
                CaseDataBuilder.builder().atStateTrialReadyCheckLiP(true).build()
                    .toBuilder().trialReadyRespondent1(YesOrNo.YES).build(),
                NOTIFY_RESPONDENT_SOLICITOR1_FOR_OTHER_TRIAL_READY.name()
            ),
            Arguments.of(
                CaseDataBuilder.builder().atStateTrialReadyCheckLiP(true).build()
                    .toBuilder().trialReadyRespondent2(YesOrNo.YES).build(),
                NOTIFY_RESPONDENT_SOLICITOR2_FOR_OTHER_TRIAL_READY.name()
            )
        );
    }

    @ParameterizedTest
    @MethodSource("provideCaseData")
    void shouldNotSendEmailWhenTrialReadyFieldsAreNotNull(CaseData caseData, String eventId) {
        CallbackParams params = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .request(CallbackRequest.builder().eventId(eventId).build())
            .build();

        handler.handle(params);

        verify(notificationService, never()).sendMail(
            anyString(),
            anyString(),
            anyMap(),
            anyString()
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
}
