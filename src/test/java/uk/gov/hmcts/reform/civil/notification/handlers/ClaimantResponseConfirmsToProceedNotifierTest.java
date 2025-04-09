package uk.gov.hmcts.reform.civil.notification.handlers;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
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
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_EVENT;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.MULTI_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.APPLICANT_ONE_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASEMAN_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;
import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.ClaimantConfirmsToProceedNotify;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.TWO_RESPONDENT_REPRESENTATIVES;

@ExtendWith(MockitoExtension.class)
public class ClaimantResponseConfirmsToProceedNotifierTest {

    public static final Long CASE_ID = 1594901956117591L;

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
    @InjectMocks
    ClaimantResponseConfirmsToProceedNotifier claimantResponseConfirmsToProceedNotifier;

    @BeforeEach
    public void setUp() {
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(stateFlow);
    }

    @Test
    void shouldNotifyApplicantAndRespondentSolicitorUnspecNotMultiClaim_whenInvoked() {
        final CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();

        when(stateFlow.isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)).thenReturn(false);
        when(notificationsProperties.getClaimantSolicitorConfirmsToProceed()).thenReturn("template-id");
        when(organisationService.findOrganisationById(anyString())).thenReturn(Optional.of(Organisation.builder().name("org name").build()));

        claimantResponseConfirmsToProceedNotifier.notifyParties(caseData, NOTIFY_EVENT.toString(), ClaimantConfirmsToProceedNotify.toString());

        verify(notificationService).sendMail(
            "applicantsolicitor@example.com",
            "template-id",
            getNotificationDataMap(),
            "claimant-confirms-to-proceed-respondent-notification-000DC001"
        );

        verify(notificationService).sendMail(
            "respondentsolicitor@example.com",
            "template-id",
            getNotificationDataMap(),
            "claimant-confirms-to-proceed-respondent-notification-000DC001"
        );
    }

    @Test
    void shouldNotifyApplicantAndRespondentSolicitorUnspecMultiClaimNotProceed_whenInvoked() {
        final CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
            .applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2(NO)
            .applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2(NO)
            .build();

        when(stateFlow.isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)).thenReturn(true);
        when(notificationsProperties.getClaimantSolicitorConfirmsNotToProceed()).thenReturn("template-id");
        when(organisationService.findOrganisationById(anyString())).thenReturn(Optional.of(Organisation.builder().name("org name").build()));

        claimantResponseConfirmsToProceedNotifier.notifyParties(caseData, NOTIFY_EVENT.toString(), ClaimantConfirmsToProceedNotify.toString());

        verify(notificationService).sendMail(
            "applicantsolicitor@example.com",
            "template-id",
            getNotificationDataMap(),
            "claimant-confirms-not-to-proceed-respondent-notification-000DC001"
        );

        verify(notificationService).sendMail(
            "respondentsolicitor@example.com",
            "template-id",
            getNotificationDataMap(),
            "claimant-confirms-not-to-proceed-respondent-notification-000DC001"
        );

        verify(notificationService).sendMail(
            "respondentsolicitor2@example.com",
            "template-id",
            getNotificationDataMap(),
            "claimant-confirms-not-to-proceed-respondent-notification-000DC001"
        );
    }

    @Test
    void shouldNotifyApplicantAndRespondentSolicitorSpecProceedWithAction_whenInvoked() {
        final CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
            .respondent1ClaimResponseTypeForSpec(FULL_DEFENCE)
            .caseAccessCategory(SPEC_CLAIM)
            .build().toBuilder()
            .responseClaimMediationSpecRequired(NO)
            .build();

        when(stateFlow.isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)).thenReturn(false);
        when(notificationsProperties.getClaimantSolicitorConfirmsToProceedSpecWithAction()).thenReturn("applicant-template-id");
        when(notificationsProperties.getRespondentSolicitorNotifyToProceedSpecWithAction()).thenReturn("respondent-template-id");
        when(organisationService.findOrganisationById(anyString())).thenReturn(Optional.of(Organisation.builder().name("org name").build()));

        claimantResponseConfirmsToProceedNotifier.notifyParties(caseData, NOTIFY_EVENT.toString(), ClaimantConfirmsToProceedNotify.toString());

        verify(notificationService).sendMail(
            "applicantsolicitor@example.com",
            "applicant-template-id",
            getNotificationDataMapSpec(),
            "claimant-confirms-to-proceed-respondent-notification-000DC001"
        );

        verify(notificationService).sendMail(
            "respondentsolicitor@example.com",
            "respondent-template-id",
            getNotificationDataMapSpec(),
            "claimant-confirms-to-proceed-respondent-notification-000DC001"
        );
    }

    @Test
    void shouldNotifyApplicantAndRespondentSolicitorSpecProceed_whenInvoked() {
        final CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
            .respondent1ClaimResponseTypeForSpec(FULL_DEFENCE)
            .caseAccessCategory(SPEC_CLAIM)
            .build();

        when(stateFlow.isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)).thenReturn(false);
        when(notificationsProperties.getClaimantSolicitorConfirmsToProceedSpec()).thenReturn("applicant-template-id");
        when(notificationsProperties.getRespondentSolicitorNotifyToProceedSpec()).thenReturn("respondent-template-id");
        when(organisationService.findOrganisationById(anyString())).thenReturn(Optional.of(Organisation.builder().name("org name").build()));

        claimantResponseConfirmsToProceedNotifier.notifyParties(caseData, NOTIFY_EVENT.toString(), ClaimantConfirmsToProceedNotify.toString());

        verify(notificationService).sendMail(
            "applicantsolicitor@example.com",
            "applicant-template-id",
            getNotificationDataMapSpec(),
            "claimant-confirms-to-proceed-respondent-notification-000DC001"
        );

        verify(notificationService).sendMail(
            "respondentsolicitor@example.com",
            "respondent-template-id",
            getNotificationDataMapSpec(),
            "claimant-confirms-to-proceed-respondent-notification-000DC001"
        );
    }

    @Test
    void shouldNotifyApplicantAndRespondentSolicitorUnspecMultiOrIntermediateTrackEnabled_whenInvoked() {
        final CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimDetailsNotified()
            .build().toBuilder()
            .allocatedTrack(MULTI_CLAIM).build();

        when(stateFlow.isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)).thenReturn(false);
        when(notificationsProperties.getSolicitorCaseTakenOffline()).thenReturn("template-id");
        when(organisationService.findOrganisationById(anyString())).thenReturn(Optional.of(Organisation.builder().name("org name").build()));
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(false);

        claimantResponseConfirmsToProceedNotifier.notifyParties(caseData, NOTIFY_EVENT.toString(), ClaimantConfirmsToProceedNotify.toString());

        verify(notificationService).sendMail(
            "applicantsolicitor@example.com",
            "template-id",
            getNotificationDataMap(),
            "claimant-confirms-to-proceed-respondent-notification-000DC001"
        );

        verify(notificationService).sendMail(
            "respondentsolicitor@example.com",
            "template-id",
            getNotificationDataMap(),
            "claimant-confirms-to-proceed-respondent-notification-000DC001"
        );
    }

    @Test
    void shouldNotifyRespondentLip_whenInvoked() {
        final CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheckLiP(true)
            .respondent2(null)
            .caseAccessCategory(SPEC_CLAIM)
            .applicant1Represented(NO).build();

        when(stateFlow.isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)).thenReturn(false);
        when(notificationsProperties.getRespondent1LipClaimUpdatedTemplate()).thenReturn("template-id");

        claimantResponseConfirmsToProceedNotifier.notifyParties(caseData, NOTIFY_EVENT.toString(), ClaimantConfirmsToProceedNotify.toString());

        verify(notificationService).sendMail(
            "sole.trader@email.com",
            "template-id",
            getNotificationDataMapLip(),
            "claimant-confirms-to-proceed-respondent-notification-000MC001"
        );
    }

    @NotNull
    private Map<String, String> getNotificationDataMap() {
        return new HashMap<>(Map.of(
            CLAIM_REFERENCE_NUMBER, CASE_ID.toString(),
            PARTY_REFERENCES, "Claimant reference: 12345 - Defendant reference: 6789",
            CASEMAN_REF, "000DC001",
            CLAIM_LEGAL_ORG_NAME_SPEC, "org name"
        ));
    }

    @NotNull
    private Map<String, String> getNotificationDataMapSpec() {
        return new HashMap<>(Map.of(
            CLAIM_REFERENCE_NUMBER, CASE_ID.toString(),
            PARTY_REFERENCES, "Claimant reference: 12345 - Defendant reference: 6789",
            CASEMAN_REF, "000DC001",
            CLAIM_LEGAL_ORG_NAME_SPEC, "org name",
            RESPONDENT_NAME, "Mr. Sole Trader",
            APPLICANT_ONE_NAME, "Mr. John Rambo"
        ));
    }

    @NotNull
    private Map<String, String> getNotificationDataMapLip() {
        return new HashMap<>(Map.of(
            CLAIM_REFERENCE_NUMBER, CASE_ID.toString(),
            RESPONDENT_NAME, "Mr. Sole Trader",
            PARTY_REFERENCES, "Claimant reference: 12345 - Defendant reference: 6789",
            CASEMAN_REF, "000MC001"
        ));
    }
}
