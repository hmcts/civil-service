package uk.gov.hmcts.reform.unspec.handler.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.unspec.event.TakeCaseOfflineEvent;
import uk.gov.hmcts.reform.unspec.service.CoreCaseDataService;

import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.TAKE_CASE_OFFLINE;

@Slf4j
@Service
@RequiredArgsConstructor
public class TakeCaseOfflineEventHandler {

    private final CoreCaseDataService coreCaseDataService;

    @EventListener
    public void takeCaseOffline(TakeCaseOfflineEvent event) {
        coreCaseDataService.triggerEvent(event.getCaseId(), TAKE_CASE_OFFLINE);
    }
}
