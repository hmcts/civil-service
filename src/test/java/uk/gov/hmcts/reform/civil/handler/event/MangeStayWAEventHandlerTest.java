package uk.gov.hmcts.reform.civil.handler.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.event.ManageStayWATaskEvent;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MANAGE_STAY_WA;

@ExtendWith(SpringExtension.class)
class MangeStayWAEventHandlerTest {

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @InjectMocks
    private ManageStayWATaskEventHandler handler;

    @Test
    void shouldCallTriggerEventWithExpectedParams_WhenManageStayWaTaskEvent() {
        ManageStayWATaskEvent event = new ManageStayWATaskEvent(1L);

        handler.triggerManageStayWaEvent(event);

        verify(coreCaseDataService).triggerEvent(event.getCaseId(), MANAGE_STAY_WA);
    }

}
