package uk.gov.hmcts.reform.civil.handler.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.event.FullAdmitPayImmediatelyNoPaymentFromDefendantEvent;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CLAIMANT_RESPONSE_FA_IMMEDIATE_CCJ;

@Slf4j
@Service
@RequiredArgsConstructor
public class FullAdmitPayImmediatelyNoPaymentFromDefendantEventHandler {

    private final CoreCaseDataService coreCaseDataService;

    @EventListener
    public void createClaimantDashboardScenario(FullAdmitPayImmediatelyNoPaymentFromDefendantEvent event) {
        coreCaseDataService.triggerEvent(event.caseId(), CLAIMANT_RESPONSE_FA_IMMEDIATE_CCJ);
    }
}
