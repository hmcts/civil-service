package uk.gov.hmcts.reform.civil.handler.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.event.DismissClaimEvent;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DISMISS_CLAIM;

@Slf4j
@Service
@RequiredArgsConstructor
public class DismissClaimEventHandler {

    private final CoreCaseDataService coreCaseDataService;

    @EventListener
    public void moveCaseToStruckOut(DismissClaimEvent event) {
        coreCaseDataService.triggerEvent(event.getCaseId(), DISMISS_CLAIM);
    }
}
