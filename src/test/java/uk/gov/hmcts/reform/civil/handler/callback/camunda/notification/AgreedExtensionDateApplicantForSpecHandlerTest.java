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
import uk.gov.hmcts.reform.civil.callback.CallbackException;
import uk.gov.hmcts.reform.civil.config.PinInPostConfiguration;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_APPLICANT_RESPONDENT2_FOR_AGREED_EXTENSION_DATE_FOR_SPEC_CC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_APPLICANT_SOLICITOR1_FOR_AGREED_EXTENSION_DATE_FOR_SPEC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_APPLICANT_SOLICITOR1_FOR_AGREED_EXTENSION_DATE_FOR_SPEC_CC;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.AGREED_EXTENSION_DATE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASEMAN_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.DEFENDANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.FRONTEND_URL;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HMCTS_SIGNATURE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.OPENING_HOURS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PHONE_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONSE_DEADLINE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.SPEC_UNSPEC_CONTACT;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.fetchDefendantName;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@ExtendWith(MockitoExtension.class)
class AgreedExtensionDateApplicantForSpecHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private AgreedExtensionDateApplicantForSpecNotificationHandler handler;

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private OrganisationService organisationService;

    @Mock
    private PinInPostConfiguration pinInPostConfiguration;

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private NotificationsSignatureConfiguration configuration;

    @Nested
    class AboutToSubmitCallback {

        final String templateId = "template-id";
        final String templateIdRespondent = "template-id-respondent";
        final String templateIdWelsh = "template-id-welsh";
        final String reference = "agreed-extension-date-applicant-notification-spec-000DC001";
        Map<String, String> expectedNotificationData;
        Map<String, String> expectedNotificationDataRespondent;
        Map<String, String> expectedNotificationDataClaimantLiP;
        CaseData caseData;

        @Nested
        class WhenRespondent1SubmitsTimeExtension {

            @BeforeEach
            void setup() {
                caseData = CaseDataBuilder.builder().atStateNotificationAcknowledgedRespondent1TimeExtension().build();
                expectedNotificationData = getNotificationDataMap(
                    caseData.getRespondentSolicitor1AgreedDeadlineExtension());
                expectedNotificationDataRespondent = getNotificationDataMapRespondent1v2(
                    caseData.getRespondentSolicitor1AgreedDeadlineExtension());
                expectedNotificationDataClaimantLiP = getNotificationDataMapClaimantLiP(
                    caseData.getRespondent1ResponseDeadline().toLocalDate()
                );
            }

            @Test
            void shouldNotifyApplicantSolicitor_whenInvoked() {
                when(organisationService.findOrganisationById(anyString()))
                    .thenReturn(Optional.of(Organisation.builder().name("Signer Name").build()));
                when(notificationsProperties.getClaimantSolicitorAgreedExtensionDateForSpec())
                    .thenReturn("template-id");
                when(configuration.getHmctsSignature()).thenReturn("Online Civil Claims \n HM Courts & Tribunal Service");
                when(configuration.getPhoneContact()).thenReturn("For anything related to hearings, call 0300 123 5577 \n For all other matters, call 0300 123 7050");
                when(configuration.getOpeningHours()).thenReturn("Monday to Friday, 8.30am to 5pm");
                when(configuration.getSpecUnspecContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk \n Email for Damages Claims: damagesclaims@justice.gov.uk");

                invokeAboutToSubmitWithEvent("NOTIFY_APPLICANT_SOLICITOR1_FOR_AGREED_EXTENSION_DATE_FOR_SPEC");

                verify(notificationService).sendMail(
                    "applicantsolicitor@example.com",
                    templateId,
                    expectedNotificationData,
                    reference
                );
            }

            @Test
            void shouldNotifyApplicantLiP_whenInvoked() {
                when(pinInPostConfiguration.getCuiFrontEndUrl()).thenReturn("http://localhost:3001/");
                when(notificationsProperties.getClaimantLipDeadlineExtension())
                    .thenReturn("template-id");

                invokeAboutToSubmitWithEvent("NOTIFY_LIP_APPLICANT_FOR_AGREED_EXTENSION_DATE_FOR_SPEC");

                verify(notificationService).sendMail(
                    "rambo@email.com",
                    templateId,
                    expectedNotificationDataClaimantLiP,
                    reference
                );
            }

            @Test
            void shouldNotifyApplicantLiPInWelsh_whenInvoked() {
                caseData = caseData.toBuilder().claimantBilingualLanguagePreference("BOTH").build();
                when(pinInPostConfiguration.getCuiFrontEndUrl()).thenReturn("http://localhost:3001/");
                when(notificationsProperties.getClaimantLipDeadlineExtensionWelsh())
                    .thenReturn("template-id-welsh");

                invokeAboutToSubmitWithEvent("NOTIFY_LIP_APPLICANT_FOR_AGREED_EXTENSION_DATE_FOR_SPEC");

                verify(notificationService).sendMail(
                    "rambo@email.com",
                    templateIdWelsh,
                    expectedNotificationDataClaimantLiP,
                    reference
                );
            }

            @Test
            void shouldNotifyRespondentSolicitor1_whenInvoked() {
                when(organisationService.findOrganisationById(anyString()))
                    .thenReturn(Optional.of(Organisation.builder().name("Signer Name").build()));
                when(notificationsProperties.getRespondentSolicitorAgreedExtensionDateForSpec())
                    .thenReturn("template-id-respondent");
                when(configuration.getHmctsSignature()).thenReturn("Online Civil Claims \n HM Courts & Tribunal Service");
                when(configuration.getPhoneContact()).thenReturn("For anything related to hearings, call 0300 123 5577 \n For all other matters, call 0300 123 7050");
                when(configuration.getOpeningHours()).thenReturn("Monday to Friday, 8.30am to 5pm");
                when(configuration.getSpecUnspecContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk \n Email for Damages Claims: damagesclaims@justice.gov.uk");

                invokeAboutToSubmitWithEvent("NOTIFY_APPLICANT_SOLICITOR1_FOR_AGREED_EXTENSION_DATE_FOR_SPEC_CC");

                verify(notificationService).sendMail(
                    "respondentsolicitor@example.com",
                    templateIdRespondent,
                    getNotificationDataMapRespondent1v1(
                        caseData.getRespondentSolicitor1AgreedDeadlineExtension()),
                    reference
                );
            }

            @Test
            void shouldNotifyRespondentSolicitor2_whenInvoked() {
                when(organisationService.findOrganisationById(anyString()))
                    .thenReturn(Optional.of(Organisation.builder().name("Signer Name").build()));
                when(notificationsProperties.getRespondentSolicitorAgreedExtensionDateForSpec())
                    .thenReturn("template-id-respondent");
                when(configuration.getHmctsSignature()).thenReturn("Online Civil Claims \n HM Courts & Tribunal Service");
                when(configuration.getPhoneContact()).thenReturn("For anything related to hearings, call 0300 123 5577 \n For all other matters, call 0300 123 7050");
                when(configuration.getOpeningHours()).thenReturn("Monday to Friday, 8.30am to 5pm");
                when(configuration.getSpecUnspecContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk \n Email for Damages Claims: damagesclaims@justice.gov.uk");

                invokeAboutToSubmitWithEvent("NOTIFY_APPLICANT_RESPONDENT2_FOR_AGREED_EXTENSION_DATE_FOR_SPEC_CC");

                verify(notificationService).sendMail(
                    "respondentsolicitor2@example.com",
                    templateIdRespondent,
                    getNotificationDataMapRespondent1v1(caseData.getRespondentSolicitor1AgreedDeadlineExtension()),
                    reference
                );
            }
        }

        @Nested
        class WhenRespondent2SubmitsTimeExtension {

            @BeforeEach
            void setup() {
                caseData = CaseDataBuilder.builder().atStateNotificationAcknowledgedRespondent1TimeExtension().build();
                expectedNotificationData = getNotificationDataMap(
                    caseData.getRespondentSolicitor1AgreedDeadlineExtension());
                expectedNotificationDataRespondent = getNotificationDataMapRespondent1v2(
                    caseData.getRespondentSolicitor1AgreedDeadlineExtension());
            }

            @Test
            void shouldNotifyRespondentSolicitor2() {
                when(notificationsProperties.getRespondentSolicitorAgreedExtensionDateForSpec())
                    .thenReturn("template-id-respondent");
                when(organisationService.findOrganisationById(anyString()))
                    .thenReturn(Optional.of(Organisation.builder().name("Signer Name").build()));
                when(configuration.getHmctsSignature()).thenReturn("Online Civil Claims \n HM Courts & Tribunal Service");
                when(configuration.getPhoneContact()).thenReturn("For anything related to hearings, call 0300 123 5577 \n For all other matters, call 0300 123 7050");
                when(configuration.getOpeningHours()).thenReturn("Monday to Friday, 8.30am to 5pm");
                when(configuration.getSpecUnspecContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk \n Email for Damages Claims: damagesclaims@justice.gov.uk");

                invokeAboutToSubmitWithEvent("NOTIFY_APPLICANT_RESPONDENT2_FOR_AGREED_EXTENSION_DATE_FOR_SPEC_CC");

                verify(notificationService).sendMail(
                    "respondentsolicitor2@example.com",
                    templateIdRespondent,
                    getNotificationDataMapRespondent1v1(caseData.getRespondentSolicitor1AgreedDeadlineExtension()),
                    reference
                );
            }

            @Test
            void shouldNotifyWithCorrectExtensionDate_whenRespondentSolicitor1TimeExtensionExists() {
                when(notificationsProperties.getRespondentSolicitorAgreedExtensionDateForSpec())
                    .thenReturn("template-id-respondent");
                when(organisationService.findOrganisationById(anyString()))
                    .thenReturn(Optional.of(Organisation.builder().name("Signer Name").build()));
                when(configuration.getHmctsSignature()).thenReturn("Online Civil Claims \n HM Courts & Tribunal Service");
                when(configuration.getPhoneContact()).thenReturn("For anything related to hearings, call 0300 123 5577 \n For all other matters, call 0300 123 7050");
                when(configuration.getOpeningHours()).thenReturn("Monday to Friday, 8.30am to 5pm");
                when(configuration.getSpecUnspecContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk \n Email for Damages Claims: damagesclaims@justice.gov.uk");

                caseData = CaseDataBuilder.builder()
                    .atStateNotificationAcknowledgedRespondent2TimeExtension()
                    .atStateNotificationAcknowledgedRespondent1TimeExtension()
                    .build();

                expectedNotificationDataRespondent = getNotificationDataMapRespondent1v2(
                    caseData.getRespondentSolicitor2AgreedDeadlineExtension()
                );

                invokeAboutToSubmitWithEvent("NOTIFY_APPLICANT_RESPONDENT2_FOR_AGREED_EXTENSION_DATE_FOR_SPEC_CC");

                verify(notificationService).sendMail(
                    "respondentsolicitor2@example.com",
                    templateIdRespondent,
                    expectedNotificationDataRespondent,
                    reference
                );
            }

            @Test
            void shouldNotifyWithCorrectExtensionDate_whenRespondentSolicitor1ExtendsFirst() {
                when(notificationsProperties.getRespondentSolicitorAgreedExtensionDateForSpec())
                    .thenReturn("template-id-respondent");
                when(organisationService.findOrganisationById(anyString()))
                    .thenReturn(Optional.of(Organisation.builder().name("Signer Name").build()));
                when(configuration.getHmctsSignature()).thenReturn("Online Civil Claims \n HM Courts & Tribunal Service");
                when(configuration.getPhoneContact()).thenReturn("For anything related to hearings, call 0300 123 5577 \n For all other matters, call 0300 123 7050");
                when(configuration.getOpeningHours()).thenReturn("Monday to Friday, 8.30am to 5pm");
                when(configuration.getSpecUnspecContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk \n Email for Damages Claims: damagesclaims@justice.gov.uk");

                caseData = CaseDataBuilder.builder()
                    .atStateNotificationAcknowledgedRespondent2TimeExtension()
                    .atStateNotificationAcknowledgedRespondent1TimeExtension()
                    .respondent1TimeExtensionDate(LocalDateTime.now().minusDays(1))
                    .build();

                expectedNotificationDataRespondent = getNotificationDataMapRespondent1v2(
                    caseData.getRespondentSolicitor2AgreedDeadlineExtension());

                invokeAboutToSubmitWithEvent("NOTIFY_APPLICANT_RESPONDENT2_FOR_AGREED_EXTENSION_DATE_FOR_SPEC_CC");

                verify(notificationService).sendMail(
                    "respondentsolicitor2@example.com",
                    templateIdRespondent,
                    expectedNotificationDataRespondent,
                    reference
                );
            }

            @Test
            void shouldNotifyWithCorrectExtensionDate_when1v2DSRespondentSolicitor1ExtendsFirst() {
                when(notificationsProperties.getRespondentSolicitorAgreedExtensionDateForSpec())
                    .thenReturn("template-id-respondent");
                when(organisationService.findOrganisationById(anyString()))
                    .thenReturn(Optional.of(Organisation.builder().name("Signer Name").build()));
                when(configuration.getHmctsSignature()).thenReturn("Online Civil Claims \n HM Courts & Tribunal Service");
                when(configuration.getPhoneContact()).thenReturn("For anything related to hearings, call 0300 123 5577 \n For all other matters, call 0300 123 7050");
                when(configuration.getOpeningHours()).thenReturn("Monday to Friday, 8.30am to 5pm");
                when(configuration.getSpecUnspecContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk \n Email for Damages Claims: damagesclaims@justice.gov.uk");

                caseData = CaseDataBuilder.builder()
                    .atStateNotificationAcknowledgedRespondent1TimeExtension()
                    .respondent1(PartyBuilder.builder().individual().build())
                    .addRespondent2(YES)
                    .respondent2SameLegalRepresentative(NO)
                    .respondent2(PartyBuilder.builder().soleTrader().build())
                    .respondent1TimeExtensionDate(LocalDateTime.now().minusDays(1))
                    .build();

                invokeAboutToSubmitWithEvent("NOTIFY_APPLICANT_RESPONDENT2_FOR_AGREED_EXTENSION_DATE_FOR_SPEC_CC");

                verify(notificationService).sendMail(
                    "respondentsolicitor2@example.com",
                    templateIdRespondent,
                    expectedNotificationDataRespondent,
                    reference
                );
            }

            @Test
            void shouldNotifyWithCorrectExtensionDate_whenRespondentSolicitor2ExtendsFirst() {
                when(notificationsProperties.getRespondentSolicitorAgreedExtensionDateForSpec())
                    .thenReturn("template-id-respondent");
                when(organisationService.findOrganisationById(anyString()))
                    .thenReturn(Optional.of(Organisation.builder().name("Signer Name").build()));
                when(configuration.getHmctsSignature()).thenReturn("Online Civil Claims \n HM Courts & Tribunal Service");
                when(configuration.getPhoneContact()).thenReturn("For anything related to hearings, call 0300 123 5577 \n For all other matters, call 0300 123 7050");
                when(configuration.getOpeningHours()).thenReturn("Monday to Friday, 8.30am to 5pm");
                when(configuration.getSpecUnspecContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk \n Email for Damages Claims: damagesclaims@justice.gov.uk");

                caseData = CaseDataBuilder.builder()
                    .atStateNotificationAcknowledgedRespondent2TimeExtension()
                    .atStateNotificationAcknowledgedRespondent1TimeExtension()
                    .respondent2TimeExtensionDate(LocalDateTime.now().minusDays(1))
                    .build();

                expectedNotificationData = getNotificationDataMap(
                    caseData.getRespondent2ResponseDeadline().toLocalDate()
                );

                invokeAboutToSubmitWithEvent("NOTIFY_APPLICANT_RESPONDENT2_FOR_AGREED_EXTENSION_DATE_FOR_SPEC_CC");

                verify(notificationService).sendMail(
                    "respondentsolicitor2@example.com",
                    templateIdRespondent,
                    expectedNotificationDataRespondent,
                    reference
                );
            }

            @Test
            void shouldNotifyWithCorrectExtensionDate_when1v2SameSolicitorExtends() {
                when(notificationsProperties.getRespondentSolicitorAgreedExtensionDateForSpec())
                    .thenReturn("template-id-respondent");
                when(organisationService.findOrganisationById(anyString()))
                    .thenReturn(Optional.of(Organisation.builder().name("Signer Name").build()));
                when(configuration.getHmctsSignature()).thenReturn("Online Civil Claims \n HM Courts & Tribunal Service");
                when(configuration.getPhoneContact()).thenReturn("For anything related to hearings, call 0300 123 5577 \n For all other matters, call 0300 123 7050");
                when(configuration.getOpeningHours()).thenReturn("Monday to Friday, 8.30am to 5pm");
                when(configuration.getSpecUnspecContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk \n Email for Damages Claims: damagesclaims@justice.gov.uk");

                caseData = CaseDataBuilder.builder()
                    .atStateNotificationAcknowledgedRespondent1TimeExtension()
                    .addRespondent2(YES)
                    .respondent2(PartyBuilder.builder().individual().build())
                    .respondent2SameLegalRepresentative(YES)
                    .build();

                expectedNotificationData = getNotificationDataMap(
                    caseData.getRespondent1ResponseDeadline().toLocalDate()
                );

                invokeAboutToSubmitWithEvent("NOTIFY_APPLICANT_RESPONDENT2_FOR_AGREED_EXTENSION_DATE_FOR_SPEC_CC");

                verify(notificationService).sendMail(
                    "respondentsolicitor2@example.com",
                    templateIdRespondent,
                    getNotificationDataMapRespondent1v1(caseData.getRespondentSolicitor1AgreedDeadlineExtension()),
                    reference
                );
            }

            @Test
            void shouldNotifyRespondentSolicitor2ClaimStatementOfTruth_whenNoOrgFound() {
                when(notificationsProperties.getRespondentSolicitorAgreedExtensionDateForSpec())
                    .thenReturn("template-id-respondent");
                when(organisationService.findOrganisationById(anyString()))
                    .thenReturn(Optional.of(Organisation.builder().name("Signer Name").build()));
                when(configuration.getHmctsSignature()).thenReturn("Online Civil Claims \n HM Courts & Tribunal Service");
                when(configuration.getPhoneContact()).thenReturn("For anything related to hearings, call 0300 123 5577 \n For all other matters, call 0300 123 7050");
                when(configuration.getOpeningHours()).thenReturn("Monday to Friday, 8.30am to 5pm");
                when(configuration.getSpecUnspecContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk \n Email for Damages Claims: damagesclaims@justice.gov.uk");
                caseData = CaseDataBuilder.builder()
                    .atStateNotificationAcknowledgedRespondent2TimeExtension()
                    .atStateNotificationAcknowledgedRespondent1TimeExtension()
                    .respondent2TimeExtensionDate(LocalDateTime.now().minusDays(1))
                    .build();

                expectedNotificationData = getNotificationDataMap(
                    caseData.getRespondent2ResponseDeadline().toLocalDate()
                );
                when(organisationService.findOrganisationById(anyString()))
                    .thenReturn(Optional.empty());
                invokeAboutToSubmitWithEvent("NOTIFY_APPLICANT_RESPONDENT2_FOR_AGREED_EXTENSION_DATE_FOR_SPEC_CC");

                verify(notificationService).sendMail(
                    "respondentsolicitor2@example.com",
                    templateIdRespondent,
                    expectedNotificationDataRespondent,
                    reference
                );
            }

            @Test
            void shouldThrowException_whenInvalidEventId() {
                caseData = CaseDataBuilder.builder()
                    .atStateNotificationAcknowledgedRespondent2TimeExtension()
                    .atStateNotificationAcknowledgedRespondent1TimeExtension()
                    .respondent2TimeExtensionDate(LocalDateTime.now().minusDays(1))
                    .build();

                expectedNotificationData = getNotificationDataMap(
                    caseData.getRespondent2ResponseDeadline().toLocalDate()
                );
                CallbackException ex = assertThrows(CallbackException.class, () -> invokeAboutToSubmitWithEvent("Invalid Event"),
                                                    "A CallbackException was expected to be thrown but wasn't.");
                assertThat(ex.getMessage()).contains("Callback handler received unexpected event id");
            }

            @Test
            void testNullRespondentSolicitor1AgreedDeadlineExtension() {
                when(notificationsProperties.getRespondentSolicitorAgreedExtensionDateForSpec())
                    .thenReturn("template-id-respondent");
                when(organisationService.findOrganisationById(anyString()))
                    .thenReturn(Optional.of(Organisation.builder().name("Signer Name").build()));
                when(configuration.getHmctsSignature()).thenReturn("Online Civil Claims \n HM Courts & Tribunal Service");
                when(configuration.getPhoneContact()).thenReturn("For anything related to hearings, call 0300 123 5577 \n For all other matters, call 0300 123 7050");
                when(configuration.getOpeningHours()).thenReturn("Monday to Friday, 8.30am to 5pm");
                when(configuration.getSpecUnspecContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk \n Email for Damages Claims: damagesclaims@justice.gov.uk");
                caseData = CaseDataBuilder.builder()
                    .atStateNotificationAcknowledgedRespondent2TimeExtension()
                    .respondent2TimeExtensionDate(LocalDateTime.now().minusDays(1))
                    .build();

                expectedNotificationData = getNotificationDataMap(
                    caseData.getRespondent2ResponseDeadline().toLocalDate()
                );
                invokeAboutToSubmitWithEvent("NOTIFY_APPLICANT_RESPONDENT2_FOR_AGREED_EXTENSION_DATE_FOR_SPEC_CC");

                verify(notificationService).sendMail(
                    "respondentsolicitor2@example.com",
                    templateIdRespondent,
                    expectedNotificationDataRespondent,
                    reference
                );
            }

            @Test
            void testRespondent2AgreedDeadlineExtensionIsAfterRespondent1() {
                when(notificationsProperties.getRespondentSolicitorAgreedExtensionDateForSpec())
                    .thenReturn("template-id-respondent");
                when(organisationService.findOrganisationById(anyString()))
                    .thenReturn(Optional.of(Organisation.builder().name("Signer Name").build()));
                when(configuration.getHmctsSignature()).thenReturn("Online Civil Claims \n HM Courts & Tribunal Service");
                when(configuration.getPhoneContact()).thenReturn("For anything related to hearings, call 0300 123 5577 \n For all other matters, call 0300 123 7050");
                when(configuration.getOpeningHours()).thenReturn("Monday to Friday, 8.30am to 5pm");
                when(configuration.getSpecUnspecContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk \n Email for Damages Claims: damagesclaims@justice.gov.uk");

                caseData = CaseDataBuilder.builder()
                    .atStateNotificationAcknowledgedRespondent2TimeExtension()
                    .atStateNotificationAcknowledgedRespondent1TimeExtension()
                    .respondentSolicitor2AgreedDeadlineExtension(LocalDate.now().plusDays(10))
                    .build();

                expectedNotificationDataRespondent = getNotificationDataMapRespondent1v2(
                    caseData.getRespondentSolicitor2AgreedDeadlineExtension()
                );
                invokeAboutToSubmitWithEvent("NOTIFY_APPLICANT_RESPONDENT2_FOR_AGREED_EXTENSION_DATE_FOR_SPEC_CC");

                verify(notificationService).sendMail(
                    "respondentsolicitor2@example.com",
                    templateIdRespondent,
                    expectedNotificationDataRespondent,
                    reference
                );
            }
        }

        @NotNull
        private Map<String, String> getNotificationDataMap(LocalDate extensionDate) {
            return Map.of(
                CLAIM_LEGAL_ORG_NAME_SPEC, "Signer Name",
                CLAIM_REFERENCE_NUMBER, CASE_ID.toString(),
                AGREED_EXTENSION_DATE, formatLocalDate(extensionDate, DATE),
                DEFENDANT_NAME, fetchDefendantName(caseData),
                PARTY_REFERENCES, "Claimant reference: 12345 - Defendant reference: 6789",
                CASEMAN_REF, "000DC001",
                PHONE_CONTACT, "For anything related to hearings, call 0300 123 5577 \n For all other matters, call 0300 123 7050",
                OPENING_HOURS, "Monday to Friday, 8.30am to 5pm",
                SPEC_UNSPEC_CONTACT, "Email for Specified Claims: contactocmc@justice.gov.uk \n Email for Damages Claims: damagesclaims@justice.gov.uk",
                HMCTS_SIGNATURE, "Online Civil Claims \n HM Courts & Tribunal Service"
            );
        }

        @NotNull
        private Map<String, String> getNotificationDataMapRespondent1v2(LocalDate extensionDate) {
            return Map.of(
                CLAIM_LEGAL_ORG_NAME_SPEC, "Signer Name",
                CLAIM_REFERENCE_NUMBER, CASE_ID.toString(),
                AGREED_EXTENSION_DATE, formatLocalDate(extensionDate, DATE),
                PARTY_REFERENCES, "Claimant reference: 12345 - Defendant 1 reference: 6789 - Defendant 2 reference: Not provided",
                CASEMAN_REF, "000DC001",
                PHONE_CONTACT, "For anything related to hearings, call 0300 123 5577 \n For all other matters, call 0300 123 7050",
                OPENING_HOURS, "Monday to Friday, 8.30am to 5pm",
                SPEC_UNSPEC_CONTACT, "Email for Specified Claims: contactocmc@justice.gov.uk \n Email for Damages Claims: damagesclaims@justice.gov.uk",
                HMCTS_SIGNATURE, "Online Civil Claims \n HM Courts & Tribunal Service"
            );
        }

        @NotNull
        private Map<String, String> getNotificationDataMapRespondent1v1(LocalDate extensionDate) {
            return Map.of(
                CLAIM_LEGAL_ORG_NAME_SPEC, "Signer Name",
                CLAIM_REFERENCE_NUMBER, CASE_ID.toString(),
                AGREED_EXTENSION_DATE, formatLocalDate(extensionDate, DATE),
                PARTY_REFERENCES, "Claimant reference: 12345 - Defendant reference: 6789",
                CASEMAN_REF, "000DC001",
                PHONE_CONTACT, "For anything related to hearings, call 0300 123 5577 \n For all other matters, call 0300 123 7050",
                OPENING_HOURS, "Monday to Friday, 8.30am to 5pm",
                SPEC_UNSPEC_CONTACT, "Email for Specified Claims: contactocmc@justice.gov.uk \n Email for Damages Claims: damagesclaims@justice.gov.uk",
                HMCTS_SIGNATURE, "Online Civil Claims \n HM Courts & Tribunal Service"
            );
        }

        @NotNull
        private Map<String, String> getNotificationDataMapClaimantLiP(LocalDate extensionDate) {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
                CLAIMANT_NAME, getPartyNameBasedOnType(caseData.getApplicant1()),
                DEFENDANT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()),
                FRONTEND_URL, "http://localhost:3001/",
                RESPONSE_DEADLINE, formatLocalDate(extensionDate, DATE)
            );
        }

        private void invokeAboutToSubmitWithEvent(String eventId) {
            handler.handle(CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(eventId).build()
            ).build());
        }
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
            "NOTIFY_APPLICANT_SOLICITOR1_FOR_AGREED_EXTENSION_DATE_FOR_SPEC").build()).build()))
            .isEqualTo("AgreedExtensionDateNotifyApplicantSolicitor1ForSpec");

        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
            "NOTIFY_APPLICANT_SOLICITOR1_FOR_AGREED_EXTENSION_DATE_FOR_SPEC_CC").build()).build()))
            .isEqualTo("AgreedExtensionDateNotifyRespondentSolicitor1CCForSpec");

        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
            "NOTIFY_APPLICANT_RESPONDENT2_FOR_AGREED_EXTENSION_DATE_FOR_SPEC_CC").build()).build()))
            .isEqualTo("AgreedExtensionDateNotifyRespondentSolicitor2CCForSpec");

    }

    @Test
    void testHandledEvents() {
        assertThat(handler.handledEvents()).contains(NOTIFY_APPLICANT_SOLICITOR1_FOR_AGREED_EXTENSION_DATE_FOR_SPEC);
        assertThat(handler.handledEvents()).contains(NOTIFY_APPLICANT_SOLICITOR1_FOR_AGREED_EXTENSION_DATE_FOR_SPEC_CC);
        assertThat(handler.handledEvents()).contains(NOTIFY_APPLICANT_RESPONDENT2_FOR_AGREED_EXTENSION_DATE_FOR_SPEC_CC);
    }
}
