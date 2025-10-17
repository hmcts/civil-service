package uk.gov.hmcts.reform.civil.handler.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.event.DeleteExpiredResponseRespondentNotificationsEvent;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.RESPONDENT_RESPONSE_DEADLINE_CHECK;

@ExtendWith(SpringExtension.class)
class DeleteExpiredResponseRespondentNotificationsEventHandlerTest {

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @InjectMocks
    private DeleteExpiredResponseRespondentNotificationsEventHandler handler;

    @Test
    void shouldCallTriggerEventWithExpectedParams_WhenDismissClaimEvent() {
        DeleteExpiredResponseRespondentNotificationsEvent event = new DeleteExpiredResponseRespondentNotificationsEvent(1L);

        handler.triggerNotificationDeletionProcess(event);

        verify(coreCaseDataService).triggerGaEvent(event.caseId(), RESPONDENT_RESPONSE_DEADLINE_CHECK);
    }

}
