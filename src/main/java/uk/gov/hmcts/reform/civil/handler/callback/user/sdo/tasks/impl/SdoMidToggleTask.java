package uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.SdoCallbackTask;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.SdoLifecycleStage;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.SdoTaskContext;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.SdoTaskResult;
import uk.gov.hmcts.reform.civil.service.sdo.SdoCaseClassificationService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoFeatureToggleService;

@Component
@RequiredArgsConstructor
public class SdoMidToggleTask implements SdoCallbackTask {

    private final SdoCaseClassificationService caseClassificationService;
    private final SdoFeatureToggleService sdoFeatureToggleService;

    @Override
    public SdoTaskResult execute(SdoTaskContext context) {
        return SdoTaskResult.empty(context.caseData());
    }

    @Override
    public boolean supports(SdoLifecycleStage stage) {
        return SdoLifecycleStage.MID_EVENT == stage;
    }
}
