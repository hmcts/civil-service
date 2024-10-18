package uk.gov.hmcts.reform.civil.handler.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.event.CoscApplicationProcessorEvent;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.PROCESS_COSC_APPLICATION;

@Slf4j
@Service
@RequiredArgsConstructor
public class CoscApplicationProcessorEventHandler {

    private final CoreCaseDataService coreCaseDataService;

    @EventListener
    public void processCoscApplication(CoscApplicationProcessorEvent event) {
        coreCaseDataService.triggerEvent(event.getCaseId(), PROCESS_COSC_APPLICATION);
    }
}
