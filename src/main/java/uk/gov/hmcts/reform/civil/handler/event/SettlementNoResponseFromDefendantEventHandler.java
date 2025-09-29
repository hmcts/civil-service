package uk.gov.hmcts.reform.civil.handler.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.event.SettlementNoResponseFromDefendantEvent;

@Service
@RequiredArgsConstructor
public class SettlementNoResponseFromDefendantEventHandler {

    private final SettlementNoResponseFromDefendantEventProcessor settlementNoResponseFromDefendantEventProcessor;

    @EventListener
    public void createClaimantDashboardScenario(SettlementNoResponseFromDefendantEvent event) {
        settlementNoResponseFromDefendantEventProcessor.createClaimantDashboardScenario(event.getCaseId());
    }
}
