package uk.gov.hmcts.reform.civil.handler.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.event.TrialReadyCheckEvent;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.TRIAL_READY_CHECK;

@ExtendWith(MockitoExtension.class)
class TrialReadyCheckEventHandlerTest {

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @InjectMocks
    private TrialReadyCheckEventHandler handler;

    @Test
    void shouldTakeCaseOfflineOnTrialReadyCheckEvent() {
        Long caseId = 1633357679902210L;
        TrialReadyCheckEvent event = new TrialReadyCheckEvent(caseId);

        handler.checkForTrialReady(event);

        verify(coreCaseDataService).triggerEvent(event.getCaseId(), TRIAL_READY_CHECK);
    }

}
