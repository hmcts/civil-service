package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.CASE_ID;

@ExtendWith(SpringExtension.class)
class BreathingSpaceEnterNotificationHandlerTest {

    @InjectMocks
    private BreathingSpaceEnterNotificationHandler handler;

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
    void notifyRespondent1_enter() {
        String recipient = "recipient";
        String templateId = "templateId";
        Mockito.when(notificationsProperties.getBreathingSpaceEnterDefendantEmailTemplate())
            .thenReturn(templateId);

        String organisationId = "organisationId";
        String organisationName = "organisation name";
        Mockito.when(organisationService.findOrganisationById(organisationId))
            .thenReturn(Optional.of(uk.gov.hmcts.reform.civil.prd.model.Organisation.builder()
                                        .name(organisationName)
                                        .build()));

        CaseData caseData = CaseData.builder()
            .legacyCaseReference("legacy ref")
            .ccdCaseReference(CASE_ID)
            .respondent1(Party.builder()
                             .type(Party.Type.COMPANY)
                             .companyName("company name")
                             .build())
            .respondentSolicitor1EmailAddress(recipient)
            .respondent1OrganisationPolicy(OrganisationPolicy.builder()
                                               .organisation(Organisation.builder()
                                                                 .organisationID(organisationId)
                                                                 .build())
                                               .build())
            .build();
        CallbackParams params = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_SUBMIT)
            .caseData(caseData)
            .request(CallbackRequest.builder()
                         .eventId(CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_BREATHING_SPACE_ENTER.name())
                         .build())
            .build();

        handler.handle(params);

        Mockito.verify(notificationService).sendMail(
            eq(recipient),
            eq(templateId),
            argThat(
                map -> map.get(NotificationData.CLAIM_REFERENCE_NUMBER).equals(caseData.getCcdCaseReference().toString())
                    && map.get(NotificationData.CLAIM_DEFENDANT_LEGAL_ORG_NAME_SPEC).equals(organisationName)
                    && map.get(NotificationData.PHONE_CONTACT).equals("For anything related to hearings, call 0300 123 5577 \n For all other matters, call 0300 123 7050")
                    && map.get(NotificationData.OPENING_HOURS).equals("Monday to Friday, 8.30am to 5pm")
                    && map.get(NotificationData.SPEC_UNSPEC_CONTACT).equals("Email for Specified Claims: contactocmc@justice.gov.uk "
                                                                                + "\n Email for Damages Claims: damagesclaims@justice.gov.uk")
                    && map.get(NotificationData.HMCTS_SIGNATURE).equals("Online Civil Claims \n HM Courts & Tribunal Service")
                    && map.get("defendantName").equals(caseData.getRespondent1().getPartyName())),
            argThat(string -> string.contains(caseData.getLegacyCaseReference()))
        );
    }

    @Test
    void notifyRespondent2_enter() {
        String recipient = "recipient";
        String templateId = "templateId";
        Mockito.when(notificationsProperties.getBreathingSpaceEnterDefendantEmailTemplate())
            .thenReturn(templateId);

        String organisationId = "organisationId";
        String organisationName = "organisation name";
        Mockito.when(organisationService.findOrganisationById(organisationId))
            .thenReturn(Optional.of(uk.gov.hmcts.reform.civil.prd.model.Organisation.builder()
                                        .name(organisationName)
                                        .build()));

        CaseData caseData = CaseData.builder()
            .legacyCaseReference("legacy ref")
            .ccdCaseReference(CASE_ID)
            .respondent2(Party.builder()
                             .type(Party.Type.COMPANY)
                             .companyName("company name")
                             .build())
            .respondentSolicitor2EmailAddress(recipient)
            .respondent2OrganisationPolicy(OrganisationPolicy.builder()
                                               .organisation(Organisation.builder()
                                                                 .organisationID(organisationId)
                                                                 .build())
                                               .build())
            .build();
        CallbackParams params = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_SUBMIT)
            .caseData(caseData)
            .request(CallbackRequest.builder()
                         .eventId(CaseEvent.NOTIFY_RESPONDENT_SOLICITOR2_BREATHING_SPACE_ENTER.name())
                         .build())
            .build();

        handler.handle(params);

        Mockito.verify(notificationService).sendMail(
            eq(recipient),
            eq(templateId),
            argThat(
                map -> map.get(NotificationData.CLAIM_REFERENCE_NUMBER).equals(caseData.getCcdCaseReference().toString())
                    && map.get(NotificationData.CLAIM_DEFENDANT_LEGAL_ORG_NAME_SPEC).equals(organisationName)
                    && map.get(NotificationData.PHONE_CONTACT).equals("For anything related to hearings, call 0300 123 5577 \n For all other matters, call 0300 123 7050")
                    && map.get(NotificationData.OPENING_HOURS).equals("Monday to Friday, 8.30am to 5pm")
                    && map.get(NotificationData.SPEC_UNSPEC_CONTACT).equals("Email for Specified Claims: contactocmc@justice.gov.uk "
                                                                                + "\n Email for Damages Claims: damagesclaims@justice.gov.uk")
                    && map.get(NotificationData.HMCTS_SIGNATURE).equals("Online Civil Claims \n HM Courts & Tribunal Service")
                    && map.get("defendantName").equals(caseData.getRespondent2().getPartyName())),
            argThat(string -> string.contains(caseData.getLegacyCaseReference()))
        );
    }

    @Test
    void notifyRespondent2SameSolicitor_enter() {
        String recipient = "recipient";
        String templateId = "templateId";
        Mockito.when(notificationsProperties.getBreathingSpaceEnterDefendantEmailTemplate())
            .thenReturn(templateId);

        String organisationId = "organisationId";
        String organisationName = "organisation name";
        Mockito.when(organisationService.findOrganisationById(organisationId))
            .thenReturn(Optional.of(uk.gov.hmcts.reform.civil.prd.model.Organisation.builder()
                                        .name(organisationName)
                                        .build()));

        CaseData caseData = CaseData.builder()
            .legacyCaseReference("legacy ref")
            .ccdCaseReference(CASE_ID)
            .respondent2(Party.builder()
                             .type(Party.Type.COMPANY)
                             .companyName("company name")
                             .build())
            .respondentSolicitor2EmailAddress(null)
            .respondentSolicitor1EmailAddress(recipient)
            .respondent2OrganisationPolicy(OrganisationPolicy.builder()
                                               .organisation(Organisation.builder()
                                                                 .organisationID(organisationId)
                                                                 .build())
                                               .build())
            .respondent2SameLegalRepresentative(YesOrNo.YES)
            .build();
        CallbackParams params = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_SUBMIT)
            .caseData(caseData)
            .request(CallbackRequest.builder()
                         .eventId(CaseEvent.NOTIFY_RESPONDENT_SOLICITOR2_BREATHING_SPACE_ENTER.name())
                         .build())
            .build();

        handler.handle(params);

        Mockito.verify(notificationService).sendMail(
            eq(recipient),
            eq(templateId),
            argThat(
                map -> map.get(NotificationData.CLAIM_REFERENCE_NUMBER).equals(caseData.getCcdCaseReference().toString())
                    && map.get(NotificationData.CLAIM_DEFENDANT_LEGAL_ORG_NAME_SPEC).equals(organisationName)
                    && map.get(NotificationData.PHONE_CONTACT).equals("For anything related to hearings, call 0300 123 5577 \n For all other matters, call 0300 123 7050")
                    && map.get(NotificationData.OPENING_HOURS).equals("Monday to Friday, 8.30am to 5pm")
                    && map.get(NotificationData.SPEC_UNSPEC_CONTACT).equals("Email for Specified Claims: contactocmc@justice.gov.uk "
                                                                                + "\n Email for Damages Claims: damagesclaims@justice.gov.uk")
                    && map.get(NotificationData.HMCTS_SIGNATURE).equals("Online Civil Claims \n HM Courts & Tribunal Service")
                    && map.get("defendantName").equals(caseData.getRespondent2().getPartyName())),
            argThat(string -> string.contains(caseData.getLegacyCaseReference()))
        );
    }

    @Test
    void notifyRespondent2DiffSolicitor_enter() {
        String recipient = "recipient";
        String templateId = "templateId";
        Mockito.when(notificationsProperties.getBreathingSpaceEnterDefendantEmailTemplate())
            .thenReturn(templateId);

        String organisationId = "organisationId";
        String organisationName = "organisation name";
        Mockito.when(organisationService.findOrganisationById(organisationId))
            .thenReturn(Optional.of(uk.gov.hmcts.reform.civil.prd.model.Organisation.builder()
                                        .name(organisationName)
                                        .build()));

        CaseData caseData = CaseData.builder()
            .legacyCaseReference("legacy ref")
            .ccdCaseReference(CASE_ID)
            .respondent2(Party.builder()
                             .type(Party.Type.COMPANY)
                             .companyName("company name")
                             .build())
            .respondentSolicitor2EmailAddress(null)
            .respondentSolicitor1EmailAddress(recipient)
            .respondent2OrganisationPolicy(OrganisationPolicy.builder()
                                               .organisation(Organisation.builder()
                                                                 .organisationID(organisationId)
                                                                 .build())
                                               .build())
            .respondent2SameLegalRepresentative(YesOrNo.NO)
            .build();
        CallbackParams params = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_SUBMIT)
            .caseData(caseData)
            .request(CallbackRequest.builder()
                         .eventId(CaseEvent.NOTIFY_RESPONDENT_SOLICITOR2_BREATHING_SPACE_ENTER.name())
                         .build())
            .build();

        handler.handle(params);

        Mockito.verify(notificationService).sendMail(
            eq(null),
            eq(templateId),
            argThat(
                map -> map.get(NotificationData.CLAIM_REFERENCE_NUMBER).equals(caseData.getCcdCaseReference().toString())
                    && map.get(NotificationData.CLAIM_DEFENDANT_LEGAL_ORG_NAME_SPEC).equals(organisationName)
                    && map.get(NotificationData.PHONE_CONTACT).equals("For anything related to hearings, call 0300 123 5577 \n For all other matters, call 0300 123 7050")
                    && map.get(NotificationData.OPENING_HOURS).equals("Monday to Friday, 8.30am to 5pm")
                    && map.get(NotificationData.SPEC_UNSPEC_CONTACT).equals("Email for Specified Claims: contactocmc@justice.gov.uk "
                                                                                + "\n Email for Damages Claims: damagesclaims@justice.gov.uk")
                    && map.get(NotificationData.HMCTS_SIGNATURE).equals("Online Civil Claims \n HM Courts & Tribunal Service")
                    && map.get("defendantName").equals(caseData.getRespondent2().getPartyName())),
            argThat(string -> string.contains(caseData.getLegacyCaseReference()))
        );
    }

    @Test
    void notifyApplicant1_enter() {
        String recipient = "recipient";
        String templateId = "templateId";
        Mockito.when(notificationsProperties.getBreathingSpaceEnterApplicantEmailTemplate())
            .thenReturn(templateId);

        String organisationId = "organisationId";

        String solicitorName = "solicitor name";
        CaseData caseData = CaseData.builder()
            .legacyCaseReference("legacy ref")
            .ccdCaseReference(CASE_ID)
            .applicant1(Party.builder()
                            .type(Party.Type.COMPANY)
                            .companyName("company name")
                            .build())
            .applicantSolicitor1UserDetails(IdamUserDetails.builder()
                                                .email(recipient)
                                                .build())
            .applicant1OrganisationPolicy(OrganisationPolicy.builder()
                                              .organisation(Organisation.builder()
                                                                .organisationID(organisationId)
                                                                .build())
                                              .build())
            .applicantSolicitor1ClaimStatementOfTruth(StatementOfTruth.builder()
                                                          .name(solicitorName)
                                                          .build())
            .build();
        CallbackParams params = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_SUBMIT)
            .caseData(caseData)
            .request(CallbackRequest.builder()
                         .eventId(CaseEvent.NOTIFY_APPLICANT_SOLICITOR1_BREATHING_SPACE_ENTER.name())
                         .build())
            .build();

        handler.handle(params);

        Mockito.verify(notificationService).sendMail(
            eq(recipient),
            eq(templateId),
            argThat(
                map -> map.get(NotificationData.CLAIM_REFERENCE_NUMBER).equals(caseData.getCcdCaseReference().toString())
                    && map.get(NotificationData.PHONE_CONTACT).equals("For anything related to hearings, call 0300 123 5577 \n For all other matters, call 0300 123 7050")
                    && map.get(NotificationData.OPENING_HOURS).equals("Monday to Friday, 8.30am to 5pm")
                    && map.get(NotificationData.SPEC_UNSPEC_CONTACT).equals("Email for Specified Claims: contactocmc@justice.gov.uk "
                                                                                + "\n Email for Damages Claims: damagesclaims@justice.gov.uk")
                    && map.get(NotificationData.HMCTS_SIGNATURE).equals("Online Civil Claims \n HM Courts & Tribunal Service")
                    && map.get(NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC).equals(solicitorName)),
            argThat(string -> string.contains(caseData.getLegacyCaseReference()))
        );
    }
}
