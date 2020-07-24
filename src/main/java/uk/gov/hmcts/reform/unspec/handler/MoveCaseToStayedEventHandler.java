package uk.gov.hmcts.reform.unspec.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.unspec.event.MoveCaseToStayedEvent;
import uk.gov.hmcts.reform.unspec.service.CoreCaseDataService;

import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.MOVE_TO_STAYED;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MoveCaseToStayedEventHandler {

    private final CoreCaseDataService coreCaseDataService;

    @EventListener
    public void moveCaseToStayed(MoveCaseToStayedEvent event) {
        coreCaseDataService.triggerEvent(event.getCaseId(), MOVE_TO_STAYED);
    }
}
