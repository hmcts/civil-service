package uk.gov.hmcts.reform.civil.handler.callback.user.dj.tasks.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderCallbackTask;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderLifecycleStage;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderTaskContext;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderTaskResult;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderTaskSupport;
import uk.gov.hmcts.reform.civil.service.dj.DjNarrativeService;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.STANDARD_DIRECTION_ORDER_DJ;

@Component
@RequiredArgsConstructor
@Slf4j
public class DjConfirmationTask implements DirectionsOrderCallbackTask {

    private final DjNarrativeService djNarrativeService;

    @Override
    public DirectionsOrderTaskResult execute(DirectionsOrderTaskContext context) {
        var caseData = context.caseData();
        log.info("Building DJ confirmation text for caseId {}", caseData.getCcdCaseReference());
        SubmittedCallbackResponse response = SubmittedCallbackResponse.builder()
            .confirmationHeader(djNarrativeService.buildConfirmationHeader(caseData))
            .confirmationBody(djNarrativeService.buildConfirmationBody(caseData))
            .build();
        return DirectionsOrderTaskResult.withSubmittedResponse(response);
    }

    @Override
    public boolean supports(DirectionsOrderLifecycleStage stage) {
        return DirectionsOrderLifecycleStage.CONFIRMATION == stage;
    }

    @Override
    public boolean appliesTo(DirectionsOrderTaskContext context) {
        return DirectionsOrderTaskSupport.supportsEvent(context, STANDARD_DIRECTION_ORDER_DJ);
    }
}
