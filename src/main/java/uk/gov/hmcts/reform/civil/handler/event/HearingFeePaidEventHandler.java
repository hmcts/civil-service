package uk.gov.hmcts.reform.civil.handler.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.event.HearingFeePaidEvent;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.HEARING_FEE_PAID;

@Slf4j
@Service
@RequiredArgsConstructor
public class HearingFeePaidEventHandler {

    private final CoreCaseDataService coreCaseDataService;

    @EventListener
    public void moveCaseToPrepareForHearing(HearingFeePaidEvent event) {
        coreCaseDataService.triggerEvent(event.getCaseId(), HEARING_FEE_PAID);
    }
}
