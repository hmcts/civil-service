package uk.gov.hmcts.reform.civil.notification.handlers;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.CertificateOfService;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.notification.handlers.createsdo.CreateSDONotifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_V_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.model.Party.Type.INDIVIDUAL;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.TWO_RESPONDENT_REPRESENTATIVES;

@ExtendWith(MockitoExtension.class)
public class CreateSDONotifierTest {

    public static final Long CASE_ID = 1594901956117591L;

    @InjectMocks
    private CreateSDONotifier createSDONotifier;
    @Mock
    private NotificationService notificationService;
    @Mock
    private NotificationsProperties notificationsProperties;
    @Mock
    private OrganisationService organisationService;
    @Mock
    private SimpleStateFlowEngine stateFlowEngine;
    @Mock
    private StateFlow stateFlow;
    @Mock
    private FeatureToggleService featureToggleService;

    @BeforeEach
    public void setUp() {
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(stateFlow);
    }

    @Test
    void shouldNotifyApplicantAndRespondentSolicitorUnspecClaim_whenInvoked() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
            .caseManagementLocation(CaseLocationCivil.builder().baseLocation("baseLocation").build())
            .build();

        when(stateFlow.isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)).thenReturn(false);
        when(notificationsProperties.getSdoOrdered()).thenReturn("template-id");
        when(organisationService.findOrganisationById(anyString()))
            .thenReturn(Optional.of(Organisation.builder().name("org name").build()));

        createSDONotifier.notifyParties(caseData, "eventId", "taskId");

        Map<String, String> properties = getNotificationDataMap();

        verify(notificationService).sendMail(
            "applicantsolicitor@example.com",
            "template-id",
            properties,
            "create-sdo-applicants-notification-000DC001"
        );

        properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, "Mr. Sole Trader");

        verify(notificationService).sendMail(
            "sole.trader@email.com",
            "template-id",
            properties,
            "create-sdo-respondent-1-notification-000DC001"
        );
    }

    @Test
    void shouldNotifyRespondentSolicitor2UnspecClaim_whenInvoked() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
            .caseManagementLocation(CaseLocationCivil.builder().baseLocation("baseLocation").build())
            .respondent2(Party.builder().individualFirstName("defendant").individualLastName("2").partyEmail("defendant2@email.com").type(INDIVIDUAL).build())
            .build();

        when(stateFlow.isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)).thenReturn(true);
        when(notificationsProperties.getSdoOrdered()).thenReturn("template-id");
        when(organisationService.findOrganisationById(anyString()))
            .thenReturn(Optional.of(Organisation.builder().name("org name").build()));

        createSDONotifier.notifyParties(caseData, "eventId", "taskId");

        Map<String, String> properties = getNotificationDataMap();
        properties.put(PARTY_REFERENCES, "Claimant reference: 12345 - Defendant 1 reference: 6789 - Defendant 2 reference: Not provided");

        verify(notificationService).sendMail(
            "applicantsolicitor@example.com",
            "template-id",
            properties,
            "create-sdo-applicants-notification-000DC001"
        );

        properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, "Mr. Sole Trader");

        verify(notificationService).sendMail(
            "sole.trader@email.com",
            "template-id",
            properties,
            "create-sdo-respondent-1-notification-000DC001"
        );

        properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, "defendant 2");

        verify(notificationService).sendMail(
            "defendant2@email.com",
            "template-id",
            properties,
            "create-sdo-respondent-2-notification-000DC001"
        );
    }

    @Test
    void shouldNotifyApplicantAndRespondentSolicitorSpecClaim_whenInvoked() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
            .caseManagementLocation(CaseLocationCivil.builder().baseLocation("baseLocation").build())
            .caseAccessCategory(SPEC_CLAIM)
            .build();

        when(stateFlow.isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)).thenReturn(false);
        when(notificationsProperties.getSdoOrderedSpec()).thenReturn("template-id");
        when(featureToggleService.isCaseProgressionEnabledAndLocationWhiteListed(any())).thenReturn(false);
        when(organisationService.findOrganisationById(anyString()))
            .thenReturn(Optional.of(Organisation.builder().name("org name").build()));

        createSDONotifier.notifyParties(caseData, "eventId", "taskId");

        Map<String, String> properties = getNotificationDataMap();

        verify(notificationService).sendMail(
            "applicantsolicitor@example.com",
            "template-id",
            properties,
            "create-sdo-applicants-notification-000DC001"
        );

        properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, "Mr. Sole Trader");

        verify(notificationService).sendMail(
            "sole.trader@email.com",
            "template-id",
            properties,
            "create-sdo-respondent-1-notification-000DC001"
        );
    }

    @Test
    void shouldNotifyRespondentSolicitorSpecClaimEA_whenInvoked() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
            .caseManagementLocation(CaseLocationCivil.builder().baseLocation("baseLocation").build())
            .caseAccessCategory(SPEC_CLAIM)
            .build();

        when(stateFlow.isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)).thenReturn(false);
        when(notificationsProperties.getSdoOrderedSpecEa()).thenReturn("template-id");
        when(featureToggleService.isCaseProgressionEnabledAndLocationWhiteListed(any())).thenReturn(true);
        when(organisationService.findOrganisationById(anyString()))
            .thenReturn(Optional.of(Organisation.builder().name("org name").build()));

        createSDONotifier.notifyParties(caseData, "eventId", "taskId");

        Map<String, String> properties = getNotificationDataMap();

        properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, "Mr. Sole Trader");
        verify(notificationService).sendMail(
            "sole.trader@email.com",
            "template-id",
            properties,
            "create-sdo-respondent-1-notification-000DC001"
        );
    }

    @Test
    void shouldNotifyRespondentSolicitorSpecClaimBilingual_whenInvoked() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
            .caseManagementLocation(CaseLocationCivil.builder().baseLocation("baseLocation").build())
            .caseAccessCategory(SPEC_CLAIM)
            .caseDataLip(CaseDataLiP.builder().respondent1LiPResponse(
                RespondentLiPResponse.builder().respondent1ResponseLanguage(Language.BOTH.toString()).build()).build()
            )
            .build();

        when(stateFlow.isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)).thenReturn(false);
        when(notificationsProperties.getSdoOrderedSpecBilingual()).thenReturn("template-id");
        when(organisationService.findOrganisationById(anyString()))
            .thenReturn(Optional.of(Organisation.builder().name("org name").build()));

        createSDONotifier.notifyParties(caseData, "eventId", "taskId");

        Map<String, String> properties = getNotificationDataMap();

        properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, "Mr. Sole Trader");
        verify(notificationService).sendMail(
            "sole.trader@email.com",
            "template-id",
            properties,
            "create-sdo-respondent-1-notification-000DC001"
        );
    }

    @Test
    void shouldNotifyApplicantAndRespondentLipBilingual_whenInvoked() {
        final CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimNotified1v1LiP(
                CertificateOfService.builder()
                    .build()
            )
            .caseManagementLocation(CaseLocationCivil.builder().baseLocation("baseLocation").build())
            .applicant1Represented(NO)
            .claimantBilingualLanguagePreference(Language.BOTH.toString())
            .caseDataLip(CaseDataLiP.builder().respondent1LiPResponse(
                RespondentLiPResponse.builder().respondent1ResponseLanguage(Language.BOTH.toString()).build()).build()
            )
            .claimantUserDetails(IdamUserDetails.builder().email("rambo@email.com").build())
            .build();

        when(stateFlow.isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)).thenReturn(false);
        when(notificationsProperties.getNotifyLipUpdateTemplateBilingual()).thenReturn("template-id");

        createSDONotifier.notifyParties(caseData, "eventId", "taskId");

        Map<String, String> properties = getNotificationDataMapLip();

        properties.put(PARTY_NAME, "Mr. Sole Trader");
        verify(notificationService).sendMail(
            "sole.trader@email.com",
            "template-id",
            properties,
            "create-sdo-respondent-1-notification-000DC001"
        );

        properties.put(PARTY_NAME, "Mr. John Rambo");
        verify(notificationService).sendMail(
            "rambo@email.com",
            "template-id",
            properties,
            "create-sdo-applicants-notification-000DC001"
        );
    }

    @Test
    void shouldNotifyApplicantAndRespondentLipNotBilingual_whenInvoked() {
        final CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimNotified1v1LiP(
                CertificateOfService.builder()
                    .build()
            )
            .applicant1Represented(NO)
            .caseManagementLocation(CaseLocationCivil.builder().baseLocation("baseLocation").build())
            .claimantUserDetails(IdamUserDetails.builder().email("rambo@email.com").build())
            .build();


        when(stateFlow.isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)).thenReturn(false);
        when(notificationsProperties.getNotifyLipUpdateTemplate()).thenReturn("template-id");

        createSDONotifier.notifyParties(caseData, "eventId", "taskId");

        Map<String, String> properties = getNotificationDataMapLip();

        properties.put(PARTY_NAME, "Mr. Sole Trader");
        verify(notificationService).sendMail(
            "sole.trader@email.com",
            "template-id",
            properties,
            "create-sdo-respondent-1-notification-000DC001"
        );

        properties.put(PARTY_NAME, "Mr. John Rambo");
        verify(notificationService).sendMail(
            "rambo@email.com",
            "template-id",
            properties,
            "create-sdo-applicants-notification-000DC001"
        );
    }

    @NotNull
    private Map<String, String> getNotificationDataMap() {
        return new HashMap<>(Map.of(
            CLAIM_REFERENCE_NUMBER, CASE_ID.toString(),
            PARTY_REFERENCES, "Claimant reference: 12345 - Defendant reference: 6789",
            CLAIM_LEGAL_ORG_NAME_SPEC, "org name"
        ));
    }

    @NotNull
    private Map<String, String> getNotificationDataMapLip() {
        return new HashMap<>(Map.of(
            CLAIM_REFERENCE_NUMBER, CASE_ID.toString(),
            CLAIMANT_V_DEFENDANT, "Mr. John Rambo V Mr. Sole Trader"
        ));
    }
}
