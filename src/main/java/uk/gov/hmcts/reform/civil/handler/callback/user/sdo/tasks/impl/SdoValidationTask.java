package uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderCallbackTask;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderLifecycleStage;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderTaskContext;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderTaskResult;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderTaskSupport;
import uk.gov.hmcts.reform.civil.service.sdo.SdoValidationService;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_SDO;

@Component
@RequiredArgsConstructor
public class SdoValidationTask implements DirectionsOrderCallbackTask {

    private final SdoValidationService sdoValidationService;

    @Override
    public DirectionsOrderTaskResult execute(DirectionsOrderTaskContext context) {
        return DirectionsOrderTaskResult.withErrors(
            context.caseData(),
            sdoValidationService.validate(context.caseData())
        );
    }

    @Override
    public boolean supports(DirectionsOrderLifecycleStage stage) {
        return DirectionsOrderLifecycleStage.MID_EVENT == stage;
    }

    @Override
    public boolean appliesTo(DirectionsOrderTaskContext context) {
        return DirectionsOrderTaskSupport.supportsEvent(context, CREATE_SDO);
    }
}
