package uk.gov.hmcts.reform.civil.handler.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.event.FullAdmitPayImmediatelyNoPaymentFromDefendantEvent;

@Slf4j
@Service
@RequiredArgsConstructor
public class FullAdmitPayImmediatelyNoPaymentFromDefendantEventHandler {

    private final FullAdmitPayImmediatelyNoPaymentFromDefendantProcessor fullAdmitPayImmediatelyNoPaymentFromDefendantProcessor;

    @EventListener
    public void createClaimantDashboardScenario(FullAdmitPayImmediatelyNoPaymentFromDefendantEvent event) {

        fullAdmitPayImmediatelyNoPaymentFromDefendantProcessor.createClaimantDashboardScenario(event.caseId());
    }
}
