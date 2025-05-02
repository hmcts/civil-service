package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
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
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_MEDIATION_UNSUCCESSFUL_DEFENDANT_1_LR;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_MEDIATION_UNSUCCESSFUL_DEFENDANT_2_LR;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulReason.NOT_CONTACTABLE_CLAIMANT_ONE;
import static uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulReason.NOT_CONTACTABLE_DEFENDANT_ONE;
import static uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulReason.NOT_CONTACTABLE_DEFENDANT_TWO;
import static uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulReason.PARTY_WITHDRAWS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASEMAN_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HMCTS_SIGNATURE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.OPENING_HOURS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PHONE_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.SPEC_UNSPEC_CONTACT;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;

@ExtendWith(MockitoExtension.class)
class NotificationMediationUnsuccessfulDefendantLRHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private NotificationService notificationService;
    @Mock
    NotificationsProperties notificationsProperties;
    @Mock
    OrganisationDetailsService organisationDetailsService;
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
    private NotificationMediationUnsuccessfulDefendantLRHandler notificationHandler;

    private static final String ORGANISATION_NAME_1 = "Org Name 1";
    private static final String ORGANISATION_NAME_2 = "Org Name 2";
    private static final String EMAIL_TEMPLATE = "test-notification-id";
    private static final String EMAIL_NO_ATTENDANCE_TEMPLATE = "test-na-notification-id";

    private static final String EMAIL_TEMPLATE_LIP_V_LR = "test-notification-id-lip-v-lr";
    private static final String APPLICANT_PARTY_NAME = "Alice";
    private static final String APPLICANT_2_PARTY_NAME = "Portia";
    private static final String DEFENDANT_1_PARTY_NAME = "Lycia";
    private static final String DEFENDANT_2_PARTY_NAME = "Alicia";
    private static final Long CCD_REFERENCE_NUMBER = 123456789L;
    private static final String CLAIMANT_EMAIL_ADDRESS = "applicantemail@hmcts.net";
    private static final String DEFENDANT_1_EMAIL_ADDRESS = "defendant1email@hmcts.net";
    private static final String DEFENDANT_2_EMAIL_ADDRESS = "defendant2email@hmcts.net";
    private static final String DEFENDANTS_TEXT = "'s claim against you";

    @ParameterizedTest
    @EnumSource(value = MediationUnsuccessfulReason.class, names = {"PARTY_WITHDRAWS", "APPOINTMENT_NO_AGREEMENT",
        "APPOINTMENT_NOT_ASSIGNED", "NOT_CONTACTABLE_CLAIMANT_ONE", "NOT_CONTACTABLE_CLAIMANT_TWO"})
    void shouldSendNotificationToDefendant1LR(MediationUnsuccessfulReason reason) {
        CaseData caseData = CaseData.builder()
            .applicant1(Party.builder().type(Party.Type.COMPANY).companyName(APPLICANT_PARTY_NAME).build())
            .applicantSolicitor1UserDetails(IdamUserDetails.builder().email(CLAIMANT_EMAIL_ADDRESS).build())
            .respondentSolicitor1EmailAddress(DEFENDANT_1_EMAIL_ADDRESS)
            .ccdCaseReference(CCD_REFERENCE_NUMBER)
            .legacyCaseReference("123456")
            .addApplicant2(YesOrNo.NO)
            .addRespondent2(YesOrNo.NO)
            .mediation(Mediation.builder()
                           .mediationUnsuccessfulReasonsMultiSelect(List.of(reason)).build())
            .build();

        //When
        given(notificationsProperties.getMediationUnsuccessfulLRTemplate()).willReturn(EMAIL_TEMPLATE);
        given(organisationDetailsService.getRespondent1LegalOrganisationName(any())).willReturn(ORGANISATION_NAME_1);

        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);
        when(configuration.getHmctsSignature()).thenReturn("Online Civil Claims \n HM Courts & Tribunal Service");
        when(configuration.getPhoneContact()).thenReturn("For anything related to hearings, call 0300 123 5577 "
                                                             + "\n For all other matters, call 0300 123 7050");
        when(configuration.getOpeningHours()).thenReturn("Monday to Friday, 8.30am to 5pm");
        when(configuration.getSpecUnspecContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk "
                                                                  + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");

        CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
            .request(CallbackRequest.builder().eventId(NOTIFY_MEDIATION_UNSUCCESSFUL_DEFENDANT_1_LR.name()).build()).build();

        notificationHandler.handle(params);
        //Then
        verify(notificationService, times(1)).sendMail(targetEmail.capture(),
                                                       emailTemplate.capture(),
                                                       notificationDataMap.capture(), reference.capture()
        );
        assertThat(targetEmail.getAllValues().get(0)).isEqualTo(DEFENDANT_1_EMAIL_ADDRESS);
        assertThat(emailTemplate.getAllValues().get(0)).isEqualTo(EMAIL_TEMPLATE);
        assertThat(notificationDataMap.getAllValues().get(0)).isEqualTo(getCarmD1PropertyMap(caseData));

    }

    @ParameterizedTest
    @EnumSource(value = MediationUnsuccessfulReason.class, names = {"PARTY_WITHDRAWS", "APPOINTMENT_NO_AGREEMENT",
        "APPOINTMENT_NOT_ASSIGNED", "NOT_CONTACTABLE_CLAIMANT_ONE", "NOT_CONTACTABLE_CLAIMANT_TWO"})
    void shouldSendNotificationToDefendant2LR(MediationUnsuccessfulReason reason) {
        CaseData caseData = CaseData.builder()
            .applicant1(Party.builder().type(Party.Type.COMPANY).companyName(APPLICANT_PARTY_NAME).build())
            .applicantSolicitor1UserDetails(IdamUserDetails.builder().email(CLAIMANT_EMAIL_ADDRESS).build())
            .respondentSolicitor2EmailAddress(DEFENDANT_2_EMAIL_ADDRESS)
            .ccdCaseReference(CCD_REFERENCE_NUMBER)
            .legacyCaseReference("123456")
            .addApplicant2(YesOrNo.NO)
            .addRespondent2(YesOrNo.NO)
            .mediation(Mediation.builder()
                           .mediationUnsuccessfulReasonsMultiSelect(List.of(reason)).build())
            .build();
        given(notificationsProperties.getMediationUnsuccessfulLRTemplate()).willReturn(EMAIL_TEMPLATE);
        given(organisationDetailsService.getRespondent2LegalOrganisationName(any())).willReturn(ORGANISATION_NAME_2);

        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);
        when(configuration.getHmctsSignature()).thenReturn("Online Civil Claims \n HM Courts & Tribunal Service");
        when(configuration.getPhoneContact()).thenReturn("For anything related to hearings, call 0300 123 5577 "
                                                             + "\n For all other matters, call 0300 123 7050");
        when(configuration.getOpeningHours()).thenReturn("Monday to Friday, 8.30am to 5pm");
        when(configuration.getSpecUnspecContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk "
                                                                  + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");
        CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
            .request(CallbackRequest.builder().eventId(NOTIFY_MEDIATION_UNSUCCESSFUL_DEFENDANT_2_LR.name()).build()).build();

        //When
        notificationHandler.handle(params);
        //Then
        verify(notificationService, times(1)).sendMail(targetEmail.capture(),
                                                       emailTemplate.capture(),
                                                       notificationDataMap.capture(), reference.capture()
        );
        assertThat(targetEmail.getAllValues().get(0)).isEqualTo(DEFENDANT_2_EMAIL_ADDRESS);
        assertThat(emailTemplate.getAllValues().get(0)).isEqualTo(EMAIL_TEMPLATE);
        assertThat(notificationDataMap.getAllValues().get(0)).isEqualTo(getCarmD2PropertyMap(caseData));
    }

    @ParameterizedTest
    @EnumSource(value = MediationUnsuccessfulReason.class, names = {"PARTY_WITHDRAWS", "APPOINTMENT_NO_AGREEMENT",
        "APPOINTMENT_NOT_ASSIGNED", "NOT_CONTACTABLE_CLAIMANT_ONE", "NOT_CONTACTABLE_CLAIMANT_TWO"})
    void shouldSendNotificationToDefendantLR_2v1(MediationUnsuccessfulReason reason) {
        CaseData caseData = CaseData.builder()
            .applicant1(Party.builder().type(Party.Type.COMPANY).companyName(APPLICANT_PARTY_NAME).build())
            .applicant2(Party.builder().type(Party.Type.COMPANY).companyName(APPLICANT_2_PARTY_NAME).build())
            .applicantSolicitor1UserDetails(IdamUserDetails.builder().email(CLAIMANT_EMAIL_ADDRESS).build())
            .respondentSolicitor1EmailAddress(DEFENDANT_1_EMAIL_ADDRESS)
            .ccdCaseReference(CCD_REFERENCE_NUMBER)
            .legacyCaseReference("123456")
            .addApplicant2(YesOrNo.YES)
            .addRespondent2(YesOrNo.NO)
            .mediation(Mediation.builder()
                           .mediationUnsuccessfulReasonsMultiSelect(List.of(reason)).build())
            .build();
        given(notificationsProperties.getMediationUnsuccessfulLRTemplate()).willReturn(EMAIL_TEMPLATE);
        given(organisationDetailsService.getRespondent1LegalOrganisationName(any())).willReturn(ORGANISATION_NAME_1);

        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);
        when(configuration.getHmctsSignature()).thenReturn("Online Civil Claims \n HM Courts & Tribunal Service");
        when(configuration.getPhoneContact()).thenReturn("For anything related to hearings, call 0300 123 5577 "
                                                             + "\n For all other matters, call 0300 123 7050");
        when(configuration.getOpeningHours()).thenReturn("Monday to Friday, 8.30am to 5pm");
        when(configuration.getSpecUnspecContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk "
                                                                  + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");
        CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
            .request(CallbackRequest.builder().eventId(NOTIFY_MEDIATION_UNSUCCESSFUL_DEFENDANT_1_LR.name()).build()).build();

        //When
        notificationHandler.handle(params);
        //Then
        verify(notificationService, times(1)).sendMail(targetEmail.capture(),
                                                       emailTemplate.capture(),
                                                       notificationDataMap.capture(), reference.capture()
        );
        assertThat(targetEmail.getAllValues().get(0)).isEqualTo(DEFENDANT_1_EMAIL_ADDRESS);
        assertThat(emailTemplate.getAllValues().get(0)).isEqualTo(EMAIL_TEMPLATE);
        assertThat(notificationDataMap.getAllValues().get(0)).isEqualTo(getCarm2v1PropertyMap(caseData));
    }

    @ParameterizedTest
    @EnumSource(value = MediationUnsuccessfulReason.class, names = {"NOT_CONTACTABLE_DEFENDANT_ONE"})
    void shouldSendNotificationToDefendant1LRNoAttendance(MediationUnsuccessfulReason reason) {
        CaseData caseData = CaseData.builder()
            .applicant1(Party.builder().type(Party.Type.COMPANY).companyName(APPLICANT_PARTY_NAME).build())
            .applicantSolicitor1UserDetails(IdamUserDetails.builder().email(CLAIMANT_EMAIL_ADDRESS).build())
            .respondent1(Party.builder().type(Party.Type.INDIVIDUAL).individualFirstName(DEFENDANT_1_PARTY_NAME).individualLastName(
                "Lea").build())
            .respondentSolicitor1EmailAddress(DEFENDANT_1_EMAIL_ADDRESS)
            .ccdCaseReference(CCD_REFERENCE_NUMBER)
            .legacyCaseReference("123456")
            .addApplicant2(YesOrNo.NO)
            .addRespondent2(YesOrNo.NO)
            .mediation(Mediation.builder()
                           .mediationUnsuccessfulReasonsMultiSelect(List.of(reason)).build())
            .build();

        //When
        given(notificationsProperties.getMediationUnsuccessfulNoAttendanceLRTemplate()).willReturn(
            EMAIL_NO_ATTENDANCE_TEMPLATE);
        given(organisationDetailsService.getRespondent1LegalOrganisationName(any())).willReturn(ORGANISATION_NAME_1);

        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);
        when(configuration.getHmctsSignature()).thenReturn("Online Civil Claims \n HM Courts & Tribunal Service");
        when(configuration.getPhoneContact()).thenReturn("For anything related to hearings, call 0300 123 5577 "
                                                             + "\n For all other matters, call 0300 123 7050");
        when(configuration.getOpeningHours()).thenReturn("Monday to Friday, 8.30am to 5pm");
        when(configuration.getSpecUnspecContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk "
                                                                  + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");
        CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
            .request(CallbackRequest.builder().eventId(NOTIFY_MEDIATION_UNSUCCESSFUL_DEFENDANT_1_LR.name()).build()).build();

        notificationHandler.handle(params);
        //Then
        verify(notificationService, times(1)).sendMail(targetEmail.capture(),
                                                       emailTemplate.capture(),
                                                       notificationDataMap.capture(), reference.capture()
        );
        assertThat(targetEmail.getAllValues().get(0)).isEqualTo(DEFENDANT_1_EMAIL_ADDRESS);
        assertThat(emailTemplate.getAllValues().get(0)).isEqualTo(EMAIL_NO_ATTENDANCE_TEMPLATE);
        assertThat(notificationDataMap.getAllValues().get(0)).isEqualTo(getCarmD1NoAttendancePropertyMap(caseData));
    }

    @ParameterizedTest
    @EnumSource(value = MediationUnsuccessfulReason.class, names = {"NOT_CONTACTABLE_DEFENDANT_TWO"})
    void shouldSendNotificationToDefendant2LRNoAttendance(MediationUnsuccessfulReason reason) {
        CaseData caseData = CaseData.builder()
            .applicant1(Party.builder().type(Party.Type.COMPANY).companyName(APPLICANT_PARTY_NAME).build())
            .applicantSolicitor1UserDetails(IdamUserDetails.builder().email(CLAIMANT_EMAIL_ADDRESS).build())
            .respondent2(Party.builder().type(Party.Type.INDIVIDUAL).individualFirstName(DEFENDANT_2_PARTY_NAME).individualLastName(
                "Lea").build())
            .respondentSolicitor2EmailAddress(DEFENDANT_2_EMAIL_ADDRESS)
            .ccdCaseReference(CCD_REFERENCE_NUMBER)
            .legacyCaseReference("123456")
            .addApplicant2(YesOrNo.NO)
            .addRespondent2(YesOrNo.YES)
            .mediation(Mediation.builder()
                           .mediationUnsuccessfulReasonsMultiSelect(List.of(reason)).build())
            .build();

        //When
        given(notificationsProperties.getMediationUnsuccessfulNoAttendanceLRTemplate()).willReturn(
            EMAIL_NO_ATTENDANCE_TEMPLATE);
        given(organisationDetailsService.getRespondent2LegalOrganisationName(any())).willReturn(ORGANISATION_NAME_2);

        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);
        when(configuration.getHmctsSignature()).thenReturn("Online Civil Claims \n HM Courts & Tribunal Service");
        when(configuration.getPhoneContact()).thenReturn("For anything related to hearings, call 0300 123 5577 "
                                                             + "\n For all other matters, call 0300 123 7050");
        when(configuration.getOpeningHours()).thenReturn("Monday to Friday, 8.30am to 5pm");
        when(configuration.getSpecUnspecContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk "
                                                                  + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");
        CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
            .request(CallbackRequest.builder().eventId(NOTIFY_MEDIATION_UNSUCCESSFUL_DEFENDANT_2_LR.name()).build()).build();

        notificationHandler.handle(params);
        //Then
        verify(notificationService, times(1)).sendMail(targetEmail.capture(),
                                                       emailTemplate.capture(),
                                                       notificationDataMap.capture(), reference.capture()
        );
        assertThat(targetEmail.getAllValues().get(0)).isEqualTo(DEFENDANT_2_EMAIL_ADDRESS);
        assertThat(emailTemplate.getAllValues().get(0)).isEqualTo(EMAIL_NO_ATTENDANCE_TEMPLATE);
        assertThat(notificationDataMap.getAllValues().get(0)).isEqualTo(getCarmD2NoAttendancePropertyMap(caseData));
    }

    @Test
    void shouldSendNotificationToDefendant1LRNoAttendance_whenMoreThan1Reason() {
        CaseData caseData = CaseData.builder()
            .applicant1(Party.builder().type(Party.Type.COMPANY).companyName(APPLICANT_PARTY_NAME).build())
            .applicantSolicitor1UserDetails(IdamUserDetails.builder().email(CLAIMANT_EMAIL_ADDRESS).build())
            .respondentSolicitor1EmailAddress(DEFENDANT_1_EMAIL_ADDRESS)
            .ccdCaseReference(CCD_REFERENCE_NUMBER)
            .legacyCaseReference("123456")
            .addApplicant2(YesOrNo.NO)
            .addRespondent2(YesOrNo.NO)
            .mediation(Mediation.builder()
                           .mediationUnsuccessfulReasonsMultiSelect(List.of(
                               NOT_CONTACTABLE_CLAIMANT_ONE,
                               NOT_CONTACTABLE_DEFENDANT_ONE
                           )).build())
            .build();

        //When
        given(notificationsProperties.getMediationUnsuccessfulNoAttendanceLRTemplate()).willReturn(
            EMAIL_NO_ATTENDANCE_TEMPLATE);
        given(organisationDetailsService.getRespondent1LegalOrganisationName(any())).willReturn(ORGANISATION_NAME_1);

        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);
        when(configuration.getHmctsSignature()).thenReturn("Online Civil Claims \n HM Courts & Tribunal Service");
        when(configuration.getPhoneContact()).thenReturn("For anything related to hearings, call 0300 123 5577 "
                                                             + "\n For all other matters, call 0300 123 7050");
        when(configuration.getOpeningHours()).thenReturn("Monday to Friday, 8.30am to 5pm");
        when(configuration.getSpecUnspecContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk "
                                                                  + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");
        CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
            .request(CallbackRequest.builder().eventId(NOTIFY_MEDIATION_UNSUCCESSFUL_DEFENDANT_1_LR.name()).build()).build();

        notificationHandler.handle(params);
        //Then
        verify(notificationService, times(1)).sendMail(targetEmail.capture(),
                                                       emailTemplate.capture(),
                                                       notificationDataMap.capture(), reference.capture()
        );
        assertThat(targetEmail.getAllValues().get(0)).isEqualTo(DEFENDANT_1_EMAIL_ADDRESS);
        assertThat(emailTemplate.getAllValues().get(0)).isEqualTo(EMAIL_NO_ATTENDANCE_TEMPLATE);
        assertThat(notificationDataMap.getAllValues().get(0)).isEqualTo(getCarmD1NoAttendancePropertyMap(caseData));
    }

    @Test
    void shouldSendNotificationToDefendant2LRNoAttendance_whenMoreThan1Reason() {
        CaseData caseData = CaseData.builder()
            .applicant1(Party.builder().type(Party.Type.COMPANY).companyName(APPLICANT_PARTY_NAME).build())
            .applicantSolicitor1UserDetails(IdamUserDetails.builder().email(CLAIMANT_EMAIL_ADDRESS).build())
            .respondent2(Party.builder().type(Party.Type.INDIVIDUAL).individualFirstName(DEFENDANT_2_PARTY_NAME).individualLastName(
                "Lea").build())
            .respondentSolicitor2EmailAddress(DEFENDANT_2_EMAIL_ADDRESS)
            .ccdCaseReference(CCD_REFERENCE_NUMBER)
            .legacyCaseReference("123456")
            .addApplicant2(YesOrNo.NO)
            .addRespondent2(YesOrNo.YES)
            .mediation(Mediation.builder()
                           .mediationUnsuccessfulReasonsMultiSelect(List.of(
                               PARTY_WITHDRAWS,
                               NOT_CONTACTABLE_DEFENDANT_ONE,
                               NOT_CONTACTABLE_DEFENDANT_TWO
                           )).build())
            .build();

        //When
        given(notificationsProperties.getMediationUnsuccessfulNoAttendanceLRTemplate()).willReturn(
            EMAIL_NO_ATTENDANCE_TEMPLATE);
        given(organisationDetailsService.getRespondent2LegalOrganisationName(any())).willReturn(ORGANISATION_NAME_2);

        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);
        when(configuration.getHmctsSignature()).thenReturn("Online Civil Claims \n HM Courts & Tribunal Service");
        when(configuration.getPhoneContact()).thenReturn("For anything related to hearings, call 0300 123 5577 "
                                                             + "\n For all other matters, call 0300 123 7050");
        when(configuration.getOpeningHours()).thenReturn("Monday to Friday, 8.30am to 5pm");
        when(configuration.getSpecUnspecContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk "
                                                                  + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");
        CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
            .request(CallbackRequest.builder().eventId(NOTIFY_MEDIATION_UNSUCCESSFUL_DEFENDANT_2_LR.name()).build()).build();

        notificationHandler.handle(params);
        //Then
        verify(notificationService, times(1)).sendMail(targetEmail.capture(),
                                                       emailTemplate.capture(),
                                                       notificationDataMap.capture(), reference.capture()
        );
        assertThat(targetEmail.getAllValues().get(0)).isEqualTo(DEFENDANT_2_EMAIL_ADDRESS);
        assertThat(emailTemplate.getAllValues().get(0)).isEqualTo(EMAIL_NO_ATTENDANCE_TEMPLATE);
        assertThat(notificationDataMap.getAllValues().get(0)).isEqualTo(getCarmD2NoAttendancePropertyMap(caseData));
    }

    @Test
    void shouldSendNotificationToDefendant1LRforLiPvLrCase() {
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(false);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        CaseData caseData = CaseData.builder()
            .applicant1(Party.builder().type(Party.Type.COMPANY).companyName(APPLICANT_PARTY_NAME).build())
            .applicant1Represented(NO)
            .specRespondent1Represented(YES)
            .respondentSolicitor1EmailAddress(DEFENDANT_1_EMAIL_ADDRESS)
            .ccdCaseReference(CCD_REFERENCE_NUMBER)
            .legacyCaseReference("123456")
            .addApplicant2(YesOrNo.NO)
            .addRespondent2(YesOrNo.NO)
            .build();

        //When
        given(organisationDetailsService.getRespondent1LegalOrganisationName(any())).willReturn(ORGANISATION_NAME_1);
        given(notificationsProperties.getMediationUnsuccessfulLRTemplateForLipVLr()).willReturn(EMAIL_TEMPLATE_LIP_V_LR);
        when(configuration.getHmctsSignature()).thenReturn("Online Civil Claims \n HM Courts & Tribunal Service");
        when(configuration.getPhoneContact()).thenReturn("For anything related to hearings, call 0300 123 5577 "
                                                             + "\n For all other matters, call 0300 123 7050");
        when(configuration.getOpeningHours()).thenReturn("Monday to Friday, 8.30am to 5pm");
        when(configuration.getSpecUnspecContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk "
                                                                  + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");
        CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
            .request(CallbackRequest.builder().eventId(NOTIFY_MEDIATION_UNSUCCESSFUL_DEFENDANT_1_LR.name()).build()).build();

        notificationHandler.handle(params);
        //Then
        verify(notificationService, times(1)).sendMail(targetEmail.capture(),
                                                       emailTemplate.capture(),
                                                       notificationDataMap.capture(), reference.capture()
        );
        assertThat(targetEmail.getAllValues().get(0)).isEqualTo(DEFENDANT_1_EMAIL_ADDRESS);
        assertThat(emailTemplate.getAllValues().get(0)).isEqualTo(EMAIL_TEMPLATE_LIP_V_LR);
    }

    @Test
    void shouldNotSendNotificationToDefendant1LRforLiPvLrCase_LipVLipIsNotSet() {
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(false);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(false);
        CaseData caseData = CaseData.builder()
            .applicant1(Party.builder().type(Party.Type.COMPANY).companyName(APPLICANT_PARTY_NAME).build())
            .applicant1Represented(NO)
            .specRespondent1Represented(YES)
            .respondentSolicitor1EmailAddress(DEFENDANT_1_EMAIL_ADDRESS)
            .ccdCaseReference(CCD_REFERENCE_NUMBER)
            .legacyCaseReference("123456")
            .addApplicant2(YesOrNo.NO)
            .addRespondent2(YesOrNo.NO)
            .build();
        CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
            .request(CallbackRequest.builder().eventId(NOTIFY_MEDIATION_UNSUCCESSFUL_DEFENDANT_1_LR.name()).build()).build();

        //When
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) notificationHandler.handle(
            params);
        //Then
        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldNotSendNotificationToDefendant1LRforLiPvLrCase_applicantRepresented() {
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(false);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        CaseData caseData = CaseData.builder()
            .applicant1(Party.builder().type(Party.Type.COMPANY).companyName(APPLICANT_PARTY_NAME).build())
            .applicantSolicitor1UserDetails(IdamUserDetails.builder().email(CLAIMANT_EMAIL_ADDRESS).build())
            .applicant1Represented(YES)
            .specRespondent1Represented(YES)
            .respondentSolicitor1EmailAddress(DEFENDANT_1_EMAIL_ADDRESS)
            .ccdCaseReference(CCD_REFERENCE_NUMBER)
            .legacyCaseReference("123456")
            .addApplicant2(YesOrNo.NO)
            .addRespondent2(YesOrNo.NO)
            .build();
        CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
            .request(CallbackRequest.builder().eventId(NOTIFY_MEDIATION_UNSUCCESSFUL_DEFENDANT_1_LR.name()).build()).build();

        //When
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) notificationHandler.handle(
            params);
        //Then
        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldNotSendNotificationToDefendant1LRforLiPvLrCase_RespondentSolicitorNotSet() {
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(false);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        CaseData caseData = CaseData.builder()
            .applicant1(Party.builder().type(Party.Type.COMPANY).companyName(APPLICANT_PARTY_NAME).build())
            .applicant1Represented(NO)
            .specRespondent1Represented(NO)
            .respondentSolicitor1EmailAddress(DEFENDANT_1_EMAIL_ADDRESS)
            .ccdCaseReference(CCD_REFERENCE_NUMBER)
            .legacyCaseReference("123456")
            .addApplicant2(YesOrNo.NO)
            .addRespondent2(YesOrNo.NO)
            .build();
        CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
            .request(CallbackRequest.builder().eventId(NOTIFY_MEDIATION_UNSUCCESSFUL_DEFENDANT_1_LR.name()).build()).build();

        //When
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) notificationHandler.handle(
            params);
        //Then
        assertThat(response.getErrors()).isNull();
    }

    @NotNull
    private Map<String, String> getCarmD1PropertyMap(CaseData caseData) {
        return Map.of(
            CLAIM_LEGAL_ORG_NAME_SPEC, ORGANISATION_NAME_1,
            PARTY_NAME, APPLICANT_PARTY_NAME + DEFENDANTS_TEXT,
            CLAIM_REFERENCE_NUMBER, CCD_REFERENCE_NUMBER.toString(),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference(),
            PHONE_CONTACT, "For anything related to hearings, call 0300 123 5577 \n For all other matters, call 0300 123 7050",
            OPENING_HOURS, "Monday to Friday, 8.30am to 5pm",
            SPEC_UNSPEC_CONTACT, "Email for Specified Claims: contactocmc@justice.gov.uk \n Email for Damages Claims: damagesclaims@justice.gov.uk",
            HMCTS_SIGNATURE, "Online Civil Claims \n HM Courts & Tribunal Service"
        );
    }

    @NotNull
    private Map<String, String> getCarmD2PropertyMap(CaseData caseData) {
        return Map.of(
            CLAIM_LEGAL_ORG_NAME_SPEC, ORGANISATION_NAME_2,
            PARTY_NAME, APPLICANT_PARTY_NAME + DEFENDANTS_TEXT,
            CLAIM_REFERENCE_NUMBER, CCD_REFERENCE_NUMBER.toString(),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference(),
            PHONE_CONTACT, "For anything related to hearings, call 0300 123 5577 \n For all other matters, call 0300 123 7050",
            OPENING_HOURS, "Monday to Friday, 8.30am to 5pm",
            SPEC_UNSPEC_CONTACT, "Email for Specified Claims: contactocmc@justice.gov.uk \n Email for Damages Claims: damagesclaims@justice.gov.uk",
            HMCTS_SIGNATURE, "Online Civil Claims \n HM Courts & Tribunal Service"
        );
    }

    @NotNull
    private Map<String, String> getCarm2v1PropertyMap(CaseData caseData) {
        return Map.of(
            CLAIM_LEGAL_ORG_NAME_SPEC, ORGANISATION_NAME_1,
            PARTY_NAME, APPLICANT_PARTY_NAME + " and " + APPLICANT_2_PARTY_NAME + DEFENDANTS_TEXT,
            CLAIM_REFERENCE_NUMBER, CCD_REFERENCE_NUMBER.toString(),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference(),
            PHONE_CONTACT, "For anything related to hearings, call 0300 123 5577 \n For all other matters, call 0300 123 7050",
            OPENING_HOURS, "Monday to Friday, 8.30am to 5pm",
            SPEC_UNSPEC_CONTACT, "Email for Specified Claims: contactocmc@justice.gov.uk \n Email for Damages Claims: damagesclaims@justice.gov.uk",
            HMCTS_SIGNATURE, "Online Civil Claims \n HM Courts & Tribunal Service"
        );
    }

    @NotNull
    private Map<String, String> getCarmD1NoAttendancePropertyMap(CaseData caseData) {
        return Map.of(
            CLAIM_LEGAL_ORG_NAME_SPEC, ORGANISATION_NAME_1,
            CLAIM_REFERENCE_NUMBER, CCD_REFERENCE_NUMBER.toString(),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference(),
            PHONE_CONTACT, "For anything related to hearings, call 0300 123 5577 \n For all other matters, call 0300 123 7050",
            OPENING_HOURS, "Monday to Friday, 8.30am to 5pm",
            SPEC_UNSPEC_CONTACT, "Email for Specified Claims: contactocmc@justice.gov.uk \n Email for Damages Claims: damagesclaims@justice.gov.uk",
            HMCTS_SIGNATURE, "Online Civil Claims \n HM Courts & Tribunal Service"
        );
    }

    @NotNull
    private Map<String, String> getCarmD2NoAttendancePropertyMap(CaseData caseData) {
        return Map.of(
            CLAIM_LEGAL_ORG_NAME_SPEC, ORGANISATION_NAME_2,
            CLAIM_REFERENCE_NUMBER, CCD_REFERENCE_NUMBER.toString(),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference(),
            PHONE_CONTACT, "For anything related to hearings, call 0300 123 5577 \n For all other matters, call 0300 123 7050",
            OPENING_HOURS, "Monday to Friday, 8.30am to 5pm",
            SPEC_UNSPEC_CONTACT, "Email for Specified Claims: contactocmc@justice.gov.uk \n Email for Damages Claims: damagesclaims@justice.gov.uk",
            HMCTS_SIGNATURE, "Online Civil Claims \n HM Courts & Tribunal Service"
        );
    }

}
