package uk.gov.hmcts.reform.civil.notification.handlers;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.CertificateOfService;
import uk.gov.hmcts.reform.civil.notification.handlers.caseproceedsincaseman.CaseProceedsInCasemanNotifier;
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
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASEMAN_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.TWO_RESPONDENT_REPRESENTATIVES;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_NOTIFIED;

@ExtendWith(MockitoExtension.class)
public class CaseProceedsInCasemanNotifierTest {

    public static final Long CASE_ID = 1594901956117591L;

    @InjectMocks
    private CaseProceedsInCasemanNotifier caseProceedsInCasemanNotifier;

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
    @Mock
    NotificationService notificationService;

    @BeforeEach
    public void setUp() {
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(stateFlow);
        when(organisationService.findOrganisationById(anyString()))
            .thenReturn(Optional.of(Organisation.builder().name("org name").build()));
    }

    @Test
    void shouldNotifyApplicantAndRespondentSolicitor_whenInvoked() {
        CaseData caseData = CaseDataBuilder.builder().atStateProceedsOfflineAdmissionOrCounterClaim().build();

        when(stateFlow.isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)).thenReturn(false);
        when(notificationsProperties.getSolicitorCaseTakenOffline()).thenReturn("template-id");
        when(stateFlowEngine.hasTransitionedTo(caseData, CLAIM_NOTIFIED)).thenReturn(true);

        caseProceedsInCasemanNotifier.notifyParties(caseData, "eventID", "taskID");

        verify(notificationService).sendMail(
            "applicantsolicitor@example.com",
            "template-id",
            getNotificationDataMap(),
            "case-proceeds-in-caseman-applicant-notification-000DC001"
        );

        verify(notificationService).sendMail(
            "respondentsolicitor@example.com",
            "template-id",
            getNotificationDataMap(),
            "case-proceeds-in-caseman-respondent-notification-000DC001"
        );
    }

    @Test
    void shouldNotifyRespondentSolicitor2_whenInvoked() {
        final CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimDetailsNotified_1v2_andNotifyBothSolicitors()
            .build();

        when(stateFlow.isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)).thenReturn(true);
        when(notificationsProperties.getSolicitorCaseTakenOffline()).thenReturn("template-id");
        when(stateFlowEngine.hasTransitionedTo(caseData, CLAIM_NOTIFIED)).thenReturn(true);

        caseProceedsInCasemanNotifier.notifyParties(caseData, "eventID", "taskID");

        Map<String, String> parameters = getNotificationDataMap();
        parameters.put(PARTY_REFERENCES, "Claimant reference: 12345 - Defendant 1 reference: 6789 - Defendant 2 reference: 01234");

        verify(notificationService).sendMail(
            "applicantsolicitor@example.com",
            "template-id",
            parameters,
            "case-proceeds-in-caseman-applicant-notification-000DC001"
        );

        verify(notificationService).sendMail(
            "respondentsolicitor@example.com",
            "template-id",
            parameters,
            "case-proceeds-in-caseman-respondent-notification-000DC001"
        );
    }

    @Test
    void shouldNotifyApplicantLipAndRespondentSolicitor_whenInvoked() {
        final CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimNotified1v1LiP(
                CertificateOfService.builder()
                    .build()
            ).applicant1Represented(NO)
            .respondent1Represented(YES)
            .respondent1OrganisationPolicy(
                OrganisationPolicy.builder()
                    .organisation(uk.gov.hmcts.reform.ccd.model.Organisation.builder().organisationID("orgID").build())
                    .build())
            .build();

        when(stateFlow.isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)).thenReturn(false);
        when(notificationsProperties.getSolicitorCaseTakenOfflineForSpec()).thenReturn("respondent-template-id");
        when(notificationsProperties.getClaimantLipClaimUpdatedTemplate()).thenReturn("applicant-template-id");
        when(stateFlowEngine.hasTransitionedTo(caseData, CLAIM_NOTIFIED)).thenReturn(true);

        caseProceedsInCasemanNotifier.notifyParties(caseData, "eventID", "taskID");

        verify(notificationService).sendMail(
            "rambo@email.com",
            "applicant-template-id",
            getNotificationDataMapLip(),
            "case-proceeds-in-caseman-applicant-notification-000DC001"
        );

        verify(notificationService).sendMail(
            "respondentsolicitor@example.com",
            "respondent-template-id",
            getNotificationDataMap(),
            "case-proceeds-in-caseman-respondent-notification-000DC001"
        );
    }

    @Test
    void shouldNotifyApplicantBilingualLipAndRespondentSolicitor_whenInvoked() {
        final CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimNotified1v1LiP(
                CertificateOfService.builder()
                    .build()
            ).applicant1Represented(NO)
            .claimantBilingualLanguagePreference(Language.BOTH.toString())
            .respondent1Represented(YES)
            .respondent1OrganisationPolicy(
                OrganisationPolicy.builder()
                    .organisation(uk.gov.hmcts.reform.ccd.model.Organisation.builder().organisationID("orgID").build())
                    .build())
            .build();

        when(stateFlow.isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)).thenReturn(false);
        when(notificationsProperties.getSolicitorCaseTakenOfflineForSpec()).thenReturn("respondent-template-id");
        when(notificationsProperties.getClaimantLipClaimUpdatedBilingualTemplate()).thenReturn("applicant-template-id");
        when(stateFlowEngine.hasTransitionedTo(caseData, CLAIM_NOTIFIED)).thenReturn(true);

        caseProceedsInCasemanNotifier.notifyParties(caseData, "eventID", "taskID");

        verify(notificationService).sendMail(
            "rambo@email.com",
            "applicant-template-id",
            getNotificationDataMapLip(),
            "case-proceeds-in-caseman-applicant-notification-000DC001"
        );

        verify(notificationService).sendMail(
            "respondentsolicitor@example.com",
            "respondent-template-id",
            getNotificationDataMap(),
            "case-proceeds-in-caseman-respondent-notification-000DC001"
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
    private Map<String, String> getNotificationDataMapLip() {
        return new HashMap<>(Map.of(
            CLAIM_REFERENCE_NUMBER, CASE_ID.toString(),
            CLAIMANT_NAME, "Mr. John Rambo"
        ));
    }
}
