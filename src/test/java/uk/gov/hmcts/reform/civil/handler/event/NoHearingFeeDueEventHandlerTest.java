package uk.gov.hmcts.reform.civil.handler.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.event.NoHearingFeeDueEvent;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NO_HEARING_FEE_DUE;

@ExtendWith(SpringExtension.class)
class NoHearingFeeDueEventHandlerTest {

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @InjectMocks
    private NoHearingFeeDueEventHandler handler;

    @Test
    void shouldCallTriggerEventWithExpectedParams_WhenDismissClaimEvent() {
        NoHearingFeeDueEvent event = new NoHearingFeeDueEvent(1L);

        handler.moveCaseToPrepareForHearing(event);

        verify(coreCaseDataService).triggerEvent(event.getCaseId(), NO_HEARING_FEE_DUE);
    }

}
