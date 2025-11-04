package uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.SdoCallbackTask;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.SdoLifecycleStage;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.SdoTaskContext;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.SdoTaskResult;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.sdo.SdoDocumentService;

import java.util.Collections;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;

@Component
@RequiredArgsConstructor
public class SdoDocumentTask implements SdoCallbackTask {

    private final SdoDocumentService sdoDocumentService;

    @Override
    public SdoTaskResult execute(SdoTaskContext context) {
        CaseData caseData = context.caseData();
        String authToken = context.callbackParams().getParams().get(BEARER_TOKEN).toString();
        Optional<CaseDocument> document = sdoDocumentService.generateSdoDocument(caseData, authToken);

        if (document.isPresent()) {
            CaseDocument generatedDocument = document.get();
            sdoDocumentService.assignCategory(generatedDocument, "caseManagementOrders");
            CaseData updatedCaseData = caseData.toBuilder()
                .sdoOrderDocument(generatedDocument)
                .build();
            return new SdoTaskResult(updatedCaseData, Collections.emptyList(), null);
        }

        return SdoTaskResult.empty(caseData);
    }

    @Override
    public boolean supports(SdoLifecycleStage stage) {
        return SdoLifecycleStage.DOCUMENT_GENERATION == stage;
    }
}
