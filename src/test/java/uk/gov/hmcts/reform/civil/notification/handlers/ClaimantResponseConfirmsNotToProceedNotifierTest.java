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
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASEMAN_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_DEFENDANT_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.ResponseOneVOneShowTag.ONE_V_ONE_PART_ADMIT_PAY_IMMEDIATELY;
import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.ClaimantResponseConfirmsNotToProceedNotify;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.TWO_RESPONDENT_REPRESENTATIVES;

@ExtendWith(MockitoExtension.class)
public class ClaimantResponseConfirmsNotToProceedNotifierTest {

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
    @InjectMocks
    private ClaimantResponseConfirmsNotToProceedNotifier claimantResponseConfirmsNotToProceedNotifier;

    @BeforeEach
    public void setUp() {
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(stateFlow);
    }

    @Test
    void shouldNotifyApplicantAndRespondentSolicitorUnspec_whenInvoked() {
        final CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();

        when(stateFlow.isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)).thenReturn(false);
        when(notificationsProperties.getClaimantSolicitorConfirmsNotToProceed()).thenReturn("template-id");

        claimantResponseConfirmsNotToProceedNotifier.notifyParties(caseData, NOTIFY_EVENT.toString(), ClaimantResponseConfirmsNotToProceedNotify.toString());

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
    }

    @Test
    void shouldNotifyRespondentSolicitor2_whenInvoked() {
        final CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();

        when(stateFlow.isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)).thenReturn(true);
        when(notificationsProperties.getClaimantSolicitorConfirmsNotToProceed()).thenReturn("template-id");

        claimantResponseConfirmsNotToProceedNotifier.notifyParties(caseData, NOTIFY_EVENT.toString(), ClaimantResponseConfirmsNotToProceedNotify.toString());

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
    void shouldNotifyApplicantAndRespondentSolicitorSpec_whenInvoked() {
        final CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().caseAccessCategory(SPEC_CLAIM).build();

        when(stateFlow.isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)).thenReturn(false);
        when(notificationsProperties.getClaimantSolicitorConfirmsNotToProceedSpec()).thenReturn("claimant-template-id");
        when(notificationsProperties.getRespondentSolicitorNotifyNotToProceedSpec()).thenReturn("respondent-template-id");
        when(organisationService.findOrganisationById(anyString()))
            .thenReturn(Optional.of(Organisation.builder().name("org name").build()));

        claimantResponseConfirmsNotToProceedNotifier.notifyParties(caseData, NOTIFY_EVENT.toString(), ClaimantResponseConfirmsNotToProceedNotify.toString());

        verify(notificationService).sendMail(
            "applicantsolicitor@example.com",
            "claimant-template-id",
            getNotificationDataMapSpec(),
            "claimant-confirms-not-to-proceed-respondent-notification-000DC001"
        );

        verify(notificationService).sendMail(
            "respondentsolicitor@example.com",
            "respondent-template-id",
            getNotificationDataMapSpec(),
            "claimant-confirms-not-to-proceed-respondent-notification-000DC001"
        );
    }

    @Test
    void shouldNotifyApplicantAndRespondentSolicitorSpecPayImmediatelyAccepted_whenInvoked() {
        final CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
            .caseAccessCategory(SPEC_CLAIM)
            .applicant1AcceptAdmitAmountPaidSpec(YES)
            .showResponseOneVOneFlag(ONE_V_ONE_PART_ADMIT_PAY_IMMEDIATELY)
            .build();

        when(stateFlow.isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)).thenReturn(false);
        when(notificationsProperties.getNotifyRespondentSolicitorPartAdmitPayImmediatelyAcceptedSpec()).thenReturn("template-id");
        when(organisationService.findOrganisationById(anyString()))
            .thenReturn(Optional.of(Organisation.builder().name("org name").build()));

        claimantResponseConfirmsNotToProceedNotifier.notifyParties(caseData, NOTIFY_EVENT.toString(), ClaimantResponseConfirmsNotToProceedNotify.toString());

        verify(notificationService).sendMail(
            "applicantsolicitor@example.com",
            "template-id",
            getNotificationDataMapSpecPayImmediatelyAccepted(),
            "claimant-confirms-not-to-proceed-respondent-notification-000DC001"
        );

        verify(notificationService).sendMail(
            "respondentsolicitor@example.com",
            "template-id",
            getNotificationDataMapSpecPayImmediatelyAccepted(),
            "claimant-confirms-not-to-proceed-respondent-notification-000DC001"
        );
    }

    @NotNull
    private Map<String, String> getNotificationDataMap() {
        return new HashMap<>(Map.of(
            CLAIM_REFERENCE_NUMBER, CASE_ID.toString(),
            PARTY_REFERENCES, "Claimant reference: 12345 - Defendant reference: 6789",
            CASEMAN_REF, "000DC001"
        ));
    }

    @NotNull
    private Map<String, String> getNotificationDataMapSpecPayImmediatelyAccepted() {
        return new HashMap<>(Map.of(
            CLAIM_REFERENCE_NUMBER, CASE_ID.toString(),
            PARTY_REFERENCES, "Claimant reference: 12345 - Defendant reference: 6789",
            CASEMAN_REF, "000DC001",
            CLAIM_DEFENDANT_LEGAL_ORG_NAME_SPEC, "org name",
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
            RESPONDENT_NAME, "Mr. Sole Trader"
        ));
    }
}
