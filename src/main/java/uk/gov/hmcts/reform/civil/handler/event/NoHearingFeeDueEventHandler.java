package uk.gov.hmcts.reform.civil.handler.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.event.NoHearingFeeDueEvent;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NO_HEARING_FEE_DUE;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoHearingFeeDueEventHandler {

    private final CoreCaseDataService coreCaseDataService;

    @EventListener
    public void moveCaseToPrepareForHearing(NoHearingFeeDueEvent event) {
        coreCaseDataService.triggerEvent(event.getCaseId(), NO_HEARING_FEE_DUE);
    }
}
