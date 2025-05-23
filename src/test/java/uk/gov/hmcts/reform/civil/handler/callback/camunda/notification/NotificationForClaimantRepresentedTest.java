package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ChangeOfRepresentation;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_APPLICANT_LIP_SOLICITOR;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_CLAIMANT_LIP_AFTER_NOC_APPROVAL;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DEFENDANT_LIP_CLAIMANT_REPRESENTED;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.APPLICANTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationForClaimantRepresented.TASK_ID_APPLICANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationForClaimantRepresented.TASK_ID_APPLICANT_SOLICITOR;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationForClaimantRepresented.TASK_ID_RESPONDENT;

@ExtendWith(MockitoExtension.class)
class NotificationForClaimantRepresentedTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private NotificationForClaimantRepresented notificationHandler;
    @Mock
    private NotificationService notificationService;
    @Mock
    NotificationsProperties notificationsProperties;
    @Captor
    private ArgumentCaptor<String> targetEmail;
    @Mock
    private OrganisationService organisationService;
    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private NotificationsSignatureConfiguration configuration;
    @Captor
    private ArgumentCaptor<String> emailTemplate;
    @Captor
    private ArgumentCaptor<Map<String, String>> notificationDataMap;
    @Captor
    private ArgumentCaptor<String> reference;

    @Nested
    class AboutToSubmitCallback {

        private static final String DEFENDANT_EMAIL_ADDRESS = "defendantmail@hmcts.net";
        private static final String APPLICANT_EMAIL_ADDRESS = "applicantmail@hmcts.net";
        private static final String DEFENDANT_PARTY_NAME = "ABC ABC";
        private static final String REFERENCE_NUMBER = "8372942374";
        private static final String EMAIL_TEMPLATE = "test-notification-id";
        private static final String EMAIL_WELSH_TEMPLATE = "test-notification-welsh-id";
        private static final String CLAIMANT_ORG_NAME = "Org Name";
        private static final String APPLICANT_SOLICITOR_TEMPLATE = "applicant1-solicitor-id";
        private static final String OTHER_SOLICITOR = "Other solicitor";

        @Test
        void shouldSendNotificationToDefendantLip_whenEventIsCalledAndDefendantHasEmail() {
            //Given
            CaseData caseData = CaseData.builder()
                .respondent1(Party.builder().type(Party.Type.COMPANY).companyName(DEFENDANT_PARTY_NAME).partyEmail(
                    DEFENDANT_EMAIL_ADDRESS).build())
                .applicant1(Party.builder().type(Party.Type.COMPANY).companyName(CLAIMANT_ORG_NAME).build())
                .legacyCaseReference(REFERENCE_NUMBER)
                .respondent1Represented(YesOrNo.NO)
                .addApplicant2(YesOrNo.NO)
                .addRespondent2(YesOrNo.NO)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId(NOTIFY_DEFENDANT_LIP_CLAIMANT_REPRESENTED.name()).build()).build();
            //When
            given(notificationsProperties.getNotifyRespondentLipForClaimantRepresentedTemplate()).willReturn(EMAIL_TEMPLATE);
            notificationHandler.handle(params);
            //Then
            verify(notificationService, times(1)).sendMail(targetEmail.capture(),
                                                           emailTemplate.capture(),
                                                           notificationDataMap.capture(), reference.capture()
            );
            assertThat(targetEmail.getAllValues().get(0)).isEqualTo(DEFENDANT_EMAIL_ADDRESS);
            assertThat(emailTemplate.getAllValues().get(0)).isEqualTo(EMAIL_TEMPLATE);
        }

        @Test
        void shouldSendNotificationToDefendantLR_whenEventIsCalledAndDefendantHasEmail() {
            //Given
            when(organisationService.findOrganisationById("QWERTY A"))
                .thenReturn(Optional.of(uk.gov.hmcts.reform.civil.prd.model.Organisation.builder().name(OTHER_SOLICITOR)
                                            .build()));
            CaseData caseData = CaseData.builder()
                .respondent1(Party.builder().type(Party.Type.COMPANY).companyName(DEFENDANT_PARTY_NAME).partyEmail(
                    DEFENDANT_EMAIL_ADDRESS).build())
                .applicant1(Party.builder().type(Party.Type.COMPANY).companyName(CLAIMANT_ORG_NAME).build())
                .legacyCaseReference(REFERENCE_NUMBER)
                .respondent1Represented(YesOrNo.YES)
                .respondentSolicitor1EmailAddress(DEFENDANT_EMAIL_ADDRESS)
                .addApplicant2(YesOrNo.NO)
                .addRespondent2(YesOrNo.NO)
                .issueDate(LocalDate.now())
                .ccdCaseReference(Long.valueOf(123456344))
                .changeOfRepresentation(ChangeOfRepresentation.builder().organisationToAddID("QWERTY A")
                                            .caseRole(CaseRole.APPLICANTSOLICITORONE.getFormattedName()).build())
                .respondent1OrganisationPolicy(OrganisationPolicy.builder()
                                                   .organisation(Organisation.builder().organisationID("QWERTY A")
                                                                     .build()).build())
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId(NOTIFY_DEFENDANT_LIP_CLAIMANT_REPRESENTED.name()).build())
                .build();
            //When
            given(notificationsProperties.getNoticeOfChangeOtherParties()).willReturn(EMAIL_TEMPLATE);
            notificationHandler.handle(params);
            //Then
            verify(notificationService, times(1)).sendMail(targetEmail.capture(),
                                                           emailTemplate.capture(),
                                                           notificationDataMap.capture(), reference.capture()
            );
            assertThat(targetEmail.getAllValues().get(0)).isEqualTo(DEFENDANT_EMAIL_ADDRESS);
            assertThat(emailTemplate.getAllValues().get(0)).isEqualTo(EMAIL_TEMPLATE);
        }

        @Test
        void notifyApplicantAfterNocApproval() {

            //Given
            CaseData caseData = CaseData.builder()
                .respondent1(Party.builder().type(Party.Type.COMPANY).companyName(DEFENDANT_PARTY_NAME).partyEmail(
                    DEFENDANT_EMAIL_ADDRESS).build())
                .applicant1(Party.builder().type(Party.Type.COMPANY).companyName(CLAIMANT_ORG_NAME)
                                .partyEmail(APPLICANT_EMAIL_ADDRESS).build())
                .legacyCaseReference(REFERENCE_NUMBER)
                .addApplicant2(YesOrNo.NO)
                .addRespondent2(YesOrNo.NO)
                .claimantBilingualLanguagePreference(Language.ENGLISH.toString())
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId(NOTIFY_CLAIMANT_LIP_AFTER_NOC_APPROVAL.name()).build())
                .build();
            //When
            given(notificationsProperties.getNotifyClaimantLipForNoLongerAccessTemplate()).willReturn(EMAIL_TEMPLATE);
            notificationHandler.handle(params);
            //Then
            verify(notificationService, times(1)).sendMail(targetEmail.capture(),
                                                           emailTemplate.capture(),
                                                           notificationDataMap.capture(), reference.capture()
            );
            assertThat(targetEmail.getAllValues().get(0)).isEqualTo(APPLICANT_EMAIL_ADDRESS);
            assertThat(emailTemplate.getAllValues().get(0)).isEqualTo(EMAIL_TEMPLATE);
        }

        @Test
        @DisplayName("Should not send any notification to Applicant1's party as no email available")
        void doNotNotifyApplicantAfterNocApproval() {

            //Given
            CaseData caseData = CaseData.builder()
                .respondent1(Party.builder().type(Party.Type.COMPANY).companyName(DEFENDANT_PARTY_NAME).partyEmail(
                    DEFENDANT_EMAIL_ADDRESS).build())
                .applicant1(Party.builder().type(Party.Type.COMPANY).companyName(CLAIMANT_ORG_NAME).build())
                .legacyCaseReference(REFERENCE_NUMBER)
                .addApplicant2(YesOrNo.NO)
                .addRespondent2(YesOrNo.NO)
                .claimantBilingualLanguagePreference(Language.ENGLISH.toString())
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId(NOTIFY_CLAIMANT_LIP_AFTER_NOC_APPROVAL.name()).build())
                .build();
            //When
            given(notificationsProperties.getNotifyClaimantLipForNoLongerAccessTemplate()).willReturn(EMAIL_TEMPLATE);
            notificationHandler.handle(params);
            //Then
            verifyNoInteractions(notificationService);
        }

        @Test
        void notifyApplicantAfterNocApprovalBilingual() {

            //Given
            CaseData caseData = CaseData.builder()
                .respondent1(Party.builder().type(Party.Type.COMPANY).companyName(DEFENDANT_PARTY_NAME).partyEmail(
                    DEFENDANT_EMAIL_ADDRESS).build())
                .applicant1(Party.builder().type(Party.Type.COMPANY).companyName(CLAIMANT_ORG_NAME)
                                .partyEmail(APPLICANT_EMAIL_ADDRESS).build())
                .legacyCaseReference(REFERENCE_NUMBER)
                .addApplicant2(YesOrNo.NO)
                .addRespondent2(YesOrNo.NO)
                .claimantBilingualLanguagePreference(Language.WELSH.toString())
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId(NOTIFY_CLAIMANT_LIP_AFTER_NOC_APPROVAL.name()).build())
                .build();
            //When
            given(notificationsProperties.getNotifyClaimantLipForNoLongerAccessWelshTemplate()).willReturn(EMAIL_WELSH_TEMPLATE);
            notificationHandler.handle(params);
            //Then
            verify(notificationService, times(1)).sendMail(targetEmail.capture(),
                                                           emailTemplate.capture(),
                                                           notificationDataMap.capture(), reference.capture()
            );
            assertThat(targetEmail.getAllValues().get(0)).isEqualTo(APPLICANT_EMAIL_ADDRESS);
            assertThat(emailTemplate.getAllValues().get(0)).isEqualTo(EMAIL_WELSH_TEMPLATE);
        }

        @Test
        void shouldSendNotificationToApplicantSolicitorAfterNoc() {
            //Given
            when(configuration.getHmctsSignature()).thenReturn("Online Civil Claims \n HM Courts & Tribunal Service");
            when(configuration.getPhoneContact()).thenReturn("For anything related to hearings, call 0300 123 5577 "
                                                                 + "\n For all other matters, call 0300 123 7050");
            when(configuration.getOpeningHours()).thenReturn("Monday to Friday, 8.30am to 5pm");
            when(configuration.getSpecUnspecContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk "
                                                                      + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");
            CaseData caseData = CaseData.builder()
                    .respondent1(Party.builder().type(Party.Type.COMPANY).companyName(DEFENDANT_PARTY_NAME).partyEmail(
                            DEFENDANT_EMAIL_ADDRESS).build())
                    .applicant1(Party.builder().type(Party.Type.COMPANY).companyName(CLAIMANT_ORG_NAME).build())
                    .legacyCaseReference(REFERENCE_NUMBER)
                    .addApplicant2(YesOrNo.NO)
                    .addRespondent2(YesOrNo.NO)
                    .applicant1OrganisationPolicy(OrganisationPolicy.builder()
                            .organisation(Organisation.builder().organisationID("HR2D876").build())
                            .orgPolicyCaseAssignedRole(APPLICANTSOLICITORONE.getFormattedName())
                            .build())
                    .ccdCaseReference(12345L)
                    .applicantSolicitor1UserDetails(
                            IdamUserDetails.builder().id("submitter-id").email("applicantsolicitor@example.com").build())
                    .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                    .request(CallbackRequest.builder().eventId(NOTIFY_APPLICANT_LIP_SOLICITOR.name()).build()).build();

            when(organisationService.findOrganisationById(anyString()))
                    .thenReturn(Optional.of(uk.gov.hmcts.reform.civil.prd.model.Organisation.builder().name("test solicitor").build()));

            //When
            given(notificationsProperties.getNoticeOfChangeApplicantLipSolicitorTemplate()).willReturn(APPLICANT_SOLICITOR_TEMPLATE);
            notificationHandler.handle(params);
            //Then
            verify(notificationService, times(1)).sendMail(targetEmail.capture(),
                    emailTemplate.capture(),
                    notificationDataMap.capture(), reference.capture());

            assertThat(targetEmail.getAllValues().get(0)).isEqualTo("applicantsolicitor@example.com");
            assertThat(emailTemplate.getAllValues().get(0)).isEqualTo(APPLICANT_SOLICITOR_TEMPLATE);
        }
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(notificationHandler
                .camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
                        NOTIFY_CLAIMANT_LIP_AFTER_NOC_APPROVAL.name()).build()).build()))
                .isEqualTo(TASK_ID_APPLICANT);

        assertThat(notificationHandler
                .camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
                        NOTIFY_DEFENDANT_LIP_CLAIMANT_REPRESENTED.name()).build()).build()))
                .isEqualTo(TASK_ID_RESPONDENT);

        assertThat(notificationHandler
                .camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
                        NOTIFY_APPLICANT_LIP_SOLICITOR.name()).build()).build()))
                .isEqualTo(TASK_ID_APPLICANT_SOLICITOR);
    }
}
