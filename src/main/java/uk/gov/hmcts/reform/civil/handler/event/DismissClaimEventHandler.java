package uk.gov.hmcts.reform.civil.handler.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.event.CloseApplicationsEvent;
import uk.gov.hmcts.reform.civil.event.DismissClaimEvent;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DISMISS_CLAIM;

@Slf4j
@Service
@RequiredArgsConstructor
public class DismissClaimEventHandler {

    private final CoreCaseDataService coreCaseDataService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @EventListener
    public void moveCaseToStruckOut(DismissClaimEvent event) {
        coreCaseDataService.triggerEvent(event.getCaseId(), DISMISS_CLAIM);
        applicationEventPublisher.publishEvent(new CloseApplicationsEvent(event.getCaseId()));
    }
}
