package uk.gov.hmcts.reform.civil.handler.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.event.ManageStayWATaskEvent;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MANAGE_STAY_WA;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManageStayWATaskEventHandler {

    private final CoreCaseDataService coreCaseDataService;

    @EventListener
    public void triggerManageStayWaEvent(ManageStayWATaskEvent event) {
        coreCaseDataService.triggerEvent(event.getCaseId(), MANAGE_STAY_WA);
    }
}
