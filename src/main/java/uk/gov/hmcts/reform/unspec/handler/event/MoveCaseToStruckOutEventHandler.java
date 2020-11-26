package uk.gov.hmcts.reform.unspec.handler.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.unspec.event.MoveCaseToStuckOutEvent;
import uk.gov.hmcts.reform.unspec.service.CoreCaseDataService;

import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.MOVE_CLAIM_TO_STRUCK_OUT;

@Slf4j
@Service
@RequiredArgsConstructor
public class MoveCaseToStruckOutEventHandler {

    private final CoreCaseDataService coreCaseDataService;

    @EventListener
    public void moveCaseToStruckOut(MoveCaseToStuckOutEvent event) {
        coreCaseDataService.triggerEvent(event.getCaseId(), MOVE_CLAIM_TO_STRUCK_OUT);
    }
}
