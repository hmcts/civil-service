package uk.gov.hmcts.reform.civil.handler.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.event.DefendantResponseDeadlineCheckEvent;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFENDANT_RESPONSE_DEADLINE_CHECK;

@ExtendWith(MockitoExtension.class)
class DefendantResponseDeadlineCheckEventHandlerTest {

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @InjectMocks
    private DefendantResponseDeadlineCheckEventHandler handler;

    @Test
    void shouldTriggerResponseDeadlineCheckOnResponseDeadlineCheckEvent() {
        Long caseId = 1633357679902210L;
        DefendantResponseDeadlineCheckEvent event = new DefendantResponseDeadlineCheckEvent(caseId);

        handler.checkForDefendantResponseDeadline(event);

        verify(coreCaseDataService).triggerEvent(event.getCaseId(), DEFENDANT_RESPONSE_DEADLINE_CHECK);
    }

}
