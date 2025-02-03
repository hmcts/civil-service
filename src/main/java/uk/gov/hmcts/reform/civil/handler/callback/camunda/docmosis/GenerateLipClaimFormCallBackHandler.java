package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.hmcts.reform.civil.service.SystemGeneratedDocumentService;
import uk.gov.hmcts.reform.civil.service.docmosis.claimform.ClaimFormGenerator;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_DRAFT_FORM;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_LIP_CLAIMANT_CLAIM_FORM_SPEC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_LIP_DEFENDANT_CLAIM_FORM_SPEC;
import static uk.gov.hmcts.reform.civil.enums.DocCategory.CLAIMANT1_DETAILS_OF_CLAIM;

@Service
@RequiredArgsConstructor
public class GenerateLipClaimFormCallBackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(
        GENERATE_DRAFT_FORM,
        GENERATE_LIP_CLAIMANT_CLAIM_FORM_SPEC,
        GENERATE_LIP_DEFENDANT_CLAIM_FORM_SPEC
    );
    private final ObjectMapper objectMapper;
    private final ClaimFormGenerator claimFormGenerator;
    private final AssignCategoryId assignCategoryId;
    private final SystemGeneratedDocumentService systemGeneratedDocumentService;
    private final Map<String, Callback> callbackMap = Map.of(callbackKey(ABOUT_TO_SUBMIT), this::generateClaimForm);

    @Override
    protected Map<String, Callback> callbacks() {
        return callbackMap;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse generateClaimForm(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseEvent caseEvent = CaseEvent.valueOf(callbackParams.getRequest().getEventId());
        CaseDocument caseDocument = claimFormGenerator.generate(
            caseData,
            callbackParams.getParams().get(BEARER_TOKEN).toString(),
            caseEvent
        );
        assignCategoryId.assignCategoryIdToCaseDocument(caseDocument, CLAIMANT1_DETAILS_OF_CLAIM.getValue());

        CaseData updatedCaseData = updateCaseData(caseData, caseDocument, caseEvent);
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData.toMap(objectMapper))
            .build();
    }

    private CaseData updateCaseData(CaseData caseData, CaseDocument caseDocument, CaseEvent caseEvent) {
        return switch (caseEvent) {
            case GENERATE_DRAFT_FORM, GENERATE_LIP_CLAIMANT_CLAIM_FORM_SPEC, GENERATE_LIP_DEFENDANT_CLAIM_FORM_SPEC ->
                buildClaimFormData(caseData, caseDocument, caseEvent);
            default -> throw new IllegalArgumentException("case event not found");
        };
    }

    private CaseData buildClaimFormData(CaseData caseData, CaseDocument caseDocument, CaseEvent event) {
        List<Element<CaseDocument>> systemGeneratedCaseDocuments = systemGeneratedDocumentService.getSystemGeneratedDocumentsWithAddedDocument(
            caseDocument,
            caseData
        );

        // Remove Draft form from documents
        if (event == GENERATE_LIP_CLAIMANT_CLAIM_FORM_SPEC) {
            systemGeneratedCaseDocuments = systemGeneratedCaseDocuments.stream().filter(claimDoc -> claimDoc.getValue().getDocumentType() != DocumentType.DRAFT_CLAIM_FORM)
                .toList();
        }

        return caseData.toBuilder()
            .systemGeneratedCaseDocuments(systemGeneratedCaseDocuments)
            .build();
    }
}
