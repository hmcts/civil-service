package uk.gov.hmcts.reform.civil.handler.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.event.HearingFeePaidEvent;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.HEARING_FEE_PAID;

@ExtendWith(SpringExtension.class)
class HearingFeePaidEventHandlerTest {

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @InjectMocks
    private HearingFeePaidEventHandler handler;

    @Test
    void shouldCallTriggerEventWithExpectedParams_WhenDismissClaimEvent() {
        HearingFeePaidEvent event = new HearingFeePaidEvent(1L);

        handler.moveCaseToPrepareForHearing(event);

        verify(coreCaseDataService).triggerEvent(event.getCaseId(), HEARING_FEE_PAID);
    }

}
