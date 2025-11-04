package uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.SdoCallbackTask;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.SdoLifecycleStage;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.SdoTaskContext;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.SdoTaskResult;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.sdo.SdoFeatureToggleService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoLocationService;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;

@Component
@RequiredArgsConstructor
public class SdoSubmissionTask implements SdoCallbackTask {

    private final SdoFeatureToggleService featureToggleService;
    private final SdoLocationService sdoLocationService;

    @Override
    public SdoTaskResult execute(SdoTaskContext context) {
        CaseData caseData = context.caseData();
        String authToken = context.callbackParams().getParams().get(BEARER_TOKEN).toString();
        sdoLocationService.updateWaLocationsIfRequired(caseData, caseData.toBuilder(), authToken);
        return SdoTaskResult.empty(caseData);
    }

    @Override
    public boolean supports(SdoLifecycleStage stage) {
        return SdoLifecycleStage.SUBMISSION == stage;
    }
}
