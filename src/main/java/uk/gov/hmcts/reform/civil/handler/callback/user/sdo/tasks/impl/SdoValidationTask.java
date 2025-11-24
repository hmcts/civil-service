package uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class SdoValidationTask implements DirectionsOrderCallbackTask {

    private final SdoValidationService sdoValidationService;

    @Override
    public DirectionsOrderTaskResult execute(DirectionsOrderTaskContext context) {
        var caseData = context.caseData();
        var errors = sdoValidationService.validate(caseData);
        log.info("SDO validation found {} issue(s) for caseId {}", errors.size(), caseData.getCcdCaseReference());
        return DirectionsOrderTaskResult.withErrors(caseData, errors);
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
