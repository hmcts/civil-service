package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationDetailsService;

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
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.DEFENDANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationMediationUnsuccessfulClaimantLRHandler.TASK_ID_MEDIATION_UNSUCCESSFUL_CLAIMANT_LR;

@SpringBootTest(classes = {
    NotificationMediationUnsuccessfulClaimantLRHandler.class,
    OrganisationDetailsService.class,
    JacksonAutoConfiguration.class,
})
class NotificationMediationUnsuccessfulClaimantLRHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private NotificationService notificationService;
    @MockBean
    NotificationsProperties notificationsProperties;
    @MockBean
    OrganisationDetailsService organisationDetailsService;
    @Captor
    private ArgumentCaptor<String> targetEmail;
    @Captor
    private ArgumentCaptor<String> emailTemplate;
    @Captor
    private ArgumentCaptor<Map<String, String>> notificationDataMap;
    @Captor
    private ArgumentCaptor<String> reference;
    @MockBean
    private FeatureToggleService featureToggleService;

    @Autowired
    private NotificationMediationUnsuccessfulClaimantLRHandler notificationHandler;

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

        private static final Map<String, String> PROPERTY_MAP = Map.of(CLAIM_LEGAL_ORG_NAME_SPEC, ORGANISATION_NAME,
                                                                       DEFENDANT_NAME, DEFENDANT_PARTY_NAME,
                                                                       CLAIM_REFERENCE_NUMBER, REFERENCE_NUMBER);
        private static final Map<String, String> PROPERTY_LIP_MAP = Map.of(CLAIMANT_NAME, APPLICANT_PARTY_NAME,
                                                                           RESPONDENT_NAME, DEFENDANT_PARTY_NAME,
                                                                           CLAIM_REFERENCE_NUMBER, REFERENCE_NUMBER);
        private static final Map<String, String> CARM_PROPERTY_MAP = Map.of(CLAIM_LEGAL_ORG_NAME_SPEC, ORGANISATION_NAME,
                                                                       PARTY_NAME, CLAIMANT_TEXT + DEFENDANT_PARTY_NAME,
                                                                       CLAIM_REFERENCE_NUMBER, CCD_REFERENCE_NUMBER.toString());
        private static final Map<String, String> CARM_1V2_PROPERTY_MAP = Map.of(CLAIM_LEGAL_ORG_NAME_SPEC, ORGANISATION_NAME,
                                                                       PARTY_NAME, CLAIMANT_TEXT + DEFENDANT_PARTY_NAME + " and " + DEFENDANT_2_PARTY_NAME,
                                                                       CLAIM_REFERENCE_NUMBER, CCD_REFERENCE_NUMBER.toString());
        private static final Map<String, String> CARM_LIP_CLAIMANT_PROPERTY_MAP = Map.of(PARTY_NAME, APPLICANT_PARTY_NAME,
                                                                       CLAIM_REFERENCE_NUMBER, CCD_REFERENCE_NUMBER.toString());
        private static final Map<String, String> CARM_NO_ATTENDANCE_CLAIMANT_PROPERTY_MAP = Map.of(CLAIM_LEGAL_ORG_NAME_SPEC, ORGANISATION_NAME,
                                                                       CLAIM_REFERENCE_NUMBER, CCD_REFERENCE_NUMBER.toString());

        @BeforeEach
        void setUp() {
            given(notificationsProperties.getMediationUnsuccessfulClaimantLRTemplate()).willReturn(EMAIL_TEMPLATE);
            given(notificationsProperties.getMediationUnsuccessfulClaimantLIPTemplate()).willReturn(EMAIL_LIP_TEMPLATE);
            given(notificationsProperties.getMediationUnsuccessfulLRTemplate()).willReturn(CARM_MAIL_TEMPLATE);
            given(notificationsProperties.getMediationUnsuccessfulLIPTemplate()).willReturn(CARM_LIP_MAIL_TEMPLATE);
            given(notificationsProperties.getMediationUnsuccessfulLIPTemplateWelsh()).willReturn(CARM_LIP_MAIL_TEMPLATE);
            given(notificationsProperties.getMediationUnsuccessfulNoAttendanceLRTemplate()).willReturn(CARM_NO_ATTENDANCE_MAIL_TEMPLATE);
            given(organisationDetailsService.getApplicantLegalOrganisationName(any())).willReturn(ORGANISATION_NAME);
            given(notificationsProperties.getMediationUnsuccessfulClaimantLIPWelshTemplate()).willReturn(EMAIL_LIP_TEMPLATE);
        }

        @Test
        void shouldSendNotificationToClaimantLr_whenEventIsCalled() {
            //Given
            CaseData caseData = CaseData.builder()
                .respondent1(Party.builder().type(Party.Type.COMPANY).companyName(DEFENDANT_PARTY_NAME).build())
                .applicantSolicitor1UserDetails(IdamUserDetails.builder().email(CLAIMANT_EMAIL_ADDRESS).build())
                .legacyCaseReference(REFERENCE_NUMBER)
                .addApplicant2(YesOrNo.NO)
                .addRespondent2(YesOrNo.NO)
                .applicant1Represented(YesOrNo.YES)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId(NOTIFY_MEDIATION_UNSUCCESSFUL_CLAIMANT_LR.name()).build()).build();
            //When
            notificationHandler.handle(params);
            //Then
            verify(notificationService, times(1)).sendMail(targetEmail.capture(),
                                                           emailTemplate.capture(),
                                                           notificationDataMap.capture(), reference.capture()
            );
            assertThat(targetEmail.getAllValues().get(0)).isEqualTo(CLAIMANT_EMAIL_ADDRESS);
            assertThat(emailTemplate.getAllValues().get(0)).isEqualTo(EMAIL_TEMPLATE);
            assertThat(notificationDataMap.getAllValues().get(0)).isEqualTo(PROPERTY_MAP);
        }

        @Test
        void shouldSendNotificationToClaimantLip_whenEventIsCalled() {
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
            //When
            notificationHandler.handle(params);
            //Then
            verify(notificationService, times(1)).sendMail(targetEmail.capture(),
                                                           emailTemplate.capture(),
                                                           notificationDataMap.capture(), reference.capture()
            );
            assertThat(targetEmail.getAllValues().get(0)).isEqualTo(CLAIMANT_EMAIL_ADDRESS);
            assertThat(emailTemplate.getAllValues().get(0)).isEqualTo(EMAIL_LIP_TEMPLATE);
            assertThat(notificationDataMap.getAllValues().get(0)).isEqualTo(PROPERTY_LIP_MAP);
        }

        @Test
        void shouldSendNotificationToClaimantLip_whenEventIsCalledClaimIssueInBilingual() {
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
            //When
            notificationHandler.handle(params);
            //Then
            verify(notificationService, times(1)).sendMail(targetEmail.capture(),
                                                           emailTemplate.capture(),
                                                           notificationDataMap.capture(), reference.capture()
            );
            assertThat(targetEmail.getAllValues().get(0)).isEqualTo(CLAIMANT_EMAIL_ADDRESS);
            assertThat(emailTemplate.getAllValues().get(0)).isEqualTo(EMAIL_LIP_TEMPLATE);
            assertThat(notificationDataMap.getAllValues().get(0)).isEqualTo(PROPERTY_LIP_MAP);
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

                //When
                notificationHandler.handle(params);
                //Then
                verify(notificationService, times(1)).sendMail(targetEmail.capture(),
                                                               emailTemplate.capture(),
                                                               notificationDataMap.capture(), reference.capture()
                );
                assertThat(targetEmail.getAllValues().get(0)).isEqualTo(CLAIMANT_EMAIL_ADDRESS);
                assertThat(emailTemplate.getAllValues().get(0)).isEqualTo(CARM_MAIL_TEMPLATE);
                assertThat(notificationDataMap.getAllValues().get(0)).isEqualTo(CARM_PROPERTY_MAP);
            }

            @ParameterizedTest
            @EnumSource(value = MediationUnsuccessfulReason.class, names = {"PARTY_WITHDRAWS",
                "APPOINTMENT_NO_AGREEMENT", "APPOINTMENT_NOT_ASSIGNED", "NOT_CONTACTABLE_DEFENDANT_ONE",
                "NOT_CONTACTABLE_DEFENDANT_TWO"})
            void shouldSendNotificationToClaimantLr_1v2SS_whenEventIsCalled(MediationUnsuccessfulReason reason) {
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

                //When
                notificationHandler.handle(params);
                //Then
                verify(notificationService, times(1)).sendMail(targetEmail.capture(),
                                                               emailTemplate.capture(),
                                                               notificationDataMap.capture(), reference.capture()
                );
                assertThat(targetEmail.getAllValues().get(0)).isEqualTo(CLAIMANT_EMAIL_ADDRESS);
                assertThat(emailTemplate.getAllValues().get(0)).isEqualTo(CARM_MAIL_TEMPLATE);
                assertThat(notificationDataMap.getAllValues().get(0)).isEqualTo(CARM_1V2_PROPERTY_MAP);
            }

            @ParameterizedTest
            @EnumSource(value = MediationUnsuccessfulReason.class, names = {"PARTY_WITHDRAWS",
                "APPOINTMENT_NO_AGREEMENT", "APPOINTMENT_NOT_ASSIGNED", "NOT_CONTACTABLE_DEFENDANT_ONE",
                "NOT_CONTACTABLE_DEFENDANT_TWO"})
            void shouldSendNotificationToClaimantLr_1v2DS_whenEventIsCalled(MediationUnsuccessfulReason reason) {
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

                //When
                notificationHandler.handle(params);
                //Then
                verify(notificationService, times(1)).sendMail(targetEmail.capture(),
                                                               emailTemplate.capture(),
                                                               notificationDataMap.capture(), reference.capture()
                );
                assertThat(targetEmail.getAllValues().get(0)).isEqualTo(CLAIMANT_EMAIL_ADDRESS);
                assertThat(emailTemplate.getAllValues().get(0)).isEqualTo(CARM_MAIL_TEMPLATE);
                assertThat(notificationDataMap.getAllValues().get(0)).isEqualTo(CARM_1V2_PROPERTY_MAP);
            }

            @ParameterizedTest
            @EnumSource(value = MediationUnsuccessfulReason.class, names = {"PARTY_WITHDRAWS",
                "APPOINTMENT_NO_AGREEMENT", "APPOINTMENT_NOT_ASSIGNED", "NOT_CONTACTABLE_DEFENDANT_ONE",
                "NOT_CONTACTABLE_DEFENDANT_TWO"})
            void shouldSendNotificationToClaimantLIP_1v1_whenEventIsCalled(MediationUnsuccessfulReason reason) {
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

                //When
                notificationHandler.handle(params);
                //Then
                verify(notificationService, times(1)).sendMail(targetEmail.capture(),
                                                               emailTemplate.capture(),
                                                               notificationDataMap.capture(), reference.capture()
                );
                assertThat(targetEmail.getAllValues().get(0)).isEqualTo(APPLICANT_PARTY_EMAIL);
                assertThat(emailTemplate.getAllValues().get(0)).isEqualTo(CARM_LIP_MAIL_TEMPLATE);
                assertThat(notificationDataMap.getAllValues().get(0)).isEqualTo(CARM_LIP_CLAIMANT_PROPERTY_MAP);
            }

            @ParameterizedTest
            @EnumSource(value = MediationUnsuccessfulReason.class, names = {"PARTY_WITHDRAWS",
                "APPOINTMENT_NO_AGREEMENT", "APPOINTMENT_NOT_ASSIGNED", "NOT_CONTACTABLE_DEFENDANT_ONE",
                "NOT_CONTACTABLE_DEFENDANT_TWO"})
            void shouldSendNotificationToClaimantLIP_1v1_WithBilingualwhenEventIsCalled(MediationUnsuccessfulReason reason) {
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

                //When
                notificationHandler.handle(params);
                //Then
                verify(notificationService, times(1)).sendMail(targetEmail.capture(),
                                                               emailTemplate.capture(),
                                                               notificationDataMap.capture(), reference.capture()
                );
                assertThat(targetEmail.getAllValues().get(0)).isEqualTo(APPLICANT_PARTY_EMAIL);
                assertThat(emailTemplate.getAllValues().get(0)).isEqualTo(CARM_LIP_MAIL_TEMPLATE);
                assertThat(notificationDataMap.getAllValues().get(0)).isEqualTo(CARM_LIP_CLAIMANT_PROPERTY_MAP);
            }

            @ParameterizedTest
            @EnumSource(value = MediationUnsuccessfulReason.class, names = {"NOT_CONTACTABLE_CLAIMANT_ONE", "NOT_CONTACTABLE_CLAIMANT_TWO"})
            void shouldSendNotificationToClaimantLR_NoAttendance_whenEventIsCalled(MediationUnsuccessfulReason reason) {
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

                //When
                notificationHandler.handle(params);
                //Then
                verify(notificationService, times(1)).sendMail(targetEmail.capture(),
                                                               emailTemplate.capture(),
                                                               notificationDataMap.capture(), reference.capture()
                );
                assertThat(targetEmail.getAllValues().get(0)).isEqualTo(CLAIMANT_EMAIL_ADDRESS);
                assertThat(emailTemplate.getAllValues().get(0)).isEqualTo(CARM_NO_ATTENDANCE_MAIL_TEMPLATE);
                assertThat(notificationDataMap.getAllValues().get(0)).isEqualTo(CARM_NO_ATTENDANCE_CLAIMANT_PROPERTY_MAP);
            }

            @Test
            void shouldSendNotificationToClaimantLR_NoAttendance_whenMoreThanOneReason() {
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

                //When
                notificationHandler.handle(params);
                //Then
                verify(notificationService, times(1)).sendMail(targetEmail.capture(),
                                                               emailTemplate.capture(),
                                                               notificationDataMap.capture(), reference.capture()
                );
                assertThat(targetEmail.getAllValues().get(0)).isEqualTo(CLAIMANT_EMAIL_ADDRESS);
                assertThat(emailTemplate.getAllValues().get(0)).isEqualTo(CARM_NO_ATTENDANCE_MAIL_TEMPLATE);
                assertThat(notificationDataMap.getAllValues().get(0)).isEqualTo(CARM_NO_ATTENDANCE_CLAIMANT_PROPERTY_MAP);
            }

            @ParameterizedTest
            @EnumSource(value = MediationUnsuccessfulReason.class, names = {"NOT_CONTACTABLE_CLAIMANT_TWO"})
            void shouldSendNotificationToClaimantLR_2v1_NoAttendance_whenEventIsCalled(MediationUnsuccessfulReason reason) {
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

                //When
                notificationHandler.handle(params);
                //Then
                verify(notificationService, times(1)).sendMail(targetEmail.capture(),
                                                               emailTemplate.capture(),
                                                               notificationDataMap.capture(), reference.capture()
                );
                assertThat(targetEmail.getAllValues().get(0)).isEqualTo(CLAIMANT_EMAIL_ADDRESS);
                assertThat(emailTemplate.getAllValues().get(0)).isEqualTo(CARM_NO_ATTENDANCE_MAIL_TEMPLATE);
                assertThat(notificationDataMap.getAllValues().get(0)).isEqualTo(CARM_NO_ATTENDANCE_CLAIMANT_PROPERTY_MAP);
            }

            @Test
            void shouldSendNotificationToClaimantLR_2v1_NoAttendance_whenMoreThanOneReason() {
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

                //When
                notificationHandler.handle(params);
                //Then
                verify(notificationService, times(1)).sendMail(targetEmail.capture(),
                                                               emailTemplate.capture(),
                                                               notificationDataMap.capture(), reference.capture()
                );
                assertThat(targetEmail.getAllValues().get(0)).isEqualTo(CLAIMANT_EMAIL_ADDRESS);
                assertThat(emailTemplate.getAllValues().get(0)).isEqualTo(CARM_NO_ATTENDANCE_MAIL_TEMPLATE);
                assertThat(notificationDataMap.getAllValues().get(0)).isEqualTo(CARM_NO_ATTENDANCE_CLAIMANT_PROPERTY_MAP);
            }
        }
    }
}
