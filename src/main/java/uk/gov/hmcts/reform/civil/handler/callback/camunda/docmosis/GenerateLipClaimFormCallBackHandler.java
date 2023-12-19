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
import uk.gov.hmcts.reform.civil.service.docmosis.claimform.ClaimFormGenerator;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_DRAFT_FORM;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_LIP_CLAIMANT_CLAIM_FORM_SPEC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_LIP_DEFENDANT_CLAIM_FORM_SPEC;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@Service
@RequiredArgsConstructor
public class GenerateLipClaimFormCallBackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(
        GENERATE_DRAFT_FORM,
        GENERATE_LIP_CLAIMANT_CLAIM_FORM_SPEC,
        GENERATE_LIP_DEFENDANT_CLAIM_FORM_SPEC
    );
    private final Map<String, Callback> callbackMap = Map.of(callbackKey(ABOUT_TO_SUBMIT), this::generateClaimForm);
    private final ObjectMapper objectMapper;
    private final ClaimFormGenerator claimFormGenerator;

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

        CaseData updatedCaseData = updateCaseData(caseData, caseDocument, caseEvent);
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData.toMap(objectMapper))
            .build();
    }

    private CaseData updateCaseData(CaseData caseData, CaseDocument caseDocument, CaseEvent caseEvent) {
        return switch (caseEvent) {
            case GENERATE_DRAFT_FORM -> buildDraftClaimFormCaseData(caseData, caseDocument);
            case GENERATE_LIP_CLAIMANT_CLAIM_FORM_SPEC -> buildClaimantFormCaseData(caseData, caseDocument);
            case GENERATE_LIP_DEFENDANT_CLAIM_FORM_SPEC -> buildDefendantClaimFormData(caseData, caseDocument);
            default -> throw new IllegalArgumentException("case event not found");
        };
    }

    private CaseData buildDraftClaimFormCaseData(CaseData caseData, CaseDocument caseDocument) {
        List<Element<CaseDocument>> claimantDocuments = caseData.getClaimantDocuments();
        claimantDocuments.add(element(caseDocument));

        return caseData.toBuilder()
            .claimantDocuments(claimantDocuments)
            .build();
    }

    private CaseData buildClaimantFormCaseData(CaseData caseData, CaseDocument caseDocument) {
        List<Element<CaseDocument>> claimantDocuments = caseData.getClaimantDocuments();

        // Remove Draft form from claimant documents
        claimantDocuments = claimantDocuments.stream().filter(claimDoc -> claimDoc.getValue().getDocumentType() != DocumentType.DRAFT_CLAIM_FORM)
            .collect(Collectors.toList());

        claimantDocuments.add(element(caseDocument));

        return caseData.toBuilder()
            .claimantDocuments(claimantDocuments)
            .build();
    }

    private CaseData buildDefendantClaimFormData(CaseData caseData, CaseDocument caseDocument) {
        List<Element<CaseDocument>> defendantDocuments = caseData.getDefendantDocuments();
        defendantDocuments.add(element(caseDocument));

        return caseData.toBuilder()
            .defendantDocuments(defendantDocuments)
            .build();
    }

}
