package uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.SdoCallbackTask;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.SdoLifecycleStage;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.SdoTaskContext;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.SdoTaskResult;
import uk.gov.hmcts.reform.civil.service.sdo.SdoValidationService;

@Component
@RequiredArgsConstructor
public class SdoValidationTask implements SdoCallbackTask {

    private final SdoValidationService sdoValidationService;

    @Override
    public SdoTaskResult execute(SdoTaskContext context) {
        return SdoTaskResult.withErrors(context.caseData(), sdoValidationService.validate(context.caseData()));
    }

    @Override
    public boolean supports(SdoLifecycleStage stage) {
        return SdoLifecycleStage.MID_EVENT == stage;
    }
}
