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
import uk.gov.hmcts.reform.civil.callback.CallbackException;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.SolicitorReferences;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.hearingnotice.HearingNoticeCamundaService;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
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
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.SPEC_UNSPEC_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_HMCTS_SIGNATURE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_OPENING_HOURS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_PHONE_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationDefendantOfHearingHandler.TASK_ID_DEFENDANT1;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationDefendantOfHearingHandler.TASK_ID_DEFENDANT1_HMC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationDefendantOfHearingHandler.TASK_ID_DEFENDANT2;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationDefendantOfHearingHandler.TASK_ID_DEFENDANT2_HMC;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.CASE_ID;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;

@ExtendWith(MockitoExtension.class)
public class NotificationDefendantOfHearingHandlerTest {

    @Mock
    private NotificationService notificationService;
    @Mock
    NotificationsProperties notificationsProperties;
    @Mock
    HearingNoticeCamundaService hearingNoticeCamundaService;
    @Mock
    private OrganisationService organisationService;
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
    private NotificationDefendantOfHearingHandler handler;

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
        void shouldNotifyRespondentSolicitor_whenInvokedNoFeeAnd1v1() {
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(new Organisation().setName("org name")));
            Map<String, Object> configMap = YamlNotificationTestUtil.loadNotificationsConfig();
            when(configuration.getRaiseQueryLr()).thenReturn((String) configMap.get("raiseQueryLr"));
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .hearingDate(LocalDate.of(2022, 10, 7))
                .applicantSolicitor1UserDetails(new IdamUserDetails().setEmail("applicantemail@hmcts.net"))
                .respondentSolicitor1EmailAddress("respondent1email@hmcts.net")
                .hearingReferenceNumber("000HN001")
                .hearingTimeHourMinute("1530")
                .addApplicant2(YesOrNo.NO)
                .addRespondent2(YesOrNo.NO)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId("NOTIFY_DEFENDANT1_HEARING").build()).build();
            // When
            when(notificationsProperties.getHearingListedNoFeeDefendantLrTemplate())
                .thenReturn("test-template-no-fee-defendant-id");
            handler.handle(params);
            // Then
            verify(notificationService).sendMail(
                "respondent1email@hmcts.net",
                "test-template-no-fee-defendant-id",
                getNotificationDataMap(caseData),
                "notification-of-hearing-000HN001"
            );
        }

        @Test
        void shouldNotifyRespondentSolicitor_whenInvokedNoFeeAnd1v1AndNoSolicitorReferences() {
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(new Organisation().setName("org name")));
            Map<String, Object> configMap = YamlNotificationTestUtil.loadNotificationsConfig();
            when(configuration.getRaiseQueryLr()).thenReturn((String) configMap.get("raiseQueryLr"));
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .hearingDate(LocalDate.of(2022, 10, 7))
                .applicantSolicitor1UserDetails(new IdamUserDetails().setEmail("applicantemail@hmcts.net"))
                .respondentSolicitor1EmailAddress("respondent1email@hmcts.net")
                .hearingReferenceNumber("000HN001")
                .hearingTimeHourMinute("1530")
                .addApplicant2(YesOrNo.NO)
                .addRespondent2(YesOrNo.NO)
                .solicitorReferences(null)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId("NOTIFY_DEFENDANT1_HEARING").build()).build();
            // When
            when(notificationsProperties.getHearingListedNoFeeDefendantLrTemplate())
                .thenReturn("test-template-no-fee-defendant-id");
            handler.handle(params);
            // Then
            verify(notificationService).sendMail(
                "respondent1email@hmcts.net",
                "test-template-no-fee-defendant-id",
                getNotificationDataMapNoReference(caseData),
                "notification-of-hearing-000HN001"
            );
        }

        @Test
        void shouldNotifyRespondentSolicitor_whenInvokedNoFeeAnd1v1AndNoSolicitorReferencesForDef1() {
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(new Organisation().setName("org name")));
            Map<String, Object> configMap = YamlNotificationTestUtil.loadNotificationsConfig();
            when(configuration.getRaiseQueryLr()).thenReturn((String) configMap.get("raiseQueryLr"));
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .hearingDate(LocalDate.of(2022, 10, 7))
                .applicantSolicitor1UserDetails(new IdamUserDetails().setEmail("applicantemail@hmcts.net"))
                .respondentSolicitor1EmailAddress("respondent1email@hmcts.net")
                .hearingReferenceNumber("000HN001")
                .hearingTimeHourMinute("1530")
                .addApplicant2(YesOrNo.NO)
                .addRespondent2(YesOrNo.NO)
                .solicitorReferences(new SolicitorReferences())
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId("NOTIFY_DEFENDANT1_HEARING").build()).build();
            // When
            when(notificationsProperties.getHearingListedNoFeeDefendantLrTemplate())
                .thenReturn("test-template-no-fee-defendant-id");
            handler.handle(params);
            // Then
            verify(notificationService).sendMail(
                "respondent1email@hmcts.net",
                "test-template-no-fee-defendant-id",
                getNotificationDataMapNoReference(caseData),
                "notification-of-hearing-000HN001"
            );
        }

        @Test
        void shouldNotifyRespondentSolicitor2_whenInvokedNoFeeAnd1v2() {
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(new Organisation().setName("org name")));
            Map<String, Object> configMap = YamlNotificationTestUtil.loadNotificationsConfig();
            when(configuration.getRaiseQueryLr()).thenReturn((String) configMap.get("raiseQueryLr"));
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .hearingDate(LocalDate.of(2022, 10, 7))
                .applicantSolicitor1UserDetails(new IdamUserDetails().setEmail("applicantemail@hmcts.net"))
                .respondentSolicitor1EmailAddress("respondent1email@hmcts.net")
                .respondentSolicitor2EmailAddress("respondent2email@hmcts.net")
                .hearingReferenceNumber("000HN001")
                .hearingTimeHourMinute("1530")
                .addApplicant2(YesOrNo.NO)
                .addRespondent2(YES)
                .respondent2(Party.builder().type(Party.Type.COMPANY).companyName("Party2").build())
                .solicitorReferences(new SolicitorReferences().setRespondentSolicitor1Reference("6789"))
                .respondentSolicitor2Reference("10111213")
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId("NOTIFY_DEFENDANT2_HEARING").build()).build();
            // When
            when(notificationsProperties.getHearingListedNoFeeDefendantLrTemplate())
                .thenReturn("test-template-no-fee-defendant-id");
            handler.handle(params);
            // Then
            verify(notificationService, times(1)).sendMail(targetEmail.capture(),
                                                           emailTemplate.capture(),
                                                           notificationDataMap.capture(), reference.capture()
            );
            assertThat(targetEmail.getAllValues().get(0)).isEqualTo("respondent2email@hmcts.net");
            assertThat(emailTemplate.getAllValues().get(0)).isEqualTo("test-template-no-fee-defendant-id");
            assertThat(notificationDataMap.getAllValues().get(0)).isEqualTo(getNotificationDataMapDef2(caseData));
            assertThat(reference.getAllValues().get(0)).isEqualTo("notification-of-hearing-000HN001");
        }

        @Test
        void shouldNotifyRespondentSolicitor2_whenInvokedNoFeeAnd1v2WithSameSolicitor() {
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(new Organisation().setName("org name")));
            Map<String, Object> configMap = YamlNotificationTestUtil.loadNotificationsConfig();
            when(configuration.getRaiseQueryLr()).thenReturn((String) configMap.get("raiseQueryLr"));
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .hearingDate(LocalDate.of(2022, 10, 7))
                .applicantSolicitor1UserDetails(new IdamUserDetails().setEmail("applicantemail@hmcts.net"))
                .respondentSolicitor1EmailAddress("respondent1email@hmcts.net")
                .respondentSolicitor2EmailAddress(null)
                .hearingReferenceNumber("000HN001")
                .hearingTimeHourMinute("1530")
                .addApplicant2(YesOrNo.NO)
                .addRespondent2(YES)
                .respondent2(Party.builder().type(Party.Type.COMPANY).companyName("Party2").build())
                .solicitorReferences(new SolicitorReferences().setRespondentSolicitor1Reference("6789"))
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId("NOTIFY_DEFENDANT2_HEARING").build()).build();
            // When
            when(notificationsProperties.getHearingListedNoFeeDefendantLrTemplate())
                .thenReturn("test-template-no-fee-defendant-id");
            handler.handle(params);
            // Then
            verify(notificationService, times(1)).sendMail(targetEmail.capture(),
                                                           emailTemplate.capture(),
                                                           notificationDataMap.capture(), reference.capture()
            );
            assertThat(targetEmail.getAllValues().get(0)).isEqualTo("respondent1email@hmcts.net");
            assertThat(emailTemplate.getAllValues().get(0)).isEqualTo("test-template-no-fee-defendant-id");
            assertThat(notificationDataMap.getAllValues().get(0))
                .isEqualTo(getNotificationDataMapDef2WithNoReference(caseData));
            assertThat(reference.getAllValues().get(0)).isEqualTo("notification-of-hearing-000HN001");
        }

        @Test
        void shouldNotifyRespondentSolicitor_whenInvokedNoFeeAnd2v1() {
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(new Organisation().setName("org name")));
            Map<String, Object> configMap = YamlNotificationTestUtil.loadNotificationsConfig();
            when(configuration.getRaiseQueryLr()).thenReturn((String) configMap.get("raiseQueryLr"));
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .hearingDate(LocalDate.of(2022, 10, 7))
                .applicantSolicitor1UserDetails(new IdamUserDetails().setEmail("applicantemail@hmcts.net"))
                .respondentSolicitor1EmailAddress("respondent1email@hmcts.net")
                .hearingReferenceNumber("000HN001")
                .hearingTimeHourMinute("1530")
                .addApplicant2(YesOrNo.YES)
                .addRespondent2(YesOrNo.NO)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId("NOTIFY_DEFENDANT1_HEARING").build()).build();
            // When
            when(notificationsProperties.getHearingListedNoFeeDefendantLrTemplate())
                .thenReturn("test-template-no-fee-defendant-id");
            handler.handle(params);
            // Then
            verify(notificationService).sendMail(
                "respondent1email@hmcts.net",
                "test-template-no-fee-defendant-id",
                getNotificationDataMap(caseData),
                "notification-of-hearing-000HN001"
            );
        }

        @Test
        void shouldNotifyRespondentSolicitorLip_whenInvokedAnd1v1() {
            // When
            when(notificationsProperties.getHearingNotificationLipDefendantTemplate())
                .thenReturn("test-template-defendant-lip-id");
            Map<String, Object> configMap = YamlNotificationTestUtil.loadNotificationsConfig();
            when(configuration.getCnbcContact()).thenReturn((String) configMap.get("cnbcContact"));
            when(configuration.getSpecUnspecContact()).thenReturn((String) configMap.get("specUnspecContact"));

            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .hearingDate(LocalDate.of(2023, 05, 17))
                .hearingTimeHourMinute("1100")
                .applicant1Represented(YesOrNo.NO)
                .respondent1Represented(YesOrNo.NO)
                .addApplicant2(YesOrNo.NO)
                .addRespondent2(YesOrNo.NO)
                .applicant1(Party.builder()
                                .individualFirstName("John")
                                .individualLastName("Doe")
                                .partyName("John").partyEmail("applicant1@example.com").type(Party.Type.INDIVIDUAL).build())
                .respondent1(Party.builder()
                                 .individualFirstName("John")
                                 .individualLastName("Doe")
                                 .partyName("Mark").partyEmail("respondent1@example.com").type(Party.Type.INDIVIDUAL).build())
                .hearingReferenceNumber("000HN001")
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId("NOTIFY_DEFENDANT1_HEARING").build()).build();
            handler.handle(params);
            // Then
            verify(notificationService).sendMail(
                "respondent1@example.com",
                "test-template-defendant-lip-id",
                getNotificationLipDataMap(caseData),
                "notification-of-hearing-lip-000HN001"
            );
        }

        @Test
        void shouldNotifyRespondentLip_whenInvokedAnd1v1HMC() {
            // When
            when(hearingNoticeCamundaService.getProcessVariables(any()))
                .thenReturn(new uk.gov.hmcts.reform.civil.service.hearingnotice.HearingNoticeVariables()
                                .setHearingStartDateTime(java.time.LocalDateTime.of(
                                    LocalDate.of(2022, 10, 7),
                                    java.time.LocalTime.of(13, 0)
                                ))
                                .setHearingId("123456"));
            when(notificationsProperties.getHearingNotificationLipDefendantTemplate())
                .thenReturn("test-template-defendant-lip-id");
            Map<String, Object> configMap = YamlNotificationTestUtil.loadNotificationsConfig();
            when(configuration.getCnbcContact()).thenReturn((String) configMap.get("cnbcContact"));
            when(configuration.getSpecUnspecContact()).thenReturn((String) configMap.get("specUnspecContact"));

            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .hearingDate(LocalDate.of(2023, 05, 17))
                .applicant1Represented(YesOrNo.NO)
                .respondent1Represented(YesOrNo.NO)
                .addApplicant2(YesOrNo.NO)
                .addRespondent2(YesOrNo.NO)
                .applicant1(Party.builder()
                                .individualFirstName("John")
                                .individualLastName("Doe")
                                .partyName("John").partyEmail("applicant1@example.com").type(Party.Type.INDIVIDUAL).build())
                .respondent1(Party.builder()
                                 .individualFirstName("John")
                                 .individualLastName("Doe")
                                 .partyName("Mark").partyEmail("respondent1@example.com").type(Party.Type.INDIVIDUAL).build())
                .hearingReferenceNumber("000HN001")
                .businessProcess(new BusinessProcess().setProcessInstanceId(""))
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId("NOTIFY_DEFENDANT1_HEARING_HMC").build()).build();
            handler.handle(params);
            // Then
            verify(notificationService).sendMail(
                "respondent1@example.com",
                "test-template-defendant-lip-id",
                getNotificationLipHmcDataMap(caseData),
                "notification-of-hearing-lip-123456"
            );
        }

        @Test
        void shouldNotifyRespondentSolicitorLipInWelsh_whenInvokedAnd1v1() {
            // When
            when(notificationsProperties.getHearingNotificationLipDefendantTemplateWelsh())
                .thenReturn("test-template-defendant-lip-welsh-id");
            Map<String, Object> configMap = YamlNotificationTestUtil.loadNotificationsConfig();
            when(configuration.getCnbcContact()).thenReturn((String) configMap.get("cnbcContact"));
            when(configuration.getSpecUnspecContact()).thenReturn((String) configMap.get("specUnspecContact"));

            // Given
            CaseData caseData = CaseDataBuilder.builder()
                .caseDataLip(new CaseDataLiP()
                                 .setRespondent1LiPResponse(new RespondentLiPResponse()
                                                             .setRespondent1ResponseLanguage(Language.BOTH.toString())))
                .atStateClaimDetailsNotified().build().toBuilder()
                .hearingDate(LocalDate.of(2023, 05, 17))
                .hearingTimeHourMinute("1100")
                .applicant1Represented(YesOrNo.NO)
                .respondent1Represented(YesOrNo.NO)
                .addApplicant2(YesOrNo.NO)
                .addRespondent2(YesOrNo.NO)

                .applicant1(Party.builder()
                                .individualFirstName("John")
                                .individualLastName("Doe")
                                .partyName("John").partyEmail("applicant1@example.com").type(Party.Type.INDIVIDUAL).build())
                .respondent1(Party.builder()
                                 .individualFirstName("John")
                                 .individualLastName("Doe")
                                 .partyName("Mark").partyEmail("respondent1@example.com").type(Party.Type.INDIVIDUAL).build())
                .hearingReferenceNumber("000HN001")
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId("NOTIFY_DEFENDANT1_HEARING").build()).build();
            handler.handle(params);
            // Then
            verify(notificationService).sendMail(
                "respondent1@example.com",
                "test-template-defendant-lip-welsh-id",
                getNotificationLipDataMap(caseData),
                "notification-of-hearing-lip-000HN001"
            );
        }

        @Test
        void shouldNotifyRespondentSolicitorLip1_whenInvokedAnd1v2() {
            // When
            when(notificationsProperties.getHearingNotificationLipDefendantTemplate())
                .thenReturn("test-template-defendant-lip-id");
            Map<String, Object> configMap = YamlNotificationTestUtil.loadNotificationsConfig();
            when(configuration.getCnbcContact()).thenReturn((String) configMap.get("cnbcContact"));
            when(configuration.getSpecUnspecContact()).thenReturn((String) configMap.get("specUnspecContact"));

            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .hearingDate(LocalDate.of(2023, 05, 17))
                .hearingTimeHourMinute("1100")
                .applicant1Represented(YesOrNo.NO)
                .respondent1Represented(YesOrNo.NO)
                .addApplicant2(YesOrNo.NO)
                .addRespondent2(YES)
                .applicant1(Party.builder()
                                .individualFirstName("John")
                                .individualLastName("Doe")
                                .partyName("John").partyEmail("applicant1@example.com").type(Party.Type.INDIVIDUAL).build())
                .respondent1(Party.builder()
                                 .individualFirstName("John")
                                 .individualLastName("Doe")
                                 .partyName("Mark").partyEmail("respondent1@example.com").type(Party.Type.INDIVIDUAL).build())
                .respondent2(Party.builder()
                                 .individualFirstName("John")
                                 .individualLastName("Doe")
                                 .partyName("Peter").partyEmail("respondent2@example.com").type(Party.Type.INDIVIDUAL).build())
                .hearingReferenceNumber("000HN001")
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId("NOTIFY_DEFENDANT1_HEARING").build()).build();
            handler.handle(params);
            // Then
            verify(notificationService, times(1)).sendMail(targetEmail.capture(),
                                                           emailTemplate.capture(),
                                                           notificationDataMap.capture(), reference.capture()
            );
            assertThat(targetEmail.getAllValues().get(0)).isEqualTo("respondent1@example.com");
            assertThat(emailTemplate.getAllValues().get(0)).isEqualTo("test-template-defendant-lip-id");
            assertThat(notificationDataMap.getAllValues().get(0)).isEqualTo(getNotificationLipDataMap(caseData));
            assertThat(reference.getAllValues().get(0)).isEqualTo("notification-of-hearing-lip-000HN001");
        }

        @Test
        void shouldNotifyRespondentSolicitorLip2_whenInvokedAnd1v2() {
            // When
            when(notificationsProperties.getHearingNotificationLipDefendantTemplate())
                .thenReturn("test-template-defendant-lip-id");
            Map<String, Object> configMap = YamlNotificationTestUtil.loadNotificationsConfig();
            when(configuration.getCnbcContact()).thenReturn((String) configMap.get("cnbcContact"));
            when(configuration.getSpecUnspecContact()).thenReturn((String) configMap.get("specUnspecContact"));

            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .hearingDate(LocalDate.of(2023, 05, 17))
                .hearingTimeHourMinute("1100")
                .applicant1Represented(YesOrNo.NO)
                .respondent1Represented(YesOrNo.NO)
                .addApplicant2(YesOrNo.NO)
                .addRespondent2(YES)
                .applicant1(Party.builder()
                                .individualFirstName("John")
                                .individualLastName("Doe")
                                .partyName("John").partyEmail("applicant1@example.com").type(Party.Type.INDIVIDUAL).build())
                .respondent1(Party.builder()
                                 .individualFirstName("John")
                                 .individualLastName("Doe")
                                 .partyName("Mark").partyEmail("respondent1@example.com").type(Party.Type.INDIVIDUAL).build())
                .respondent2(Party.builder()
                                 .individualFirstName("John")
                                 .individualLastName("Doe")
                                 .partyName("Peter").partyEmail("respondent2@example.com").type(Party.Type.INDIVIDUAL).build())
                .hearingReferenceNumber("000HN001")
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId("NOTIFY_DEFENDANT2_HEARING").build()).build();
            handler.handle(params);
            // Then
            verify(notificationService, times(1)).sendMail(targetEmail.capture(),
                                                           emailTemplate.capture(),
                                                           notificationDataMap.capture(), reference.capture()
            );
            assertThat(targetEmail.getAllValues().get(0)).isEqualTo("respondent2@example.com");
            assertThat(emailTemplate.getAllValues().get(0)).isEqualTo("test-template-defendant-lip-id");
            assertThat(notificationDataMap.getAllValues().get(0)).isEqualTo(getNotificationLipDataMap(caseData));
            assertThat(reference.getAllValues().get(0)).isEqualTo("notification-of-hearing-lip-000HN001");
        }

        @Test
        void shouldNotifyRespondentSolicitorLip1_whenInvokedAnd1v2AndResp2EmailNull() {
            // When
            when(notificationsProperties.getHearingNotificationLipDefendantTemplate())
                .thenReturn("test-template-defendant-lip-id");
            Map<String, Object> configMap = YamlNotificationTestUtil.loadNotificationsConfig();
            when(configuration.getCnbcContact()).thenReturn((String) configMap.get("cnbcContact"));
            when(configuration.getSpecUnspecContact()).thenReturn((String) configMap.get("specUnspecContact"));

            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .hearingDate(LocalDate.of(2023, 05, 17))
                .hearingTimeHourMinute("1100")
                .applicant1Represented(YesOrNo.NO)
                .respondent1Represented(YesOrNo.NO)
                .addApplicant2(YesOrNo.NO)
                .addRespondent2(YES)
                .applicant1(Party.builder()
                                .individualFirstName("John")
                                .individualLastName("Doe")
                                .partyName("John").partyEmail("applicant1@example.com").type(Party.Type.INDIVIDUAL).build())
                .respondent1(Party.builder()
                                 .individualFirstName("John")
                                 .individualLastName("Doe")
                                 .partyName("Mark").partyEmail("respondent1@example.com").type(Party.Type.INDIVIDUAL).build())
                .respondent2(Party.builder()
                                 .individualFirstName("John")
                                 .individualLastName("Doe")
                                 .partyName("Peter").type(Party.Type.INDIVIDUAL).build())
                .hearingReferenceNumber("000HN001")
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId("NOTIFY_DEFENDANT2_HEARING").build()).build();
            handler.handle(params);
            // Then
            verify(notificationService, times(1)).sendMail(targetEmail.capture(),
                                                           emailTemplate.capture(),
                                                           notificationDataMap.capture(), reference.capture()
            );
            assertThat(targetEmail.getAllValues().get(0)).isEqualTo("respondent1@example.com");
            assertThat(emailTemplate.getAllValues().get(0)).isEqualTo("test-template-defendant-lip-id");
            assertThat(notificationDataMap.getAllValues().get(0)).isEqualTo(getNotificationLipDataMap(caseData));
            assertThat(reference.getAllValues().get(0)).isEqualTo("notification-of-hearing-lip-000HN001");
        }

        @Test
        void shouldNotifyRespondentSolicitor_whenInvokedNoFeeAnd1v1HMC() {
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(new Organisation().setName("org name")));
            // When
            Map<String, Object> configMap = YamlNotificationTestUtil.loadNotificationsConfig();
            when(configuration.getRaiseQueryLr()).thenReturn((String) configMap.get("raiseQueryLr"));
            when(hearingNoticeCamundaService.getProcessVariables(any()))
                .thenReturn(new uk.gov.hmcts.reform.civil.service.hearingnotice.HearingNoticeVariables()
                                .setHearingStartDateTime(java.time.LocalDateTime.of(
                                    LocalDate.of(2022, 10, 7),
                                    java.time.LocalTime.of(15, 30)
                                ))
                                .setHearingId("123456"));
            when(notificationsProperties.getHearingListedNoFeeDefendantLrTemplateHMC())
                .thenReturn("test-template-no-fee-defendant-id-hmc");

            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .applicantSolicitor1UserDetails(new IdamUserDetails().setEmail("applicantemail@hmcts.net"))
                .respondentSolicitor1EmailAddress("respondent1email@hmcts.net")
                .addApplicant2(YesOrNo.NO)
                .addRespondent2(YesOrNo.NO)
                .businessProcess(new BusinessProcess().setProcessInstanceId(""))
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId("NOTIFY_DEFENDANT1_HEARING_HMC").build()).build();
            handler.handle(params);
            // Then
            verify(notificationService).sendMail(
                "respondent1email@hmcts.net",
                "test-template-no-fee-defendant-id-hmc",
                getNotificationDataMapHMC(caseData, false),
                "notification-of-hearing-123456"
            );
        }

        @Test
        void shouldNotifyRespondentSolicitor_whenInvokedNoFeeAnd1v2DS_HMC() {
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(new Organisation().setName("org name")));
            Map<String, Object> configMap = YamlNotificationTestUtil.loadNotificationsConfig();
            when(configuration.getRaiseQueryLr()).thenReturn((String) configMap.get("raiseQueryLr"));
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .applicantSolicitor1UserDetails(new IdamUserDetails().setEmail("applicantemail@hmcts.net"))
                .respondentSolicitor1EmailAddress("respondent1email@hmcts.net")
                .respondentSolicitor2EmailAddress("respondent2email@hmcts.net")
                .addApplicant2(YesOrNo.NO)
                .addRespondent2(YesOrNo.YES)
                .respondent2(Party.builder().type(Party.Type.COMPANY).companyName("Party2").build())
                .businessProcess(new BusinessProcess().setProcessInstanceId(""))
                .build().toBuilder()
                .respondentSolicitor2Reference("6789").build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId("NOTIFY_DEFENDANT2_HEARING_HMC").build()).build();
            // When
            when(hearingNoticeCamundaService.getProcessVariables(any()))
                .thenReturn(new uk.gov.hmcts.reform.civil.service.hearingnotice.HearingNoticeVariables()
                                .setHearingStartDateTime(java.time.LocalDateTime.of(
                                    LocalDate.of(2022, 10, 7),
                                    java.time.LocalTime.of(15, 30)
                                ))
                                .setHearingId("123456"));
            when(notificationsProperties.getHearingListedNoFeeDefendantLrTemplateHMC())
                .thenReturn("test-template-no-fee-defendant-id-hmc");
            handler.handle(params);
            // Then
            verify(notificationService).sendMail(
                "respondent2email@hmcts.net",
                "test-template-no-fee-defendant-id-hmc",
                getNotificationDataMapHMC(caseData, true),
                "notification-of-hearing-123456"
            );
        }

        @NotNull
        public Map<String, String> addCommonProperties(boolean isLipCase) {
            Map<String, String> expectedProperties = new HashMap<>();
            expectedProperties.put(PHONE_CONTACT, configuration.getPhoneContact());
            expectedProperties.put(OPENING_HOURS, configuration.getOpeningHours());
            expectedProperties.put(HMCTS_SIGNATURE, configuration.getHmctsSignature());
            expectedProperties.put(WELSH_PHONE_CONTACT, configuration.getWelshPhoneContact());
            expectedProperties.put(WELSH_OPENING_HOURS, configuration.getWelshOpeningHours());
            expectedProperties.put(WELSH_HMCTS_SIGNATURE, configuration.getWelshHmctsSignature());
            expectedProperties.put(LIP_CONTACT, configuration.getLipContactEmail());
            expectedProperties.put(LIP_CONTACT_WELSH, configuration.getLipContactEmailWelsh());
            if (isLipCase) {
                expectedProperties.put(SPEC_UNSPEC_CONTACT, configuration.getSpecUnspecContact());
                expectedProperties.put(CNBC_CONTACT, configuration.getCnbcContact());
            } else {
                expectedProperties.put(SPEC_UNSPEC_CONTACT, configuration.getRaiseQueryLr());
                expectedProperties.put(CNBC_CONTACT, configuration.getRaiseQueryLr());
            }
            return expectedProperties;
        }

        @NotNull
        private Map<String, String> getNotificationDataMapHMC(CaseData caseData, boolean is1v2) {
            Map<String, String> expectedProperties = new HashMap<>(addCommonProperties(false));
            expectedProperties.putAll(Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
                "hearingDate", "07-10-2022",
                "hearingTime", "03:30pm",
                "defendantReferenceNumber", "6789",
                PARTY_REFERENCES, is1v2
                    ? "Claimant reference: 12345 - Defendant 1 reference: 6789 - Defendant 2 reference: 6789"
                    : "Claimant reference: 12345 - Defendant reference: 6789",
                CLAIM_LEGAL_ORG_NAME_SPEC, "org name",
                CASEMAN_REF, "000DC001"
            ));
            return expectedProperties;
        }

        @NotNull
        private Map<String, String> getNotificationDataMap(CaseData caseData) {
            Map<String, String> expectedProperties = new HashMap<>(addCommonProperties(false));
            expectedProperties.putAll(Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
                "defendantReferenceNumber", "6789",
                "hearingDate", "07-10-2022",
                "hearingTime", "03:30pm",
                PARTY_REFERENCES, "Claimant reference: 12345 - Defendant reference: 6789",
                CLAIM_LEGAL_ORG_NAME_SPEC, "org name",
                CASEMAN_REF, "000DC001"
            ));
            return expectedProperties;
        }

        @NotNull
        private Map<String, String> getNotificationDataMapNoReference(CaseData caseData) {
            Map<String, String> expectedProperties = new HashMap<>(addCommonProperties(false));
            expectedProperties.putAll(Map.of(
                CLAIM_REFERENCE_NUMBER, CASE_ID.toString(),
                "defendantReferenceNumber", "",
                "hearingDate", "07-10-2022",
                "hearingTime", "03:30pm",
                PARTY_REFERENCES, "Claimant reference: Not provided - Defendant reference: Not provided",
                CLAIM_LEGAL_ORG_NAME_SPEC, "org name",
                CASEMAN_REF, "000DC001"
            ));
            return expectedProperties;
        }

        @NotNull
        private Map<String, String> getNotificationDataMapDef2(CaseData caseData) {
            Map<String, String> expectedProperties = new HashMap<>(addCommonProperties(false));
            expectedProperties.putAll(Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
                "defendantReferenceNumber", "10111213",
                "hearingDate", "07-10-2022",
                "hearingTime", "03:30pm",
                PARTY_REFERENCES, "Claimant reference: Not provided - Defendant 1 reference: 6789 - Defendant 2 reference: 10111213",
                CLAIM_LEGAL_ORG_NAME_SPEC, "org name",
                CASEMAN_REF, "000DC001"
            ));
            return expectedProperties;
        }

        @NotNull
        private Map<String, String> getNotificationDataMapDef2WithNoReference(CaseData caseData) {
            Map<String, String> expectedProperties = new HashMap<>(addCommonProperties(false));
            expectedProperties.putAll(Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
                "defendantReferenceNumber", "",
                "hearingDate", "07-10-2022",
                "hearingTime", "03:30pm",
                PARTY_REFERENCES, "Claimant reference: Not provided - Defendant 1 reference: 6789 - Defendant 2 reference: Not provided",
                CLAIM_LEGAL_ORG_NAME_SPEC, "org name",
                CASEMAN_REF, "000DC001"
            ));
            return expectedProperties;
        }

        @NotNull
        private Map<String, String> getNotificationLipDataMap(CaseData caseData) {
            Map<String, String> expectedProperties = new HashMap<>(addCommonProperties(true));
            expectedProperties.putAll(Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
                "hearingDate", "17-05-2023",
                "hearingTime", "11:00am",
                PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
                CLAIM_LEGAL_ORG_NAME_SPEC, "John Doe",
                CASEMAN_REF, "000DC001"
            ));
            return expectedProperties;
        }

        @NotNull
        private Map<String, String> getNotificationLipHmcDataMap(CaseData caseData) {
            Map<String, String> expectedProperties = new HashMap<>(addCommonProperties(true));
            expectedProperties.putAll(Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
                "hearingDate", "17-05-2023",
                "hearingTime", "01:00pm",
                PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
                CLAIM_LEGAL_ORG_NAME_SPEC, "John Doe",
                CASEMAN_REF, "000DC001"
            ));
            return expectedProperties;
        }
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest
                                                                                         .builder().eventId(
                "NOTIFY_DEFENDANT1_HEARING").build()).build())).isEqualTo(TASK_ID_DEFENDANT1);
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvokedWithDefendant2() {
        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest
                                                                                         .builder().eventId(
                "NOTIFY_DEFENDANT2_HEARING").build()).build())).isEqualTo(TASK_ID_DEFENDANT2);
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvokedWithDefendant1Hmc() {
        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest
                                                                                         .builder().eventId(
                "NOTIFY_DEFENDANT1_HEARING_HMC").build()).build())).isEqualTo(TASK_ID_DEFENDANT1_HMC);
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvokedWithDefendant2Hmc() {
        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest
                                                                                         .builder().eventId(
                "NOTIFY_DEFENDANT2_HEARING_HMC").build()).build())).isEqualTo(TASK_ID_DEFENDANT2_HMC);
    }

    @Test
    void shouldReturnEventNotFoundMessage_whenInvokedWithInvalidEvent() {

        // Given: an invalid event id
        CallbackParams callbackParams = CallbackParamsBuilder.builder().request(CallbackRequest.builder()
                                                                                    .eventId("TRIGGER_LOCATION_UPDATE").build()).build();
        // When: I call the camundaActivityId
        // Then: an exception is thrown
        CallbackException ex = assertThrows(CallbackException.class, () -> handler.camundaActivityId(callbackParams),
                                            "A CallbackException was expected to be thrown but wasn't.");
        assertThat(ex.getMessage()).contains("Callback handler received illegal event");
    }
}
