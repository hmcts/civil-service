package uk.gov.hmcts.reform.civil.handler.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.civil.event.CoscApplicationProcessorEvent;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.PROCESS_COSC_APPLICATION;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CoscApplicationProcessorEventHandlerTest {

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @InjectMocks
    private CoscApplicationProcessorEventHandler handler;

    @Test
    void shouldCallMoveToDecisionOutcomeEventWithExpectedParams_WhenDismissClaimEvent() {
        CoscApplicationProcessorEvent event = new CoscApplicationProcessorEvent(1L);

        handler.processCoscApplication(event);

        verify(coreCaseDataService).triggerEvent(event.getCaseId(), PROCESS_COSC_APPLICATION);
    }

}
