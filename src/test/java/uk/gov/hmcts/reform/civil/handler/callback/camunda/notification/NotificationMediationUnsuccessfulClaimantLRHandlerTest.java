package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulReason;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.Mediation;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationDetailsService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_MEDIATION_UNSUCCESSFUL_CLAIMANT_LR;
import static uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulReason.NOT_CONTACTABLE_CLAIMANT_ONE;
import static uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulReason.NOT_CONTACTABLE_CLAIMANT_TWO;
import static uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulReason.NOT_CONTACTABLE_DEFENDANT_ONE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASEMAN_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CNBC_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.DEFENDANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HMCTS_SIGNATURE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LIP_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LIP_CONTACT_WELSH;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.OPENING_HOURS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PHONE_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.SPEC_UNSPEC_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_HMCTS_SIGNATURE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_OPENING_HOURS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_PHONE_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationMediationUnsuccessfulClaimantLRHandler.TASK_ID_MEDIATION_UNSUCCESSFUL_CLAIMANT_LR;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NotificationMediationUnsuccessfulClaimantLRHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private NotificationService notificationService;
    @Mock
    NotificationsProperties notificationsProperties;
    @Mock
    OrganisationDetailsService organisationDetailsService;
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
    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private NotificationMediationUnsuccessfulClaimantLRHandler notificationHandler;

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

    @Nested
    class AboutToSubmitCallback {

        private static final String CLAIMANT_EMAIL_ADDRESS = "applicantemail@hmcts.net";
        private static final String ORGANISATION_NAME = "Org Name";
        private static final String APPLICANT_PARTY_NAME = "Mike";
        private static final String APPLICANT_PARTY_EMAIL = "mike@party.com";
        private static final String DEFENDANT_PARTY_NAME = "Randy";
        private static final String DEFENDANT_2_PARTY_NAME = "Peaches";
        private static final String REFERENCE_NUMBER = "8372942374";
        private static final Long CCD_REFERENCE_NUMBER = 123456789L;
        private static final String EMAIL_TEMPLATE = "test-notification-id";
        private static final String EMAIL_LIP_TEMPLATE = "test-notification-lip-id";
        private static final String CARM_MAIL_TEMPLATE = "carm-test-notification-id";
        private static final String CARM_LIP_MAIL_TEMPLATE = "carm-test-lip-notification-id";
        private static final String CARM_NO_ATTENDANCE_MAIL_TEMPLATE = "carm-test--no-attendance-notification-id";
        private static final String CLAIMANT_TEXT = "your claim against ";

        @Test
        void shouldSendNotificationToClaimantLr_whenEventIsCalled() {
            //When
            given(notificationsProperties.getMediationUnsuccessfulClaimantLRTemplate()).willReturn(EMAIL_TEMPLATE);
            given(organisationDetailsService.getApplicantLegalOrganisationName(any())).willReturn(ORGANISATION_NAME);
            Map<String, Object> configMap = YamlNotificationTestUtil.loadNotificationsConfig();
            when(configuration.getRaiseQueryLr()).thenReturn((String) configMap.get("raiseQueryLr"));

            //Given
            CaseData caseData = CaseData.builder()
                .respondent1(Party.builder().type(Party.Type.COMPANY).companyName(DEFENDANT_PARTY_NAME).build())
                .applicantSolicitor1UserDetails(IdamUserDetails.builder().email(CLAIMANT_EMAIL_ADDRESS).build())
                .legacyCaseReference(REFERENCE_NUMBER)
                .ccdCaseReference(CCD_REFERENCE_NUMBER)
                .addApplicant2(YesOrNo.NO)
                .addRespondent2(YesOrNo.NO)
                .applicant1Represented(YesOrNo.YES)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId(NOTIFY_MEDIATION_UNSUCCESSFUL_CLAIMANT_LR.name()).build()).build();
            notificationHandler.handle(params);
            //Then
            verify(notificationService, times(1)).sendMail(targetEmail.capture(),
                                                           emailTemplate.capture(),
                                                           notificationDataMap.capture(), reference.capture()
            );
            assertThat(targetEmail.getAllValues().get(0)).isEqualTo(CLAIMANT_EMAIL_ADDRESS);
            assertThat(emailTemplate.getAllValues().get(0)).isEqualTo(EMAIL_TEMPLATE);
            assertThat(notificationDataMap.getAllValues().get(0)).isEqualTo(getPropertyMap(caseData));
        }

        @Test
        void shouldSendNotificationToClaimantLip_whenEventIsCalled() {
            //When
            given(notificationsProperties.getMediationUnsuccessfulClaimantLIPTemplate()).willReturn(EMAIL_LIP_TEMPLATE);
            Map<String, Object> configMap = YamlNotificationTestUtil.loadNotificationsConfig();
            when(configuration.getCnbcContact()).thenReturn((String) configMap.get("cnbcContact"));
            when(configuration.getSpecUnspecContact()).thenReturn((String) configMap.get("specUnspecContact"));

            //Given
            when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
            CaseData caseData = CaseData.builder()
                .applicant1(Party.builder().type(Party.Type.COMPANY).companyName(APPLICANT_PARTY_NAME)
                                .partyEmail(CLAIMANT_EMAIL_ADDRESS)
                                .build())
                .respondent1(Party.builder().type(Party.Type.COMPANY).companyName(DEFENDANT_PARTY_NAME).build())
                .legacyCaseReference(REFERENCE_NUMBER)
                .addApplicant2(YesOrNo.NO)
                .addRespondent2(YesOrNo.NO)
                .applicant1Represented(YesOrNo.NO)
                .respondent1Represented(YesOrNo.NO)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId(NOTIFY_MEDIATION_UNSUCCESSFUL_CLAIMANT_LR.name()).build()).build();
            notificationHandler.handle(params);
            //Then
            verify(notificationService, times(1)).sendMail(targetEmail.capture(),
                                                           emailTemplate.capture(),
                                                           notificationDataMap.capture(), reference.capture()
            );
            assertThat(targetEmail.getAllValues().get(0)).isEqualTo(CLAIMANT_EMAIL_ADDRESS);
            assertThat(emailTemplate.getAllValues().get(0)).isEqualTo(EMAIL_LIP_TEMPLATE);
            assertThat(notificationDataMap.getAllValues().get(0)).isEqualTo(getPropertyLipMap());
        }

        @Test
        void shouldSendNotificationToClaimantLip_whenEventIsCalledClaimIssueInBilingual() {
            //When
            given(notificationsProperties.getMediationUnsuccessfulClaimantLIPWelshTemplate()).willReturn(EMAIL_LIP_TEMPLATE);
            Map<String, Object> configMap = YamlNotificationTestUtil.loadNotificationsConfig();
            when(configuration.getCnbcContact()).thenReturn((String) configMap.get("cnbcContact"));
            when(configuration.getSpecUnspecContact()).thenReturn((String) configMap.get("specUnspecContact"));

            //Given
            when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
            CaseData caseData = CaseData.builder()
                .applicant1(Party.builder().type(Party.Type.COMPANY).companyName(APPLICANT_PARTY_NAME)
                                .partyEmail(CLAIMANT_EMAIL_ADDRESS)
                                .build())
                .respondent1(Party.builder().type(Party.Type.COMPANY).companyName(DEFENDANT_PARTY_NAME).build())
                .legacyCaseReference(REFERENCE_NUMBER)
                .addApplicant2(YesOrNo.NO)
                .addRespondent2(YesOrNo.NO)
                .applicant1Represented(YesOrNo.NO)
                .claimantBilingualLanguagePreference("BOTH")
                .respondent1Represented(YesOrNo.NO)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId(NOTIFY_MEDIATION_UNSUCCESSFUL_CLAIMANT_LR.name()).build()).build();
            notificationHandler.handle(params);
            //Then
            verify(notificationService, times(1)).sendMail(targetEmail.capture(),
                                                           emailTemplate.capture(),
                                                           notificationDataMap.capture(), reference.capture()
            );
            assertThat(targetEmail.getAllValues().get(0)).isEqualTo(CLAIMANT_EMAIL_ADDRESS);
            assertThat(emailTemplate.getAllValues().get(0)).isEqualTo(EMAIL_LIP_TEMPLATE);
            assertThat(notificationDataMap.getAllValues().get(0)).isEqualTo(getPropertyLipMap());
        }

        @Test
        void shouldReturnCorrectCamundaActivityId_whenInvoked() {
            assertThat(notificationHandler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
                NOTIFY_MEDIATION_UNSUCCESSFUL_CLAIMANT_LR.name()).build()).build())).isEqualTo(TASK_ID_MEDIATION_UNSUCCESSFUL_CLAIMANT_LR);
        }

        @Nested
        class CARM {
            @BeforeEach
            void setup() {
                when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);
            }

            @ParameterizedTest
            @EnumSource(value = MediationUnsuccessfulReason.class, names = {"PARTY_WITHDRAWS",
                "APPOINTMENT_NO_AGREEMENT", "APPOINTMENT_NOT_ASSIGNED", "NOT_CONTACTABLE_DEFENDANT_ONE",
                "NOT_CONTACTABLE_DEFENDANT_TWO"})
            void shouldSendNotificationToClaimantLr_whenEventIsCalled(MediationUnsuccessfulReason reason) {
                //When
                given(notificationsProperties.getMediationUnsuccessfulLRTemplate()).willReturn(CARM_MAIL_TEMPLATE);
                given(organisationDetailsService.getApplicantLegalOrganisationName(any())).willReturn(ORGANISATION_NAME);
                Map<String, Object> configMap = YamlNotificationTestUtil.loadNotificationsConfig();
                when(configuration.getRaiseQueryLr()).thenReturn((String) configMap.get("raiseQueryLr"));
                //Given
                CaseData caseData = CaseData.builder()
                    .respondent1(Party.builder().type(Party.Type.COMPANY).companyName(DEFENDANT_PARTY_NAME).build())
                    .applicantSolicitor1UserDetails(IdamUserDetails.builder().email(CLAIMANT_EMAIL_ADDRESS).build())
                    .legacyCaseReference(REFERENCE_NUMBER)
                    .ccdCaseReference(CCD_REFERENCE_NUMBER)
                    .addApplicant2(YesOrNo.NO)
                    .addRespondent2(YesOrNo.NO)
                    .applicant1Represented(YesOrNo.YES)
                    .mediation(Mediation.builder()
                                   .mediationUnsuccessfulReasonsMultiSelect(List.of(reason)).build())
                    .build();
                CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                    .request(CallbackRequest.builder().eventId(NOTIFY_MEDIATION_UNSUCCESSFUL_CLAIMANT_LR.name()).build()).build();
                notificationHandler.handle(params);
                //Then
                verify(notificationService, times(1)).sendMail(targetEmail.capture(),
                                                               emailTemplate.capture(),
                                                               notificationDataMap.capture(), reference.capture()
                );
                assertThat(targetEmail.getAllValues().get(0)).isEqualTo(CLAIMANT_EMAIL_ADDRESS);
                assertThat(emailTemplate.getAllValues().get(0)).isEqualTo(CARM_MAIL_TEMPLATE);
                assertThat(notificationDataMap.getAllValues().get(0)).isEqualTo(getCarmPropertyMap(caseData));
            }

            @ParameterizedTest
            @EnumSource(value = MediationUnsuccessfulReason.class, names = {"PARTY_WITHDRAWS",
                "APPOINTMENT_NO_AGREEMENT", "APPOINTMENT_NOT_ASSIGNED", "NOT_CONTACTABLE_DEFENDANT_ONE",
                "NOT_CONTACTABLE_DEFENDANT_TWO"})
            void shouldSendNotificationToClaimantLr_1v2SS_whenEventIsCalled(MediationUnsuccessfulReason reason) {
                //When
                given(notificationsProperties.getMediationUnsuccessfulLRTemplate()).willReturn(CARM_MAIL_TEMPLATE);
                given(organisationDetailsService.getApplicantLegalOrganisationName(any())).willReturn(ORGANISATION_NAME);
                Map<String, Object> configMap = YamlNotificationTestUtil.loadNotificationsConfig();
                when(configuration.getRaiseQueryLr()).thenReturn((String) configMap.get("raiseQueryLr"));
                //Given
                CaseData caseData = CaseData.builder()
                    .respondent1(Party.builder().type(Party.Type.COMPANY).companyName(DEFENDANT_PARTY_NAME).build())
                    .respondent2(Party.builder().type(Party.Type.COMPANY).companyName(DEFENDANT_2_PARTY_NAME).build())
                    .applicantSolicitor1UserDetails(IdamUserDetails.builder().email(CLAIMANT_EMAIL_ADDRESS).build())
                    .legacyCaseReference(REFERENCE_NUMBER)
                    .ccdCaseReference(CCD_REFERENCE_NUMBER)
                    .addApplicant2(YesOrNo.NO)
                    .addRespondent2(YesOrNo.YES)
                    .applicant1Represented(YesOrNo.YES)
                    .respondent2SameLegalRepresentative(YesOrNo.YES)
                    .mediation(Mediation.builder()
                                   .mediationUnsuccessfulReasonsMultiSelect(List.of(reason)).build())
                    .build();
                CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                    .request(CallbackRequest.builder().eventId(NOTIFY_MEDIATION_UNSUCCESSFUL_CLAIMANT_LR.name()).build()).build();
                notificationHandler.handle(params);
                //Then
                verify(notificationService, times(1)).sendMail(targetEmail.capture(),
                                                               emailTemplate.capture(),
                                                               notificationDataMap.capture(), reference.capture()
                );
                assertThat(targetEmail.getAllValues().get(0)).isEqualTo(CLAIMANT_EMAIL_ADDRESS);
                assertThat(emailTemplate.getAllValues().get(0)).isEqualTo(CARM_MAIL_TEMPLATE);
                assertThat(notificationDataMap.getAllValues().get(0)).isEqualTo(getCarm1v2PropertyMap(caseData));
            }

            @ParameterizedTest
            @EnumSource(value = MediationUnsuccessfulReason.class, names = {"PARTY_WITHDRAWS",
                "APPOINTMENT_NO_AGREEMENT", "APPOINTMENT_NOT_ASSIGNED", "NOT_CONTACTABLE_DEFENDANT_ONE",
                "NOT_CONTACTABLE_DEFENDANT_TWO"})
            void shouldSendNotificationToClaimantLr_1v2DS_whenEventIsCalled(MediationUnsuccessfulReason reason) {
                //When
                given(notificationsProperties.getMediationUnsuccessfulLRTemplate()).willReturn(CARM_MAIL_TEMPLATE);
                given(organisationDetailsService.getApplicantLegalOrganisationName(any())).willReturn(ORGANISATION_NAME);
                Map<String, Object> configMap = YamlNotificationTestUtil.loadNotificationsConfig();
                when(configuration.getRaiseQueryLr()).thenReturn((String) configMap.get("raiseQueryLr"));

                //Given
                CaseData caseData = CaseData.builder()
                    .respondent1(Party.builder().type(Party.Type.COMPANY).companyName(DEFENDANT_PARTY_NAME).build())
                    .respondent2(Party.builder().type(Party.Type.COMPANY).companyName(DEFENDANT_2_PARTY_NAME).build())
                    .applicantSolicitor1UserDetails(IdamUserDetails.builder().email(CLAIMANT_EMAIL_ADDRESS).build())
                    .legacyCaseReference(REFERENCE_NUMBER)
                    .ccdCaseReference(CCD_REFERENCE_NUMBER)
                    .addApplicant2(YesOrNo.NO)
                    .addRespondent2(YesOrNo.YES)
                    .applicant1Represented(YesOrNo.YES)
                    .respondent2SameLegalRepresentative(YesOrNo.NO)
                    .mediation(Mediation.builder()
                                   .mediationUnsuccessfulReasonsMultiSelect(List.of(reason)).build())
                    .build();
                CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                    .request(CallbackRequest.builder().eventId(NOTIFY_MEDIATION_UNSUCCESSFUL_CLAIMANT_LR.name()).build()).build();

                notificationHandler.handle(params);
                //Then
                verify(notificationService, times(1)).sendMail(targetEmail.capture(),
                                                               emailTemplate.capture(),
                                                               notificationDataMap.capture(), reference.capture()
                );
                assertThat(targetEmail.getAllValues().get(0)).isEqualTo(CLAIMANT_EMAIL_ADDRESS);
                assertThat(emailTemplate.getAllValues().get(0)).isEqualTo(CARM_MAIL_TEMPLATE);
                assertThat(notificationDataMap.getAllValues().get(0)).isEqualTo(getCarm1v2PropertyMap(caseData));
            }

            @ParameterizedTest
            @EnumSource(value = MediationUnsuccessfulReason.class, names = {"PARTY_WITHDRAWS",
                "APPOINTMENT_NO_AGREEMENT", "APPOINTMENT_NOT_ASSIGNED", "NOT_CONTACTABLE_DEFENDANT_ONE",
                "NOT_CONTACTABLE_DEFENDANT_TWO"})
            void shouldSendNotificationToClaimantLIP_1v1_whenEventIsCalled(MediationUnsuccessfulReason reason) {
                //When
                given(notificationsProperties.getMediationUnsuccessfulLIPTemplate()).willReturn(CARM_LIP_MAIL_TEMPLATE);
                Map<String, Object> configMap = YamlNotificationTestUtil.loadNotificationsConfig();
                when(configuration.getCnbcContact()).thenReturn((String) configMap.get("cnbcContact"));
                when(configuration.getSpecUnspecContact()).thenReturn((String) configMap.get("specUnspecContact"));

                //Given
                CaseData caseData = CaseData.builder()
                    .applicant1(Party.builder().type(Party.Type.COMPANY).companyName(APPLICANT_PARTY_NAME)
                                    .partyEmail(APPLICANT_PARTY_EMAIL).build())
                    .respondent1(Party.builder().type(Party.Type.COMPANY).companyName(DEFENDANT_PARTY_NAME).build())
                    .legacyCaseReference(REFERENCE_NUMBER)
                    .ccdCaseReference(CCD_REFERENCE_NUMBER)
                    .addApplicant2(YesOrNo.NO)
                    .addRespondent2(YesOrNo.NO)
                    .applicant1Represented(YesOrNo.NO)
                    .mediation(Mediation.builder()
                                   .mediationUnsuccessfulReasonsMultiSelect(List.of(reason)).build())
                    .build();
                CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                    .request(CallbackRequest.builder().eventId(NOTIFY_MEDIATION_UNSUCCESSFUL_CLAIMANT_LR.name()).build()).build();

                notificationHandler.handle(params);
                //Then
                verify(notificationService, times(1)).sendMail(targetEmail.capture(),
                                                               emailTemplate.capture(),
                                                               notificationDataMap.capture(), reference.capture()
                );
                assertThat(targetEmail.getAllValues().get(0)).isEqualTo(APPLICANT_PARTY_EMAIL);
                assertThat(emailTemplate.getAllValues().get(0)).isEqualTo(CARM_LIP_MAIL_TEMPLATE);
                assertThat(notificationDataMap.getAllValues().get(0)).isEqualTo(getCarmLipClaimantPropertyMap());
            }

            @ParameterizedTest
            @EnumSource(value = MediationUnsuccessfulReason.class, names = {"PARTY_WITHDRAWS",
                "APPOINTMENT_NO_AGREEMENT", "APPOINTMENT_NOT_ASSIGNED", "NOT_CONTACTABLE_DEFENDANT_ONE",
                "NOT_CONTACTABLE_DEFENDANT_TWO"})
            void shouldSendNotificationToClaimantLIP_1v1_WithBilingualwhenEventIsCalled(MediationUnsuccessfulReason reason) {
                //When
                given(notificationsProperties.getMediationUnsuccessfulLIPTemplateWelsh()).willReturn(CARM_LIP_MAIL_TEMPLATE);
                Map<String, Object> configMap = YamlNotificationTestUtil.loadNotificationsConfig();
                when(configuration.getCnbcContact()).thenReturn((String) configMap.get("cnbcContact"));
                when(configuration.getSpecUnspecContact()).thenReturn((String) configMap.get("specUnspecContact"));

                //Given
                CaseData caseData = CaseData.builder()
                    .applicant1(Party.builder().type(Party.Type.COMPANY).companyName(APPLICANT_PARTY_NAME)
                                    .partyEmail(APPLICANT_PARTY_EMAIL).build())
                    .respondent1(Party.builder().type(Party.Type.COMPANY).companyName(DEFENDANT_PARTY_NAME).build())
                    .legacyCaseReference(REFERENCE_NUMBER)
                    .ccdCaseReference(CCD_REFERENCE_NUMBER)
                    .addApplicant2(YesOrNo.NO)
                    .addRespondent2(YesOrNo.NO)
                    .applicant1Represented(YesOrNo.NO)
                    .mediation(Mediation.builder()
                                   .mediationUnsuccessfulReasonsMultiSelect(List.of(reason)).build())
                    .claimantBilingualLanguagePreference("BOTH")
                    .build();
                CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                    .request(CallbackRequest.builder().eventId(NOTIFY_MEDIATION_UNSUCCESSFUL_CLAIMANT_LR.name()).build()).build();

                notificationHandler.handle(params);
                //Then
                verify(notificationService, times(1)).sendMail(targetEmail.capture(),
                                                               emailTemplate.capture(),
                                                               notificationDataMap.capture(), reference.capture()
                );
                assertThat(targetEmail.getAllValues().get(0)).isEqualTo(APPLICANT_PARTY_EMAIL);
                assertThat(emailTemplate.getAllValues().get(0)).isEqualTo(CARM_LIP_MAIL_TEMPLATE);
                assertThat(notificationDataMap.getAllValues().get(0)).isEqualTo(getCarmLipClaimantPropertyMap());
            }

            @ParameterizedTest
            @EnumSource(value = MediationUnsuccessfulReason.class, names = {"NOT_CONTACTABLE_CLAIMANT_ONE", "NOT_CONTACTABLE_CLAIMANT_TWO"})
            void shouldSendNotificationToClaimantLR_NoAttendance_whenEventIsCalled(MediationUnsuccessfulReason reason) {
                //When
                given(notificationsProperties.getMediationUnsuccessfulNoAttendanceLRTemplate()).willReturn(CARM_NO_ATTENDANCE_MAIL_TEMPLATE);
                given(organisationDetailsService.getApplicantLegalOrganisationName(any())).willReturn(ORGANISATION_NAME);
                Map<String, Object> configMap = YamlNotificationTestUtil.loadNotificationsConfig();
                when(configuration.getRaiseQueryLr()).thenReturn((String) configMap.get("raiseQueryLr"));

                //Given
                CaseData caseData = CaseData.builder()
                    .respondent1(Party.builder().type(Party.Type.COMPANY).companyName(DEFENDANT_PARTY_NAME).build())
                    .applicantSolicitor1UserDetails(IdamUserDetails.builder().email(CLAIMANT_EMAIL_ADDRESS).build())
                    .legacyCaseReference(REFERENCE_NUMBER)
                    .ccdCaseReference(CCD_REFERENCE_NUMBER)
                    .addApplicant2(YesOrNo.NO)
                    .addRespondent2(YesOrNo.NO)
                    .applicant1Represented(YesOrNo.YES)
                    .mediation(Mediation.builder()
                                   .mediationUnsuccessfulReasonsMultiSelect(List.of(reason)).build())
                    .build();
                CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                    .request(CallbackRequest.builder().eventId(NOTIFY_MEDIATION_UNSUCCESSFUL_CLAIMANT_LR.name()).build()).build();

                notificationHandler.handle(params);
                //Then
                verify(notificationService, times(1)).sendMail(targetEmail.capture(),
                                                               emailTemplate.capture(),
                                                               notificationDataMap.capture(), reference.capture()
                );
                assertThat(targetEmail.getAllValues().get(0)).isEqualTo(CLAIMANT_EMAIL_ADDRESS);
                assertThat(emailTemplate.getAllValues().get(0)).isEqualTo(CARM_NO_ATTENDANCE_MAIL_TEMPLATE);
                assertThat(notificationDataMap.getAllValues().get(0)).isEqualTo(getCarmNoAttendancePropertyMap(caseData));
            }

            @Test
            void shouldSendNotificationToClaimantLR_NoAttendance_whenMoreThanOneReason() {
                //When
                given(notificationsProperties.getMediationUnsuccessfulNoAttendanceLRTemplate()).willReturn(CARM_NO_ATTENDANCE_MAIL_TEMPLATE);
                given(organisationDetailsService.getApplicantLegalOrganisationName(any())).willReturn(ORGANISATION_NAME);
                Map<String, Object> configMap = YamlNotificationTestUtil.loadNotificationsConfig();
                when(configuration.getRaiseQueryLr()).thenReturn((String) configMap.get("raiseQueryLr"));

                //Given
                CaseData caseData = CaseData.builder()
                    .respondent1(Party.builder().type(Party.Type.COMPANY).companyName(DEFENDANT_PARTY_NAME).build())
                    .applicantSolicitor1UserDetails(IdamUserDetails.builder().email(CLAIMANT_EMAIL_ADDRESS).build())
                    .legacyCaseReference(REFERENCE_NUMBER)
                    .ccdCaseReference(CCD_REFERENCE_NUMBER)
                    .addApplicant2(YesOrNo.NO)
                    .addRespondent2(YesOrNo.NO)
                    .applicant1Represented(YesOrNo.YES)
                    .mediation(Mediation.builder()
                                   .mediationUnsuccessfulReasonsMultiSelect(List.of(NOT_CONTACTABLE_CLAIMANT_ONE, NOT_CONTACTABLE_DEFENDANT_ONE)).build())
                    .build();
                CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                    .request(CallbackRequest.builder().eventId(NOTIFY_MEDIATION_UNSUCCESSFUL_CLAIMANT_LR.name()).build()).build();

                notificationHandler.handle(params);
                //Then
                verify(notificationService, times(1)).sendMail(targetEmail.capture(),
                                                               emailTemplate.capture(),
                                                               notificationDataMap.capture(), reference.capture()
                );
                assertThat(targetEmail.getAllValues().get(0)).isEqualTo(CLAIMANT_EMAIL_ADDRESS);
                assertThat(emailTemplate.getAllValues().get(0)).isEqualTo(CARM_NO_ATTENDANCE_MAIL_TEMPLATE);
                assertThat(notificationDataMap.getAllValues().get(0)).isEqualTo(getCarmNoAttendancePropertyMap(caseData));
            }

            @ParameterizedTest
            @EnumSource(value = MediationUnsuccessfulReason.class, names = {"NOT_CONTACTABLE_CLAIMANT_TWO"})
            void shouldSendNotificationToClaimantLR_2v1_NoAttendance_whenEventIsCalled(MediationUnsuccessfulReason reason) {
                //When
                given(notificationsProperties.getMediationUnsuccessfulNoAttendanceLRTemplate()).willReturn(CARM_NO_ATTENDANCE_MAIL_TEMPLATE);
                given(organisationDetailsService.getApplicantLegalOrganisationName(any())).willReturn(ORGANISATION_NAME);
                Map<String, Object> configMap = YamlNotificationTestUtil.loadNotificationsConfig();
                when(configuration.getRaiseQueryLr()).thenReturn((String) configMap.get("raiseQueryLr"));

                //Given
                CaseData caseData = CaseData.builder()
                    .respondent1(Party.builder().type(Party.Type.COMPANY).companyName(DEFENDANT_PARTY_NAME).build())
                    .applicantSolicitor1UserDetails(IdamUserDetails.builder().email(CLAIMANT_EMAIL_ADDRESS).build())
                    .legacyCaseReference(REFERENCE_NUMBER)
                    .ccdCaseReference(CCD_REFERENCE_NUMBER)
                    .addApplicant2(YesOrNo.YES)
                    .addRespondent2(YesOrNo.NO)
                    .applicant1Represented(YesOrNo.YES)
                    .mediation(Mediation.builder()
                                   .mediationUnsuccessfulReasonsMultiSelect(List.of(reason)).build())
                    .build();
                CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                    .request(CallbackRequest.builder().eventId(NOTIFY_MEDIATION_UNSUCCESSFUL_CLAIMANT_LR.name()).build()).build();

                notificationHandler.handle(params);
                //Then
                verify(notificationService, times(1)).sendMail(targetEmail.capture(),
                                                               emailTemplate.capture(),
                                                               notificationDataMap.capture(), reference.capture()
                );
                assertThat(targetEmail.getAllValues().get(0)).isEqualTo(CLAIMANT_EMAIL_ADDRESS);
                assertThat(emailTemplate.getAllValues().get(0)).isEqualTo(CARM_NO_ATTENDANCE_MAIL_TEMPLATE);
                assertThat(notificationDataMap.getAllValues().get(0)).isEqualTo(getCarmNoAttendancePropertyMap(caseData));
            }

            @Test
            void shouldSendNotificationToClaimantLR_2v1_NoAttendance_whenMoreThanOneReason() {
                //When
                given(notificationsProperties.getMediationUnsuccessfulNoAttendanceLRTemplate()).willReturn(CARM_NO_ATTENDANCE_MAIL_TEMPLATE);
                given(organisationDetailsService.getApplicantLegalOrganisationName(any())).willReturn(ORGANISATION_NAME);
                Map<String, Object> configMap = YamlNotificationTestUtil.loadNotificationsConfig();
                when(configuration.getRaiseQueryLr()).thenReturn((String) configMap.get("raiseQueryLr"));

                //Given
                CaseData caseData = CaseData.builder()
                    .respondent1(Party.builder().type(Party.Type.COMPANY).companyName(DEFENDANT_PARTY_NAME).build())
                    .applicantSolicitor1UserDetails(IdamUserDetails.builder().email(CLAIMANT_EMAIL_ADDRESS).build())
                    .legacyCaseReference(REFERENCE_NUMBER)
                    .ccdCaseReference(CCD_REFERENCE_NUMBER)
                    .addApplicant2(YesOrNo.YES)
                    .addRespondent2(YesOrNo.NO)
                    .applicant1Represented(YesOrNo.YES)
                    .mediation(Mediation.builder()
                                   .mediationUnsuccessfulReasonsMultiSelect(List.of(NOT_CONTACTABLE_CLAIMANT_TWO, NOT_CONTACTABLE_DEFENDANT_ONE)).build())
                    .build();
                CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                    .request(CallbackRequest.builder().eventId(NOTIFY_MEDIATION_UNSUCCESSFUL_CLAIMANT_LR.name()).build()).build();

                notificationHandler.handle(params);
                //Then
                verify(notificationService, times(1)).sendMail(targetEmail.capture(),
                                                               emailTemplate.capture(),
                                                               notificationDataMap.capture(), reference.capture()
                );
                assertThat(targetEmail.getAllValues().get(0)).isEqualTo(CLAIMANT_EMAIL_ADDRESS);
                assertThat(emailTemplate.getAllValues().get(0)).isEqualTo(CARM_NO_ATTENDANCE_MAIL_TEMPLATE);
                assertThat(notificationDataMap.getAllValues().get(0)).isEqualTo(getCarmNoAttendancePropertyMap(caseData));
            }
        }

        @NotNull
        private Map<String, String> getPropertyMap(CaseData caseData) {
            Map<String, String> properties = new HashMap<>(addCommonProperties(false));
            properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, ORGANISATION_NAME);
            properties.put(DEFENDANT_NAME, DEFENDANT_PARTY_NAME);
            properties.put(CLAIM_REFERENCE_NUMBER, CCD_REFERENCE_NUMBER.toString());
            properties.put(PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData));
            properties.put(CASEMAN_REF, caseData.getLegacyCaseReference());
            return properties;
        }

        @NotNull
        private Map<String, String> getCarmPropertyMap(CaseData caseData) {
            Map<String, String> properties = new HashMap<>(addCommonProperties(false));
            properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, ORGANISATION_NAME);
            properties.put(PARTY_NAME, CLAIMANT_TEXT + DEFENDANT_PARTY_NAME);
            properties.put(CLAIM_REFERENCE_NUMBER, CCD_REFERENCE_NUMBER.toString());
            properties.put(PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData));
            properties.put(CASEMAN_REF, caseData.getLegacyCaseReference());
            return properties;
        }

        @NotNull
        private Map<String, String> getCarm1v2PropertyMap(CaseData caseData) {
            Map<String, String> properties = new HashMap<>(addCommonProperties(false));
            properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, ORGANISATION_NAME);
            properties.put(PARTY_NAME, CLAIMANT_TEXT + DEFENDANT_PARTY_NAME + " and " + DEFENDANT_2_PARTY_NAME);
            properties.put(CLAIM_REFERENCE_NUMBER, CCD_REFERENCE_NUMBER.toString());
            properties.put(PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData));
            properties.put(CASEMAN_REF, caseData.getLegacyCaseReference());
            return properties;
        }

        @NotNull
        private Map<String, String> getCarmNoAttendancePropertyMap(CaseData caseData) {
            Map<String, String> properties = new HashMap<>(addCommonProperties(false));
            properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, ORGANISATION_NAME);
            properties.put(CLAIM_REFERENCE_NUMBER, CCD_REFERENCE_NUMBER.toString());
            properties.put(PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData));
            properties.put(CASEMAN_REF, caseData.getLegacyCaseReference());
            return properties;
        }

        @NotNull
        private Map<String, String> getPropertyLipMap() {
            Map<String, String> properties = new HashMap<>(addCommonProperties(true));
            properties.put(CLAIMANT_NAME, APPLICANT_PARTY_NAME);
            properties.put(RESPONDENT_NAME, DEFENDANT_PARTY_NAME);
            properties.put(CLAIM_REFERENCE_NUMBER, REFERENCE_NUMBER);
            return properties;
        }

        @NotNull
        private Map<String, String> getCarmLipClaimantPropertyMap() {
            Map<String, String> properties = new HashMap<>(addCommonProperties(true));
            properties.put(PARTY_NAME, APPLICANT_PARTY_NAME);
            properties.put(CLAIM_REFERENCE_NUMBER, CCD_REFERENCE_NUMBER.toString());
            return properties;
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

    }
}
