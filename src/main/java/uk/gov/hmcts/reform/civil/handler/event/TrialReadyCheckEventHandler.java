package uk.gov.hmcts.reform.civil.handler.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.event.TrialReadyCheckEvent;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.TRIAL_READY_CHECK;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrialReadyCheckEventHandler {

    private final CoreCaseDataService coreCaseDataService;

    @EventListener
    public void checkForTrialReady(TrialReadyCheckEvent event) {
        coreCaseDataService.triggerEvent(event.getCaseId(), TRIAL_READY_CHECK);
    }
}
