package uk.gov.hmcts.reform.civil.handler.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.event.FullAdmitPayImmediatelyNoPaymentFromDefendantEvent;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CLAIMANT_RESPONSE_FA_IMMEDIATE_CCJ;

@ExtendWith(MockitoExtension.class)
class FullAdmitPayImmediatelyNoPaymentFromDefendantEventHandlerTest {

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @InjectMocks
    private FullAdmitPayImmediatelyNoPaymentFromDefendantEventHandler handler;

    static final Long CASE_ID = 1111111111111111L;

    @Test
    void shouldTriggerFullAdmitPayImmediatelyCCJEvent() {

        handler.createClaimantDashboardScenario(new FullAdmitPayImmediatelyNoPaymentFromDefendantEvent(CASE_ID));

        verify(coreCaseDataService).triggerEvent(CASE_ID, CLAIMANT_RESPONSE_FA_IMMEDIATE_CCJ);
    }
}
