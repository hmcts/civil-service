package uk.gov.hmcts.reform.civil.handler.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.event.HearingFeeUnpaidEvent;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.HEARING_FEE_UNPAID;

@ExtendWith(SpringExtension.class)
class HearingFeeUnpaidEventHandlerTest {

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @InjectMocks
    private HearingFeeUnpaidEventHandler handler;

    @Test
    void shouldCallTriggerEventWithExpectedParams_WhenDismissClaimEvent() {
        //Given: HearingFeeUnpaidEvent with caseId 1
        HearingFeeUnpaidEvent event = new HearingFeeUnpaidEvent(1L);
        //When: moveCaseToStruckOut is called with HearingFeeUnpaidEvent
        handler.moveCaseToStruckOut(event);
        //Then: it should trigger HEARING_FEE_UNPAID event
        verify(coreCaseDataService).triggerEvent(event.getCaseId(), HEARING_FEE_UNPAID);
    }

}
