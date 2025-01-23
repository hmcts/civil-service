package uk.gov.hmcts.reform.civil.notification.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.TWO_RESPONDENT_REPRESENTATIVES;

@ExtendWith(MockitoExtension.class)
class AddDefendantLitigationFriendNotificationHandlerTest {

    public static final Long CASE_ID = 1594901956117591L;
    @Mock
    NotificationService notificationService;
    @InjectMocks
    AddDefendantLitigationFriendNotificationHandler addDefendantLitigationFriendNotificationHandler;
    @Mock
    private NotificationsProperties notificationsProperties;
    @Mock
    private OrganisationService organisationService;
    @Mock
    private SimpleStateFlowEngine stateFlowEngine;

    @Test
    void shouldNotifyApplicantSolicitor_whenInvoked() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();

        StateFlow stateFlow = mock(StateFlow.class);
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(stateFlow);
        when(stateFlow.isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)).thenReturn(true);

        addDefendantLitigationFriendNotificationHandler.notifyParties(caseData);
    }
}