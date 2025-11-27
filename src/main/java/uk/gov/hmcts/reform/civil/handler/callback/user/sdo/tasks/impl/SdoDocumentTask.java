package uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderCallbackTask;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderLifecycleStage;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderTaskContext;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderTaskResult;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderTaskSupport;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.sdo.SdoDocumentService;

import java.util.Collections;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_SDO;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;

@Component
@RequiredArgsConstructor
@Slf4j
public class SdoDocumentTask implements DirectionsOrderCallbackTask {

    private final SdoDocumentService sdoDocumentService;

    @Override
    public DirectionsOrderTaskResult execute(DirectionsOrderTaskContext context) {
        CaseData caseData = context.caseData();
        String authToken = context.callbackParams().getParams().get(BEARER_TOKEN).toString();
        log.info("Generating SDO document for caseId {}", caseData.getCcdCaseReference());
        Optional<CaseDocument> document = sdoDocumentService.generateSdoDocument(caseData, authToken);

        if (document.isPresent()) {
            CaseDocument generatedDocument = document.get();
            sdoDocumentService.assignCategory(generatedDocument, "caseManagementOrders");
            caseData.setSdoOrderDocument(generatedDocument);
            log.info("Generated SDO document for caseId {}", caseData.getCcdCaseReference());
            return new DirectionsOrderTaskResult(caseData, Collections.emptyList(), null);
        }

        log.info("No SDO document generated for caseId {}", caseData.getCcdCaseReference());
        return DirectionsOrderTaskResult.empty(caseData);
    }

    @Override
    public boolean supports(DirectionsOrderLifecycleStage stage) {
        return DirectionsOrderLifecycleStage.DOCUMENT_GENERATION == stage;
    }

    @Override
    public boolean appliesTo(DirectionsOrderTaskContext context) {
        return DirectionsOrderTaskSupport.supportsEvent(context, CREATE_SDO);
    }
}
