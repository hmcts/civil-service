package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.config.properties.notification.NotificationsProperties;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.service.NotificationService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;

@RunWith(SpringRunner.class)
public class BreathingSpaceEnterNotificationHandlerTest {

    @InjectMocks
    private BreathingSpaceEnterNotificationHandler handler;

    @Mock
    private NotificationService notificationService;
    @Mock
    private NotificationsProperties notificationsProperties;
    @Mock
    private OrganisationService organisationService;

    @Test
    public void notifyRespondent1_enter() {
        String recipient = "recipient";
        String templateId = "templateId";
        Mockito.when(notificationsProperties.getBreathingSpaceEnterDefendantEmailTemplate())
            .thenReturn(templateId);

        String organisationId = "organisationId";
        String organisationName = "organisation name";
        Mockito.when(organisationService.findOrganisationById(organisationId))
            .thenReturn(Optional.of(uk.gov.hmcts.reform.prd.model.Organisation.builder()
                            .name(organisationName)
                            .build()));

        CaseData caseData = CaseData.builder()
            .legacyCaseReference("legacy ref")
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
                map -> map.get(NotificationData.CLAIM_REFERENCE_NUMBER).equals(caseData.getLegacyCaseReference())
            && map.get(NotificationData.CLAIM_DEFENDANT_LEGAL_ORG_NAME_SPEC).equals(organisationName)
            && map.get("defendantName").equals(caseData.getRespondent1().getPartyName())),
            argThat(string -> string.contains(caseData.getLegacyCaseReference()))
        );
    }

    @Test
    public void notifyRespondent2_enter() {
        String recipient = "recipient";
        String templateId = "templateId";
        Mockito.when(notificationsProperties.getBreathingSpaceEnterDefendantEmailTemplate())
            .thenReturn(templateId);

        String organisationId = "organisationId";
        String organisationName = "organisation name";
        Mockito.when(organisationService.findOrganisationById(organisationId))
            .thenReturn(Optional.of(uk.gov.hmcts.reform.prd.model.Organisation.builder()
                                        .name(organisationName)
                                        .build()));

        CaseData caseData = CaseData.builder()
            .legacyCaseReference("legacy ref")
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
                map -> map.get(NotificationData.CLAIM_REFERENCE_NUMBER).equals(caseData.getLegacyCaseReference())
                    && map.get(NotificationData.CLAIM_DEFENDANT_LEGAL_ORG_NAME_SPEC).equals(organisationName)
                    && map.get("defendantName").equals(caseData.getRespondent2().getPartyName())),
            argThat(string -> string.contains(caseData.getLegacyCaseReference()))
        );
    }

    @Test
    public void notifyApplicant1_enter() {
        String recipient = "recipient";
        String templateId = "templateId";
        Mockito.when(notificationsProperties.getBreathingSpaceEnterApplicantEmailTemplate())
            .thenReturn(templateId);

        String organisationId = "organisationId";

        String solicitorName = "solicitor name";
        CaseData caseData = CaseData.builder()
            .legacyCaseReference("legacy ref")
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
                map -> map.get(NotificationData.CLAIM_REFERENCE_NUMBER).equals(caseData.getLegacyCaseReference())
                    && map.get(NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC).equals(solicitorName)),
            argThat(string -> string.contains(caseData.getLegacyCaseReference()))
        );
    }

    @Test
    public void notifyApplicant2_enter() {
        String recipient = "recipient";
        String templateId = "templateId";
        Mockito.when(notificationsProperties.getBreathingSpaceEnterApplicantEmailTemplate())
            .thenReturn(templateId);

        String organisationId = "organisationId";

        String solicitorName = "solicitor name";
        CaseData caseData = CaseData.builder()
            .legacyCaseReference("legacy ref")
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
            .respondentSolicitor2EmailAddress(null)
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
                map -> map.get(NotificationData.CLAIM_REFERENCE_NUMBER).equals(caseData.getLegacyCaseReference())
                    && map.get(NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC).equals(solicitorName)),
            argThat(string -> string.contains(caseData.getLegacyCaseReference()))
        );
    }
}
