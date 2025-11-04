package uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.SdoCallbackTask;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.SdoLifecycleStage;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.SdoTaskContext;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.SdoTaskResult;
import uk.gov.hmcts.reform.civil.service.sdo.SdoDocumentService;

import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;

@Component
@RequiredArgsConstructor
public class SdoDocumentTask implements SdoCallbackTask {

    private final SdoDocumentService sdoDocumentService;

    @Override
    public SdoTaskResult execute(SdoTaskContext context) {
        CallbackParams params = context.callbackParams();
        String authToken = params.getParams().get(BEARER_TOKEN).toString();
        Optional<CaseDocument> document = sdoDocumentService.generateSdoDocument(context.caseData(), authToken);
        document.ifPresent(doc -> sdoDocumentService.assignCategory(doc, "caseManagementOrders"));
        return SdoTaskResult.empty(context.caseData());
    }

    @Override
    public boolean supports(SdoLifecycleStage stage) {
        return SdoLifecycleStage.DOCUMENT_GENERATION == stage;
    }
}
