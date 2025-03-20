package uk.gov.hmcts.reform.civil.notification.handlers;

import com.launchdarkly.shaded.org.jetbrains.annotations.NotNull;
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
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASEMAN_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.TWO_RESPONDENT_REPRESENTATIVES;

@ExtendWith(MockitoExtension.class)
public class ClaimDismissedNotifierTest {

    public static final Long CASE_ID = 1594901956117591L;
    public static final String TASK_ID = "ClaimDismissedNotifier";

    @InjectMocks
    private ClaimDismissedNotifier claimDismissedNotifier;

    @Mock
    NotificationService notificationService;
    @Mock
    private NotificationsProperties notificationsProperties;
    @Mock
    private OrganisationService organisationService;
    @Mock
    private SimpleStateFlowEngine stateFlowEngine;
    @Mock
    private StateFlow stateFlow;

    @BeforeEach
    public void setUp() {
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(stateFlow);
    }

    @Test
    void shouldNotifyApplicantAndRespondentSolicitor_whenInvoked() {
        final CaseData caseData = CaseDataBuilder.builder().atStateClaimDismissedPastClaimNotificationDeadline().build();

        when(stateFlow.isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)).thenReturn(false);
        when(organisationService.findOrganisationById(anyString()))
            .thenReturn(Optional.of(Organisation.builder().name("org name").build()));
        when(notificationsProperties.getSolicitorClaimDismissedWithinDeadline()).thenReturn("template-id");

        claimDismissedNotifier.notifyParties(caseData, "event", "task");

        verify(notificationService).sendMail(
            "applicantsolicitor@example.com",
            "template-id",
            getNotificationDataMap(),
            "claim-dismissed-applicant-notification-000DC001"
        );

        verify(notificationService).sendMail(
            "respondentsolicitor@example.com",
            "template-id",
            getNotificationDataMap(),
            "claim-dismissed-respondent-notification-000DC001"
        );
    }

    @Test
    void shouldNotifyRespondentSolicitor2_whenInvoked() {
        final CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified_1v2_andNotifyBothSolicitors().build();

        when(stateFlow.isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)).thenReturn(true);
        when(organisationService.findOrganisationById(anyString()))
            .thenReturn(Optional.of(Organisation.builder().name("org name").build()));
        when(notificationsProperties.getSolicitorClaimDismissedWithinDeadline()).thenReturn("template-id");

        claimDismissedNotifier.notifyParties(caseData, "event", "task");

        Map<String, String> parameters = getNotificationDataMap();
        parameters.put(PARTY_REFERENCES, "Claimant reference: 12345 - Defendant 1 reference: 6789 - Defendant 2 reference: 01234");

        verify(notificationService).sendMail(
            "applicantsolicitor@example.com",
            "template-id",
            parameters,
            "claim-dismissed-applicant-notification-000DC001"
        );

        verify(notificationService).sendMail(
            "respondentsolicitor@example.com",
            "template-id",
            parameters,
            "claim-dismissed-respondent-notification-000DC001"
        );

        verify(notificationService).sendMail(
            "respondentsolicitor2@example.com",
            "template-id",
            parameters,
            "claim-dismissed-respondent-notification-000DC001"
        );
    }

    @NotNull
    private Map<String, String> getNotificationDataMap() {
        return new HashMap<>(Map.of(
            CLAIM_REFERENCE_NUMBER, CASE_ID.toString(),
            PARTY_REFERENCES, "Claimant reference: 12345 - Defendant reference: 6789",
            CLAIM_LEGAL_ORG_NAME_SPEC, "org name",
            CASEMAN_REF, "000DC001"
        ));
    }
}
