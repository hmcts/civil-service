package uk.gov.hmcts.reform.civil.handler.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.event.DecisionOutcomeEvent;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MOVE_TO_DECISION_OUTCOME;

@Slf4j
@Service
@RequiredArgsConstructor
public class DecisionOutcomeEventHandler {

    private final CoreCaseDataService coreCaseDataService;

    @EventListener
    public void moveCaseToDecisionOutcome(DecisionOutcomeEvent event) {
        coreCaseDataService.triggerEvent(event.getCaseId(), MOVE_TO_DECISION_OUTCOME);
    }
}
