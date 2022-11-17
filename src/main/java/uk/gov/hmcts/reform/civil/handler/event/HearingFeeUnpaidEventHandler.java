package uk.gov.hmcts.reform.civil.handler.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
<<<<<<< HEAD
import uk.gov.hmcts.reform.civil.event.DismissClaimEvent;
=======
import uk.gov.hmcts.reform.civil.event.HearingFeeUnpaidEvent;
>>>>>>> d7092041ac95c317bc72d5490daab487e1ac366c
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.HEARING_FEE_UNPAID;

@Slf4j
@Service
@RequiredArgsConstructor
public class HearingFeeUnpaidEventHandler {

    private final CoreCaseDataService coreCaseDataService;

    @EventListener
<<<<<<< HEAD
    public void moveCaseToStruckOut(DismissClaimEvent event) {
=======
    public void moveCaseToStruckOut(HearingFeeUnpaidEvent event) {
>>>>>>> d7092041ac95c317bc72d5490daab487e1ac366c
        coreCaseDataService.triggerEvent(event.getCaseId(), HEARING_FEE_UNPAID);
    }
}
