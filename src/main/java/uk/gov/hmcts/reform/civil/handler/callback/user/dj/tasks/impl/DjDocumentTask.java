package uk.gov.hmcts.reform.civil.handler.callback.user.dj.tasks.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.SdoCallbackTask;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.SdoLifecycleStage;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.SdoTaskContext;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.SdoTaskResult;
import uk.gov.hmcts.reform.civil.service.dj.DjDocumentService;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;

@Component
@RequiredArgsConstructor
public class DjDocumentTask implements SdoCallbackTask {

    private final DjDocumentService djDocumentService;

    @Override
    public SdoTaskResult execute(SdoTaskContext context) {
        String authToken = context.callbackParams().getParams().get(BEARER_TOKEN).toString();
        djDocumentService.generateOrder(context.caseData(), authToken)
            .ifPresent(document -> djDocumentService.assignCategory(document, "caseManagementOrders"));
        return SdoTaskResult.empty(context.caseData());
    }

    @Override
    public boolean supports(SdoLifecycleStage stage) {
        return SdoLifecycleStage.DOCUMENT_GENERATION == stage;
    }
}
