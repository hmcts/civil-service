package uk.gov.hmcts.reform.civil.handler.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.event.OrderReviewObligationCheckEvent;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.ORDER_REVIEW_OBLIGATION_CHECK;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderReviewObligationCheckEventHandler {

    private final CoreCaseDataService coreCaseDataService;

    @EventListener
    public void checkForOrderReviewObligation(OrderReviewObligationCheckEvent event) {
        coreCaseDataService.triggerEvent(event.getCaseId(), ORDER_REVIEW_OBLIGATION_CHECK);
    }
}
