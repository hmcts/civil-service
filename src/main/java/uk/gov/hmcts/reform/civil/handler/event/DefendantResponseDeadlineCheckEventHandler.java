package uk.gov.hmcts.reform.civil.handler.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.event.DefendantResponseDeadlineCheckEvent;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFENDANT_RESPONSE_DEADLINE_CHECK;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefendantResponseDeadlineCheckEventHandler {

    private final CoreCaseDataService coreCaseDataService;

    @EventListener
    public void checkForDefendantResponseDeadline(DefendantResponseDeadlineCheckEvent event) {
        coreCaseDataService.triggerEvent(event.getCaseId(), DEFENDANT_RESPONSE_DEADLINE_CHECK);
    }
}
