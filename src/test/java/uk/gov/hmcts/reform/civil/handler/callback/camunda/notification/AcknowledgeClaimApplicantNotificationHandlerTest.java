package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.ResponseIntention.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.enums.ResponseIntention.PART_DEFENCE;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.AcknowledgeClaimApplicantNotificationHandler.TASK_ID;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.AcknowledgeClaimApplicantNotificationHandler.TASK_ID_CC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASEMAN_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CNBC_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HMCTS_SIGNATURE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LIP_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LIP_CONTACT_WELSH;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.OPENING_HOURS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PHONE_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONSE_DEADLINE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONSE_INTENTION;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.SPEC_UNSPEC_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_HMCTS_SIGNATURE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_OPENING_HOURS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_PHONE_CONTACT;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getResponseIntentionForEmail;

@ExtendWith(MockitoExtension.class)
class AcknowledgeClaimApplicantNotificationHandlerTest extends BaseCallbackHandlerTest {

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
    @InjectMocks
    private AcknowledgeClaimApplicantNotificationHandler handler;

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
            when(configuration.getRaiseQueryLr()).thenReturn((String) configMap.get("raiseQueryLr"));
        }

        @Test
        void shouldNotifyApplicantSolicitor_whenInvoked() {
            when(notificationsProperties.getRespondentSolicitorAcknowledgeClaim()).thenReturn("template-id");
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId("NOTIFY_APPLICANT_SOLICITOR1_FOR_CLAIM_ACKNOWLEDGEMENT").build())
                .build();
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name("org name").build()));
            handler.handle(params);

            verify(notificationService).sendMail(
                "applicantsolicitor@example.com",
                "template-id",
                getNotificationDataMap(caseData),
                "acknowledge-claim-applicant-notification-000DC001"
            );
        }

        @Test
        void shouldNotNotifyApplicantSolicitor_whenRecipientIsNull() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateNotificationAcknowledged()
                .applicantSolicitor1UserDetails(IdamUserDetails.builder().email(null).build())
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                    CallbackRequest.builder().eventId("NOTIFY_APPLICANT_SOLICITOR1_FOR_CLAIM_ACKNOWLEDGEMENT").build())
                .build();
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name("org name").build()));
            handler.handle(params);

            assertThatNoException();
        }

        @Test
        void shouldNotNotifyRespondentSolicitor_whenRecipient1IsNull() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateNotificationAcknowledged()
                .respondentSolicitor1EmailAddress(null)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                    CallbackRequest.builder().eventId("NOTIFY_APPLICANT_SOLICITOR1_FOR_CLAIM_ACKNOWLEDGEMENT").build())
                .build();
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name("org name").build()));
            handler.handle(params);

            assertThatNoException();
        }

        @Test
        void shouldNotNotifyRespondentSolicitor_whenRecipient2IsNull() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateNotificationAcknowledged()
                .respondentSolicitor2EmailAddress(null)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                    CallbackRequest.builder().eventId("NOTIFY_APPLICANT_SOLICITOR1_FOR_CLAIM_ACKNOWLEDGEMENT").build())
                .build();
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name("org name").build()));
            handler.handle(params);

            assertThatNoException();
        }

        @Test
        void shouldNotifyRespondentSolicitor_whenInvokedWithCcEvent() {
            when(notificationsProperties.getRespondentSolicitorAcknowledgeClaim()).thenReturn("template-id");
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId("NOTIFY_APPLICANT_SOLICITOR1_FOR_CLAIM_ACKNOWLEDGEMENT_CC").build())
                .build();
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name("org name").build()));
            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor@example.com",
                "template-id",
                getNotificationDataMap(caseData),
                "acknowledge-claim-applicant-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondentSolicitor2WhenSolicitor2RespondsFirst_whenInvokedWithCcEvent() {
            //solicitor 2  acknowledges claim, solicitor 1 does not
            when(notificationsProperties.getRespondentSolicitorAcknowledgeClaim()).thenReturn("template-id");
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
                .addRespondent2(YES)
                .respondent2SameLegalRepresentative(NO)
                .respondent2(PartyBuilder.builder().individual().build())
                .respondent1AcknowledgeNotificationDate(null)
                .respondent2AcknowledgeNotificationDate(LocalDateTime.now())
                .respondent1ResponseDeadline(null)
                .respondent2ResponseDeadline(LocalDateTime.now().plusDays(14))
                .respondent2ClaimResponseIntentionType(FULL_DEFENCE)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId("NOTIFY_APPLICANT_SOLICITOR1_FOR_CLAIM_ACKNOWLEDGEMENT_CC").build())
                .build();
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name("org name").build()));
            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor2@example.com",
                "template-id",
                getNotificationDataMap(caseData),
                "acknowledge-claim-applicant-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondentSolicitor2WhenSolicitor2RespondsLast_whenInvokedWithCcEvent() {
            //solicitor 2 acknowledges claim,solicitor 1 already acknowledged
            when(notificationsProperties.getRespondentSolicitorAcknowledgeClaim()).thenReturn("template-id");
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
                .addRespondent2(YES)
                .respondent2SameLegalRepresentative(NO)
                .respondent2(PartyBuilder.builder().individual().build())
                .respondent2AcknowledgeNotificationDate(LocalDateTime.now())
                .respondent1AcknowledgeNotificationDate(LocalDateTime.now().minusDays(2))
                .respondent1ResponseDeadline(LocalDateTime.now().plusDays(14))
                .respondent2ResponseDeadline(LocalDateTime.now().plusDays(14))
                .respondent2ClaimResponseIntentionType(FULL_DEFENCE)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId("NOTIFY_APPLICANT_SOLICITOR1_FOR_CLAIM_ACKNOWLEDGEMENT_CC").build())
                .build();
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name("org name").build()));
            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor2@example.com",
                "template-id",
                getNotificationDataMap(caseData),
                "acknowledge-claim-applicant-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondentSolicitor1WhenSolicitor1RespondsLast_whenInvokedWithCcEvent() {
            //solicitor 1 acknowledges claim,solicitor 2 already acknowledged
            when(notificationsProperties.getRespondentSolicitorAcknowledgeClaim()).thenReturn("template-id");
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
                .addRespondent2(YES)
                .respondent2SameLegalRepresentative(NO)
                .respondent2(PartyBuilder.builder().individual().build())
                .respondent1AcknowledgeNotificationDate(LocalDateTime.now())
                .respondent2AcknowledgeNotificationDate(LocalDateTime.now().minusDays(2))
                .respondent2ResponseDeadline(LocalDateTime.now().plusDays(14))
                .respondent1ResponseDeadline(LocalDateTime.now().plusDays(14))
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId("NOTIFY_APPLICANT_SOLICITOR1_FOR_CLAIM_ACKNOWLEDGEMENT_CC").build())
                .build();
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name("org name").build()));
            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor@example.com",
                "template-id",
                getNotificationDataMap(caseData),
                "acknowledge-claim-applicant-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondentSolicitor1v2SameSolicitor_whenInvokedWithCcEvent() {
            when(notificationsProperties.getRespondentSolicitorAcknowledgeClaim()).thenReturn("template-id");
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
                .addRespondent2(YES)
                .respondent2SameLegalRepresentative(YES)
                .respondent2(PartyBuilder.builder().individual().build())
                .respondent1AcknowledgeNotificationDate(LocalDateTime.now())
                .respondent2AcknowledgeNotificationDate(LocalDateTime.now().minusDays(2))
                .respondent2ResponseDeadline(LocalDateTime.now().plusDays(14))
                .respondent1ResponseDeadline(LocalDateTime.now().plusDays(14))
                .respondent2ClaimResponseIntentionType(FULL_DEFENCE)
                .respondent1ClaimResponseIntentionType(PART_DEFENCE)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                    CallbackRequest.builder().eventId("NOTIFY_APPLICANT_SOLICITOR1_FOR_CLAIM_ACKNOWLEDGEMENT_CC")
                        .build())
                .build();
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name("org name").build()));
            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor@example.com",
                "template-id",
                getNotificationDataMap(caseData),
                "acknowledge-claim-applicant-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondentSolicitor2v1SameSolicitor_whenInvokedWithCcEvent() {
            when(notificationsProperties.getRespondentSolicitorAcknowledgeClaim()).thenReturn("template-id");
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .addApplicant2(YesOrNo.YES)
                .applicant2(PartyBuilder.builder().individual().build())
                .respondent1AcknowledgeNotificationDate(LocalDateTime.now())
                .respondent1ResponseDeadline(LocalDateTime.now().plusDays(14))
                .respondent1ClaimResponseIntentionTypeApplicant2(FULL_DEFENCE)
                .respondent1ClaimResponseIntentionType(PART_DEFENCE)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                    CallbackRequest.builder()
                        .eventId("NOTIFY_APPLICANT_SOLICITOR1_FOR_CLAIM_ACKNOWLEDGEMENT_CC").build())
                .build();
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name("org name").build()));
            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor@example.com",
                "template-id",
                getNotificationDataMap(caseData),
                "acknowledge-claim-applicant-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondentSolicitor1_whenInvokedWithCcEvent() {
            //solicitor 1 acknowledges claim,solicitor 2 not
            when(notificationsProperties.getRespondentSolicitorAcknowledgeClaim()).thenReturn("template-id");
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
                .addRespondent2(YES)
                .respondent2SameLegalRepresentative(NO)
                .respondent2(PartyBuilder.builder().individual().build())
                .respondent1AcknowledgeNotificationDate(LocalDateTime.now().plusDays(14))
                .respondent2AcknowledgeNotificationDate(null)
                .respondent1ResponseDeadline(LocalDateTime.now())
                .respondent2ResponseDeadline(null)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId("NOTIFY_APPLICANT_SOLICITOR1_FOR_CLAIM_ACKNOWLEDGEMENT_CC").build())
                .build();
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name("org name").build()));
            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor@example.com",
                "template-id",
                getNotificationDataMap(caseData),
                "acknowledge-claim-applicant-notification-000DC001"
            );
        }

        @NotNull
        private Map<String, String> getNotificationDataMap(CaseData caseData) {
            // TODO: duplicate code - need to refactor
            LocalDateTime responseDeadline = caseData.getRespondent1ResponseDeadline();
            Party respondent = caseData.getRespondent1();
            MultiPartyScenario multiPartyScenario = getMultiPartyScenario(caseData);
            if (multiPartyScenario == ONE_V_TWO_TWO_LEGAL_REP) {
                if ((caseData.getRespondent1AcknowledgeNotificationDate() == null)
                    && (caseData.getRespondent2AcknowledgeNotificationDate() != null)) {
                    responseDeadline = caseData.getRespondent2ResponseDeadline();
                    respondent = caseData.getRespondent2();
                } else if ((caseData.getRespondent1AcknowledgeNotificationDate() != null)
                    && (caseData.getRespondent2AcknowledgeNotificationDate() == null)) {
                    responseDeadline = caseData.getRespondent1ResponseDeadline();
                    respondent = caseData.getRespondent1();
                } else if ((caseData.getRespondent1AcknowledgeNotificationDate() != null)
                    && (caseData.getRespondent2AcknowledgeNotificationDate() != null)) {
                    if (caseData.getRespondent2AcknowledgeNotificationDate()
                        .isAfter(caseData.getRespondent1AcknowledgeNotificationDate())) {
                        responseDeadline = caseData.getRespondent2ResponseDeadline();
                        respondent = caseData.getRespondent2();
                    } else {
                        responseDeadline = caseData.getRespondent1ResponseDeadline();
                        respondent = caseData.getRespondent1();
                    }
                }
            }

            HashMap<String, String> properties = new HashMap<>();
            properties.put(CLAIM_REFERENCE_NUMBER, CASE_ID.toString());
            properties.put(RESPONDENT_NAME, respondent.getPartyName());
            properties.put(PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData));
            properties.put(RESPONSE_DEADLINE, formatLocalDate(responseDeadline.toLocalDate(), DATE));
            properties.put(RESPONSE_INTENTION, getResponseIntentionForEmail(caseData));
            properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, "org name");
            properties.put(CASEMAN_REF, "000DC001");
            properties.put(PHONE_CONTACT, configuration.getPhoneContact());
            properties.put(OPENING_HOURS, configuration.getOpeningHours());
            properties.put(HMCTS_SIGNATURE, configuration.getHmctsSignature());
            properties.put(WELSH_PHONE_CONTACT, configuration.getWelshPhoneContact());
            properties.put(WELSH_OPENING_HOURS, configuration.getWelshOpeningHours());
            properties.put(WELSH_HMCTS_SIGNATURE, configuration.getWelshHmctsSignature());
            properties.put(LIP_CONTACT, configuration.getLipContactEmail());
            properties.put(LIP_CONTACT_WELSH, configuration.getLipContactEmailWelsh());
            properties.put(SPEC_UNSPEC_CONTACT, configuration.getRaiseQueryLr());
            properties.put(CNBC_CONTACT, configuration.getRaiseQueryLr());
            return properties;
        }
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
            "NOTIFY_APPLICANT_SOLICITOR1_FOR_CLAIM_ACKNOWLEDGEMENT").build()).build())).isEqualTo(TASK_ID);

        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
            "NOTIFY_APPLICANT_SOLICITOR1_FOR_CLAIM_ACKNOWLEDGEMENT_CC").build()).build())).isEqualTo(TASK_ID_CC);
    }
}
