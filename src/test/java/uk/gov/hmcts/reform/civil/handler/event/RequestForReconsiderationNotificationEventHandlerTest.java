package uk.gov.hmcts.reform.civil.handler.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.event.RequestForReconsiderationNotificationDeadlineEvent;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.REQUEST_FOR_RECONSIDERATION_DEADLINE_CHECK;

@ExtendWith(SpringExtension.class)
class RequestForReconsiderationNotificationEventHandlerTest {

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @InjectMocks
    private RequestForReconsiderationNotificationEventHandler handler;

    @Test
    void shouldCallTriggerEventWithExpectedParams_WhenDismissClaimEvent() {
        RequestForReconsiderationNotificationDeadlineEvent event = new RequestForReconsiderationNotificationDeadlineEvent(1L);

        handler.triggerNotificationDeletionProcess(event);

        verify(coreCaseDataService).triggerEvent(event.getCaseId(), REQUEST_FOR_RECONSIDERATION_DEADLINE_CHECK);
    }

}
