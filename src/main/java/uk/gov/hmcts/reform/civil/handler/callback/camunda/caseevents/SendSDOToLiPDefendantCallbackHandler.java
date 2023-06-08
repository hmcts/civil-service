package uk.gov.hmcts.reform.civil.handler.callback.camunda.caseevents;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.service.BulkPrintService;
import uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim.SealedClaimFormGeneratorForSpec;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SEND_SDO_ORDER_TO_LiP_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.tasks.BaseExternalTaskHandler.log;

@Service
@RequiredArgsConstructor
public class SendSDOToLiPDefendantCallbackHandler extends CallbackHandler {
    private static final List<CaseEvent> EVENTS = List.of(SEND_SDO_ORDER_TO_LiP_DEFENDANT);
    private static final String LETTER_TYPE = "SDO_ORDER";
    public static final String TASK_ID = "SendSDOToDefendantLip";

    private final BulkPrintService bulkPrintService;
    private final SealedClaimFormGeneratorForSpec sealedClaimFormGeneratorForSpec;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::sendSDOBulkPrint
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    private CallbackResponse sendSDOBulkPrint(CallbackParams callbackParams) {
        //get the sdo order template
        CaseData caseData = callbackParams.getCaseData();
        List<Element<uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument>> systemGeneratedCaseDocuments = caseData.getSystemGeneratedCaseDocuments();

        if(systemGeneratedCaseDocuments.size() > 0 ) {
            log.info("caseDoc size: {} ",systemGeneratedCaseDocuments.size());
            CaseDocument caseDocument = systemGeneratedCaseDocuments.stream()
                .filter(systemGeneratedCaseDocument-> systemGeneratedCaseDocument.getValue().getDocumentType().equals(DocumentType.SDO_ORDER)).findAny().get().getValue();

            if(caseDocument != null) {
                log.info("caseDoc name: {}",caseDocument.getDocumentName());
                 byte[] letter = sealedClaimFormGeneratorForSpec.downloadDocument(caseDocument);
                 log.info("letter content : {}", letter);
                 //print the pip letter
                  bulkPrintService.printLetter(letter, caseData.getLegacyCaseReference(),
                                             caseData.getLegacyCaseReference(), LETTER_TYPE);
            }
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }
}
