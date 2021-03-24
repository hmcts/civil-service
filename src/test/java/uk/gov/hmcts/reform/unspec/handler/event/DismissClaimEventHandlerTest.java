package uk.gov.hmcts.reform.unspec.handler.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.unspec.event.DismissClaimEvent;
import uk.gov.hmcts.reform.unspec.service.CoreCaseDataService;

import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.DISMISS_CLAIM;

@ExtendWith(SpringExtension.class)
class DismissClaimEventHandlerTest {

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @InjectMocks
    private DismissClaimEventHandler handler;

    @Test
    void shouldCallTriggerEventWithExpectedParams_WhenDismissClaimEvent() {
        DismissClaimEvent event = new DismissClaimEvent(1L);

        handler.moveCaseToStruckOut(event);

        verify(coreCaseDataService).triggerEvent(event.getCaseId(), DISMISS_CLAIM);
    }

}
