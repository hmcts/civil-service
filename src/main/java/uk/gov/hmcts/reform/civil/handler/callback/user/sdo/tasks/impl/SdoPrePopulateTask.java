package uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.SdoCallbackTask;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.SdoLifecycleStage;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.SdoTaskContext;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.SdoTaskResult;
import uk.gov.hmcts.reform.civil.service.sdo.SdoFeatureToggleService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoLocationService;

@Component
@RequiredArgsConstructor
public class SdoPrePopulateTask implements SdoCallbackTask {

    private final SdoFeatureToggleService sdoFeatureToggleService;
    private final SdoLocationService sdoLocationService;

    @Override
    public SdoTaskResult execute(SdoTaskContext context) {
        return SdoTaskResult.empty(context.caseData());
    }

    @Override
    public boolean supports(SdoLifecycleStage stage) {
        return SdoLifecycleStage.PRE_POPULATE == stage;
    }
}
