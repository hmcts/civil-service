package uk.gov.hmcts.reform.civil.handler.callback.user.dj.tasks.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderCallbackTask;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderLifecycleStage;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderTaskContext;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderTaskResult;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderTaskSupport;
import uk.gov.hmcts.reform.civil.service.dj.DjSubmissionService;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.STANDARD_DIRECTION_ORDER_DJ;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;

@Component
@RequiredArgsConstructor
@Slf4j
public class DjSubmissionTask implements DirectionsOrderCallbackTask {

    private final DjSubmissionService submissionService;

    @Override
    public DirectionsOrderTaskResult execute(DirectionsOrderTaskContext context) {
        String authToken = context.callbackParams().getParams().get(BEARER_TOKEN).toString();
        log.info("Preparing DJ submission for caseId {}", context.caseData().getCcdCaseReference());
        return DirectionsOrderTaskResult.empty(submissionService.prepareSubmission(context.caseData(), authToken));
    }

    @Override
    public boolean supports(DirectionsOrderLifecycleStage stage) {
        return DirectionsOrderLifecycleStage.SUBMISSION == stage;
    }

    @Override
    public boolean appliesTo(DirectionsOrderTaskContext context) {
        return DirectionsOrderTaskSupport.supportsEvent(context, STANDARD_DIRECTION_ORDER_DJ);
    }
}
