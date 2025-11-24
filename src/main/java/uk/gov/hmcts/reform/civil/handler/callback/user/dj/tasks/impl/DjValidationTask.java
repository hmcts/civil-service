package uk.gov.hmcts.reform.civil.handler.callback.user.dj.tasks.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderCallbackTask;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderLifecycleStage;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderTaskContext;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderTaskResult;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderTaskSupport;
import uk.gov.hmcts.reform.civil.service.dj.DjValidationService;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.STANDARD_DIRECTION_ORDER_DJ;

@Component
@RequiredArgsConstructor
@Slf4j
public class DjValidationTask implements DirectionsOrderCallbackTask {

    private final DjValidationService djValidationService;

    @Override
    public DirectionsOrderTaskResult execute(DirectionsOrderTaskContext context) {
        var caseData = context.caseData();
        var errors = djValidationService.validate(caseData);
        log.info("DJ validation found {} issue(s) for caseId {}", errors.size(), caseData.getCcdCaseReference());
        return DirectionsOrderTaskResult.withErrors(caseData, errors);
    }

    @Override
    public boolean supports(DirectionsOrderLifecycleStage stage) {
        return DirectionsOrderLifecycleStage.MID_EVENT == stage;
    }

    @Override
    public boolean appliesTo(DirectionsOrderTaskContext context) {
        return DirectionsOrderTaskSupport.supportsEvent(context, STANDARD_DIRECTION_ORDER_DJ);
    }
}
