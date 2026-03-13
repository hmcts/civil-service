package uk.gov.hmcts.reform.civil.handler.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.civil.event.NoHearingFeeDueEvent;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NO_HEARING_FEE_DUE;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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
