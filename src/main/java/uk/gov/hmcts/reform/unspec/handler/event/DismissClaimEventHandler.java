package uk.gov.hmcts.reform.unspec.handler.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.unspec.event.DismissClaimEvent;
import uk.gov.hmcts.reform.unspec.service.CoreCaseDataService;

import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.DISMISS_CLAIM;

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
