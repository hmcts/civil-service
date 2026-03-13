package uk.gov.hmcts.reform.civil.handler.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.civil.event.DismissClaimEvent;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DISMISS_CLAIM;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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
