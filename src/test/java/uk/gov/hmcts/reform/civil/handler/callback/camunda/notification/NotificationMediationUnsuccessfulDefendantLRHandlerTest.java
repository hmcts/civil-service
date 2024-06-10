package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_NAME;

@SpringBootTest(classes = {
    NotificationMediationUnsuccessfulDefendantLRHandler.class,
    OrganisationDetailsService.class,
    JacksonAutoConfiguration.class,
})
class NotificationMediationUnsuccessfulDefendantLRHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private NotificationService notificationService;
    @MockBean
    NotificationsProperties notificationsProperties;
    @MockBean
    OrganisationDetailsService organisationDetailsService;
    @MockBean
    private FeatureToggleService featureToggleService;
    @Captor
    private ArgumentCaptor<String> targetEmail;
    @Captor
    private ArgumentCaptor<String> emailTemplate;
    @Captor
    private ArgumentCaptor<Map<String, String>> notificationDataMap;
    @Captor
    private ArgumentCaptor<String> reference;

    @Autowired
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
    private static final Map<String, String> CARM_D1_PROPERTY_MAP = Map.of(CLAIM_LEGAL_ORG_NAME_SPEC, ORGANISATION_NAME_1,
                                                                        PARTY_NAME, APPLICANT_PARTY_NAME + DEFENDANTS_TEXT,
                                                                        CLAIM_REFERENCE_NUMBER, CCD_REFERENCE_NUMBER.toString());
    private static final Map<String, String> CARM_D2_PROPERTY_MAP = Map.of(CLAIM_LEGAL_ORG_NAME_SPEC, ORGANISATION_NAME_2,
                                                                        PARTY_NAME, APPLICANT_PARTY_NAME + DEFENDANTS_TEXT,
                                                                        CLAIM_REFERENCE_NUMBER, CCD_REFERENCE_NUMBER.toString());
    private static final Map<String, String> CARM_2V1_PROPERTY_MAP = Map.of(CLAIM_LEGAL_ORG_NAME_SPEC, ORGANISATION_NAME_1,
                                                                        PARTY_NAME, APPLICANT_PARTY_NAME + " and " + APPLICANT_2_PARTY_NAME + DEFENDANTS_TEXT,
                                                                        CLAIM_REFERENCE_NUMBER, CCD_REFERENCE_NUMBER.toString());
    private static final Map<String, String> CARM_D1_NO_ATTENDANCE_PROPERTY_MAP = Map.of(CLAIM_LEGAL_ORG_NAME_SPEC, ORGANISATION_NAME_1,
                                                                        CLAIM_REFERENCE_NUMBER, CCD_REFERENCE_NUMBER.toString());
    private static final Map<String, String> CARM_D2_NO_ATTENDANCE_PROPERTY_MAP = Map.of(CLAIM_LEGAL_ORG_NAME_SPEC, ORGANISATION_NAME_2,
                                                                        CLAIM_REFERENCE_NUMBER, CCD_REFERENCE_NUMBER.toString());

    @BeforeEach
    void setUp() {
        given(notificationsProperties.getMediationUnsuccessfulLRTemplate()).willReturn(EMAIL_TEMPLATE);
        given(notificationsProperties.getMediationUnsuccessfulNoAttendanceLRTemplate()).willReturn(EMAIL_NO_ATTENDANCE_TEMPLATE);
        given(organisationDetailsService.getRespondent1LegalOrganisationName(any())).willReturn(ORGANISATION_NAME_1);
        given(organisationDetailsService.getRespondent2LegalOrganisationName(any())).willReturn(ORGANISATION_NAME_2);
        given(notificationsProperties.getMediationUnsuccessfulLRTemplateForLipVLr()).willReturn(EMAIL_TEMPLATE_LIP_V_LR);

        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);
    }

    @ParameterizedTest
    @EnumSource(value = MediationUnsuccessfulReason.class, names = {"PARTY_WITHDRAWS", "APPOINTMENT_NO_AGREEMENT",
        "APPOINTMENT_NOT_ASSIGNED", "NOT_CONTACTABLE_CLAIMANT_ONE", "NOT_CONTACTABLE_CLAIMANT_TWO"})
    void shouldSendNotificationToDefendant1LR(MediationUnsuccessfulReason reason) {
        CaseData caseData = CaseData.builder()
            .applicant1(Party.builder().type(Party.Type.COMPANY).companyName(APPLICANT_PARTY_NAME).build())
            .applicantSolicitor1UserDetails(IdamUserDetails.builder().email(CLAIMANT_EMAIL_ADDRESS).build())
            .respondentSolicitor1EmailAddress(DEFENDANT_1_EMAIL_ADDRESS)
            .ccdCaseReference(CCD_REFERENCE_NUMBER)
            .addApplicant2(YesOrNo.NO)
            .addRespondent2(YesOrNo.NO)
            .mediation(Mediation.builder()
                           .mediationUnsuccessfulReasonsMultiSelect(List.of(reason)).build())
            .build();
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
        assertThat(notificationDataMap.getAllValues().get(0)).isEqualTo(CARM_D1_PROPERTY_MAP);

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
            .addApplicant2(YesOrNo.NO)
            .addRespondent2(YesOrNo.NO)
            .mediation(Mediation.builder()
                           .mediationUnsuccessfulReasonsMultiSelect(List.of(reason)).build())
            .build();
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
        assertThat(notificationDataMap.getAllValues().get(0)).isEqualTo(CARM_D2_PROPERTY_MAP);
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
            .addApplicant2(YesOrNo.YES)
            .addRespondent2(YesOrNo.NO)
            .mediation(Mediation.builder()
                           .mediationUnsuccessfulReasonsMultiSelect(List.of(reason)).build())
            .build();
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
        assertThat(notificationDataMap.getAllValues().get(0)).isEqualTo(CARM_2V1_PROPERTY_MAP);
    }

    @ParameterizedTest
    @EnumSource(value = MediationUnsuccessfulReason.class, names = {"NOT_CONTACTABLE_DEFENDANT_ONE"})
    void shouldSendNotificationToDefendant1LRNoAttendance(MediationUnsuccessfulReason reason) {
        CaseData caseData = CaseData.builder()
            .applicant1(Party.builder().type(Party.Type.COMPANY).companyName(APPLICANT_PARTY_NAME).build())
            .applicantSolicitor1UserDetails(IdamUserDetails.builder().email(CLAIMANT_EMAIL_ADDRESS).build())
            .respondent1(Party.builder().type(Party.Type.INDIVIDUAL).individualFirstName(DEFENDANT_1_PARTY_NAME).individualLastName("Lea").build())
            .respondentSolicitor1EmailAddress(DEFENDANT_1_EMAIL_ADDRESS)
            .ccdCaseReference(CCD_REFERENCE_NUMBER)
            .addApplicant2(YesOrNo.NO)
            .addRespondent2(YesOrNo.NO)
            .mediation(Mediation.builder()
                           .mediationUnsuccessfulReasonsMultiSelect(List.of(reason)).build())
            .build();
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
        assertThat(emailTemplate.getAllValues().get(0)).isEqualTo(EMAIL_NO_ATTENDANCE_TEMPLATE);
        assertThat(notificationDataMap.getAllValues().get(0)).isEqualTo(CARM_D1_NO_ATTENDANCE_PROPERTY_MAP);
    }

    @ParameterizedTest
    @EnumSource(value = MediationUnsuccessfulReason.class, names = {"NOT_CONTACTABLE_DEFENDANT_TWO"})
    void shouldSendNotificationToDefendant2LRNoAttendance(MediationUnsuccessfulReason reason) {
        CaseData caseData = CaseData.builder()
            .applicant1(Party.builder().type(Party.Type.COMPANY).companyName(APPLICANT_PARTY_NAME).build())
            .applicantSolicitor1UserDetails(IdamUserDetails.builder().email(CLAIMANT_EMAIL_ADDRESS).build())
            .respondent2(Party.builder().type(Party.Type.INDIVIDUAL).individualFirstName(DEFENDANT_2_PARTY_NAME).individualLastName("Lea").build())
            .respondentSolicitor2EmailAddress(DEFENDANT_2_EMAIL_ADDRESS)
            .ccdCaseReference(CCD_REFERENCE_NUMBER)
            .addApplicant2(YesOrNo.NO)
            .addRespondent2(YesOrNo.YES)
            .mediation(Mediation.builder()
                           .mediationUnsuccessfulReasonsMultiSelect(List.of(reason)).build())
            .build();
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
        assertThat(emailTemplate.getAllValues().get(0)).isEqualTo(EMAIL_NO_ATTENDANCE_TEMPLATE);
        assertThat(notificationDataMap.getAllValues().get(0)).isEqualTo(CARM_D2_NO_ATTENDANCE_PROPERTY_MAP);
    }

    @Test
    void shouldSendNotificationToDefendant1LRNoAttendance_whenMoreThan1Reason() {
        CaseData caseData = CaseData.builder()
            .applicant1(Party.builder().type(Party.Type.COMPANY).companyName(APPLICANT_PARTY_NAME).build())
            .applicantSolicitor1UserDetails(IdamUserDetails.builder().email(CLAIMANT_EMAIL_ADDRESS).build())
            .respondentSolicitor1EmailAddress(DEFENDANT_1_EMAIL_ADDRESS)
            .ccdCaseReference(CCD_REFERENCE_NUMBER)
            .addApplicant2(YesOrNo.NO)
            .addRespondent2(YesOrNo.NO)
            .mediation(Mediation.builder()
                           .mediationUnsuccessfulReasonsMultiSelect(List.of(NOT_CONTACTABLE_CLAIMANT_ONE, NOT_CONTACTABLE_DEFENDANT_ONE)).build())
            .build();
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
        assertThat(emailTemplate.getAllValues().get(0)).isEqualTo(EMAIL_NO_ATTENDANCE_TEMPLATE);
        assertThat(notificationDataMap.getAllValues().get(0)).isEqualTo(CARM_D1_NO_ATTENDANCE_PROPERTY_MAP);
    }

    @Test
    void shouldSendNotificationToDefendant2LRNoAttendance_whenMoreThan1Reason() {
        CaseData caseData = CaseData.builder()
            .applicant1(Party.builder().type(Party.Type.COMPANY).companyName(APPLICANT_PARTY_NAME).build())
            .applicantSolicitor1UserDetails(IdamUserDetails.builder().email(CLAIMANT_EMAIL_ADDRESS).build())
            .respondent2(Party.builder().type(Party.Type.INDIVIDUAL).individualFirstName(DEFENDANT_2_PARTY_NAME).individualLastName("Lea").build())
            .respondentSolicitor2EmailAddress(DEFENDANT_2_EMAIL_ADDRESS)
            .ccdCaseReference(CCD_REFERENCE_NUMBER)
            .addApplicant2(YesOrNo.NO)
            .addRespondent2(YesOrNo.YES)
            .mediation(Mediation.builder()
                           .mediationUnsuccessfulReasonsMultiSelect(List.of(PARTY_WITHDRAWS, NOT_CONTACTABLE_DEFENDANT_ONE, NOT_CONTACTABLE_DEFENDANT_TWO)).build())
            .build();
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
        assertThat(emailTemplate.getAllValues().get(0)).isEqualTo(EMAIL_NO_ATTENDANCE_TEMPLATE);
        assertThat(notificationDataMap.getAllValues().get(0)).isEqualTo(CARM_D2_NO_ATTENDANCE_PROPERTY_MAP);
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
            .addApplicant2(YesOrNo.NO)
            .addRespondent2(YesOrNo.NO)
            .build();
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
            .addApplicant2(YesOrNo.NO)
            .addRespondent2(YesOrNo.NO)
            .build();
        CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
            .request(CallbackRequest.builder().eventId(NOTIFY_MEDIATION_UNSUCCESSFUL_DEFENDANT_1_LR.name()).build()).build();

        //When
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) notificationHandler.handle(params);
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
            .addApplicant2(YesOrNo.NO)
            .addRespondent2(YesOrNo.NO)
            .build();
        CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
            .request(CallbackRequest.builder().eventId(NOTIFY_MEDIATION_UNSUCCESSFUL_DEFENDANT_1_LR.name()).build()).build();

        //When
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) notificationHandler.handle(params);
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
            .addApplicant2(YesOrNo.NO)
            .addRespondent2(YesOrNo.NO)
            .build();
        CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
            .request(CallbackRequest.builder().eventId(NOTIFY_MEDIATION_UNSUCCESSFUL_DEFENDANT_1_LR.name()).build()).build();

        //When
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) notificationHandler.handle(params);
        //Then
        assertThat(response.getErrors()).isNull();
    }
}
