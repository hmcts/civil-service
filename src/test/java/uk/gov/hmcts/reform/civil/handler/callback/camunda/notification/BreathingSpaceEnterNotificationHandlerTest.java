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
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;
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
    void notifyRespondent1_enter() {
        String recipient = "recipient";
        String templateId = "templateId";
        Mockito.when(notificationsProperties.getBreathingSpaceEnterDefendantEmailTemplate())
            .thenReturn(templateId);

        String organisationId = "organisationId";
        String organisationName = "organisation name";
        Mockito.when(organisationService.findOrganisationById(organisationId))
            .thenReturn(Optional.of(new uk.gov.hmcts.reform.civil.prd.model.Organisation()
                                        .setName(organisationName)
                                        ));

        CaseData caseData = CaseData.builder()
            .legacyCaseReference("legacy ref")
            .ccdCaseReference(CASE_ID)
            .respondent1(Party.builder()
                             .type(Party.Type.COMPANY)
                             .companyName("company name")
                             .build())
            .respondentSolicitor1EmailAddress(recipient)
            .respondent1OrganisationPolicy(new OrganisationPolicy()
                                               .setOrganisation(new Organisation()
                                                                    .setOrganisationID(organisationId)))
            .build();
        CallbackParams params = new CallbackParams()
            .type(CallbackType.ABOUT_TO_SUBMIT)
            .caseData(caseData)
            .request(CallbackRequest.builder()
                         .eventId(CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_BREATHING_SPACE_ENTER.name())
                         .build());

        handler.handle(params);

        Mockito.verify(notificationService).sendMail(
            eq(recipient),
            eq(templateId),
            argThat(
                map -> map.get(NotificationData.CLAIM_REFERENCE_NUMBER).equals(caseData.getCcdCaseReference().toString())
                    && map.get(NotificationData.CLAIM_DEFENDANT_LEGAL_ORG_NAME_SPEC).equals(organisationName)
                    && map.get(NotificationData.PHONE_CONTACT).equals(configuration.getPhoneContact())
                    && map.get(NotificationData.OPENING_HOURS).equals(configuration.getOpeningHours())
                    && map.get(NotificationData.HMCTS_SIGNATURE).equals(configuration.getHmctsSignature())
                    && map.get(NotificationData.WELSH_PHONE_CONTACT).equals(configuration.getWelshPhoneContact())
                    && map.get(NotificationData.WELSH_OPENING_HOURS).equals(configuration.getWelshOpeningHours())
                    && map.get(NotificationData.WELSH_HMCTS_SIGNATURE).equals(configuration.getWelshHmctsSignature())
                    && map.get(NotificationData.LIP_CONTACT).equals(configuration.getLipContactEmail())
                    && map.get(NotificationData.LIP_CONTACT_WELSH).equals(configuration.getLipContactEmailWelsh())
                    && map.get(NotificationData.SPEC_UNSPEC_CONTACT).equals(configuration.getRaiseQueryLr())
                    && map.get(NotificationData.CNBC_CONTACT).equals(configuration.getRaiseQueryLr())
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
            .thenReturn(Optional.of(new uk.gov.hmcts.reform.civil.prd.model.Organisation()
                                        .setName(organisationName)
                                        ));

        CaseData caseData = CaseData.builder()
            .legacyCaseReference("legacy ref")
            .ccdCaseReference(CASE_ID)
            .respondent2(Party.builder()
                             .type(Party.Type.COMPANY)
                             .companyName("company name")
                             .build())
            .respondentSolicitor2EmailAddress(recipient)
            .respondent2OrganisationPolicy(new OrganisationPolicy()
                                               .setOrganisation(new Organisation()
                                                                    .setOrganisationID(organisationId)))
            .build();
        CallbackParams params = new CallbackParams()
            .type(CallbackType.ABOUT_TO_SUBMIT)
            .caseData(caseData)
            .request(CallbackRequest.builder()
                         .eventId(CaseEvent.NOTIFY_RESPONDENT_SOLICITOR2_BREATHING_SPACE_ENTER.name())
                         .build());

        handler.handle(params);

        Mockito.verify(notificationService).sendMail(
            eq(recipient),
            eq(templateId),
            argThat(
                map -> map.get(NotificationData.CLAIM_REFERENCE_NUMBER).equals(caseData.getCcdCaseReference().toString())
                    && map.get(NotificationData.CLAIM_DEFENDANT_LEGAL_ORG_NAME_SPEC).equals(organisationName)
                    && map.get(NotificationData.PHONE_CONTACT).equals(configuration.getPhoneContact())
                    && map.get(NotificationData.OPENING_HOURS).equals(configuration.getOpeningHours())
                    && map.get(NotificationData.HMCTS_SIGNATURE).equals(configuration.getHmctsSignature())
                    && map.get(NotificationData.WELSH_PHONE_CONTACT).equals(configuration.getWelshPhoneContact())
                    && map.get(NotificationData.WELSH_OPENING_HOURS).equals(configuration.getWelshOpeningHours())
                    && map.get(NotificationData.WELSH_HMCTS_SIGNATURE).equals(configuration.getWelshHmctsSignature())
                    && map.get(NotificationData.LIP_CONTACT).equals(configuration.getLipContactEmail())
                    && map.get(NotificationData.LIP_CONTACT_WELSH).equals(configuration.getLipContactEmailWelsh())
                    && map.get(NotificationData.SPEC_UNSPEC_CONTACT).equals(configuration.getRaiseQueryLr())
                    && map.get(NotificationData.CNBC_CONTACT).equals(configuration.getRaiseQueryLr())
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
            .thenReturn(Optional.of(new uk.gov.hmcts.reform.civil.prd.model.Organisation()
                                        .setName(organisationName)));

        CaseData caseData = CaseData.builder()
            .legacyCaseReference("legacy ref")
            .ccdCaseReference(CASE_ID)
            .respondent2(Party.builder()
                             .type(Party.Type.COMPANY)
                             .companyName("company name")
                             .build())
            .respondentSolicitor2EmailAddress(null)
            .respondentSolicitor1EmailAddress(recipient)
            .respondent2OrganisationPolicy(new OrganisationPolicy()
                                               .setOrganisation(new Organisation()
                                                                    .setOrganisationID(organisationId)))
            .respondent2SameLegalRepresentative(YesOrNo.YES)
            .build();
        CallbackParams params = new CallbackParams()
            .type(CallbackType.ABOUT_TO_SUBMIT)
            .caseData(caseData)
            .request(CallbackRequest.builder()
                         .eventId(CaseEvent.NOTIFY_RESPONDENT_SOLICITOR2_BREATHING_SPACE_ENTER.name())
                         .build());

        handler.handle(params);

        Mockito.verify(notificationService).sendMail(
            eq(recipient),
            eq(templateId),
            argThat(
                map -> map.get(NotificationData.CLAIM_REFERENCE_NUMBER).equals(caseData.getCcdCaseReference().toString())
                    && map.get(NotificationData.CLAIM_DEFENDANT_LEGAL_ORG_NAME_SPEC).equals(organisationName)
                    && map.get(NotificationData.PHONE_CONTACT).equals(configuration.getPhoneContact())
                    && map.get(NotificationData.OPENING_HOURS).equals(configuration.getOpeningHours())
                    && map.get(NotificationData.HMCTS_SIGNATURE).equals(configuration.getHmctsSignature())
                    && map.get(NotificationData.WELSH_PHONE_CONTACT).equals(configuration.getWelshPhoneContact())
                    && map.get(NotificationData.WELSH_OPENING_HOURS).equals(configuration.getWelshOpeningHours())
                    && map.get(NotificationData.WELSH_HMCTS_SIGNATURE).equals(configuration.getWelshHmctsSignature())
                    && map.get(NotificationData.LIP_CONTACT).equals(configuration.getLipContactEmail())
                    && map.get(NotificationData.LIP_CONTACT_WELSH).equals(configuration.getLipContactEmailWelsh())
                    && map.get(NotificationData.SPEC_UNSPEC_CONTACT).equals(configuration.getRaiseQueryLr())
                    && map.get(NotificationData.CNBC_CONTACT).equals(configuration.getRaiseQueryLr())
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
            .thenReturn(Optional.of(new uk.gov.hmcts.reform.civil.prd.model.Organisation()
                                        .setName(organisationName)
                                        ));

        CaseData caseData = CaseData.builder()
            .legacyCaseReference("legacy ref")
            .ccdCaseReference(CASE_ID)
            .respondent2(Party.builder()
                             .type(Party.Type.COMPANY)
                             .companyName("company name")
                             .build())
            .respondentSolicitor2EmailAddress(null)
            .respondentSolicitor1EmailAddress(recipient)
            .respondent2OrganisationPolicy(new OrganisationPolicy()
                                               .setOrganisation(new Organisation()
                                                                    .setOrganisationID(organisationId)))
            .respondent2SameLegalRepresentative(YesOrNo.NO)
            .build();
        CallbackParams params = new CallbackParams()
            .type(CallbackType.ABOUT_TO_SUBMIT)
            .caseData(caseData)
            .request(CallbackRequest.builder()
                         .eventId(CaseEvent.NOTIFY_RESPONDENT_SOLICITOR2_BREATHING_SPACE_ENTER.name())
                         .build());

        handler.handle(params);

        Mockito.verify(notificationService).sendMail(
            eq(null),
            eq(templateId),
            argThat(
                map -> map.get(NotificationData.CLAIM_REFERENCE_NUMBER).equals(caseData.getCcdCaseReference().toString())
                    && map.get(NotificationData.CLAIM_DEFENDANT_LEGAL_ORG_NAME_SPEC).equals(organisationName)
                    && map.get(NotificationData.PHONE_CONTACT).equals(configuration.getPhoneContact())
                    && map.get(NotificationData.OPENING_HOURS).equals(configuration.getOpeningHours())
                    && map.get(NotificationData.HMCTS_SIGNATURE).equals(configuration.getHmctsSignature())
                    && map.get(NotificationData.WELSH_PHONE_CONTACT).equals(configuration.getWelshPhoneContact())
                    && map.get(NotificationData.WELSH_OPENING_HOURS).equals(configuration.getWelshOpeningHours())
                    && map.get(NotificationData.WELSH_HMCTS_SIGNATURE).equals(configuration.getWelshHmctsSignature())
                    && map.get(NotificationData.LIP_CONTACT).equals(configuration.getLipContactEmail())
                    && map.get(NotificationData.LIP_CONTACT_WELSH).equals(configuration.getLipContactEmailWelsh())
                    && map.get(NotificationData.SPEC_UNSPEC_CONTACT).equals(configuration.getRaiseQueryLr())
                    && map.get(NotificationData.CNBC_CONTACT).equals(configuration.getRaiseQueryLr())
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
            .applicantSolicitor1UserDetails(new IdamUserDetails()
                                                .setEmail(recipient))
            .applicant1OrganisationPolicy(new OrganisationPolicy()
                                              .setOrganisation(new Organisation()
                                                                   .setOrganisationID(organisationId)))
            .applicantSolicitor1ClaimStatementOfTruth(new StatementOfTruth()
                                                          .setName(solicitorName)
                                                          )
            .build();
        CallbackParams params = new CallbackParams()
            .type(CallbackType.ABOUT_TO_SUBMIT)
            .caseData(caseData)
            .request(CallbackRequest.builder()
                         .eventId(CaseEvent.NOTIFY_APPLICANT_SOLICITOR1_BREATHING_SPACE_ENTER.name())
                         .build());

        handler.handle(params);

        Mockito.verify(notificationService).sendMail(
            eq(recipient),
            eq(templateId),
            argThat(
                map -> map.get(NotificationData.CLAIM_REFERENCE_NUMBER).equals(caseData.getCcdCaseReference().toString())
                    && map.get(NotificationData.PHONE_CONTACT).equals(configuration.getPhoneContact())
                    && map.get(NotificationData.OPENING_HOURS).equals(configuration.getOpeningHours())
                    && map.get(NotificationData.HMCTS_SIGNATURE).equals(configuration.getHmctsSignature())
                    && map.get(NotificationData.WELSH_PHONE_CONTACT).equals(configuration.getWelshPhoneContact())
                    && map.get(NotificationData.WELSH_OPENING_HOURS).equals(configuration.getWelshOpeningHours())
                    && map.get(NotificationData.WELSH_HMCTS_SIGNATURE).equals(configuration.getWelshHmctsSignature())
                    && map.get(NotificationData.LIP_CONTACT).equals(configuration.getLipContactEmail())
                    && map.get(NotificationData.LIP_CONTACT_WELSH).equals(configuration.getLipContactEmailWelsh())
                    && map.get(NotificationData.SPEC_UNSPEC_CONTACT).equals(configuration.getRaiseQueryLr())
                    && map.get(NotificationData.CNBC_CONTACT).equals(configuration.getRaiseQueryLr())
                    && map.get(NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC).equals(solicitorName)),
            argThat(string -> string.contains(caseData.getLegacyCaseReference()))
        );
    }
}
