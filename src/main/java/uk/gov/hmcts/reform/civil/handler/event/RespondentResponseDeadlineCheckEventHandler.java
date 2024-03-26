package uk.gov.hmcts.reform.civil.handler.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.event.RespondentResponseDeadlineCheckEvent;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RespondentResponseDeadlineCheckEventHandler {

    private final CoreCaseDataService coreCaseDataService;

    @EventListener
    public void checkForRespondentResponseDeadline(RespondentResponseDeadlineCheckEvent event) {
        coreCaseDataService.triggerEvent(event.getCaseId(), RESPONDENT_RESPONSE_DEADLINE_CHECK);
    }
}
