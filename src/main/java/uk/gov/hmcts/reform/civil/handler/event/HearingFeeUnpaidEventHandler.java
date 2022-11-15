package uk.gov.hmcts.reform.civil.handler.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.event.HearingFeeUnpaidEvent;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.HEARING_FEE_UNPAID;

@Slf4j
@Service
@RequiredArgsConstructor
public class HearingFeeUnpaidEventHandler {

    private final CoreCaseDataService coreCaseDataService;

    @EventListener
    public void moveCaseToStruckOut(HearingFeeUnpaidEvent event) {
        coreCaseDataService.triggerEvent(event.getCaseId(), HEARING_FEE_UNPAID);
    }
}
