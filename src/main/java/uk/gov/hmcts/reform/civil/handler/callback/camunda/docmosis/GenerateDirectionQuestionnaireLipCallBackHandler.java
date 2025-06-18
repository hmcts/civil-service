package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.enums.DocCategory;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.SystemGeneratedDocumentService;
import uk.gov.hmcts.reform.civil.service.docmosis.dq.DirectionQuestionnaireLipGeneratorFactory;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_RESPONSE_DQ_LIP_SEALED;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@Service
@RequiredArgsConstructor
public class GenerateDirectionQuestionnaireLipCallBackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(GENERATE_RESPONSE_DQ_LIP_SEALED);
    private final Map<String, Callback> callbackMap = Map.of(
        callbackKey(CallbackType.ABOUT_TO_SUBMIT), this::prepareDirectionsQuestionnaire
    );

    private final ObjectMapper objectMapper;
    private final DirectionQuestionnaireLipGeneratorFactory directionQuestionnaireLipGeneratorFactory;
    private final SystemGeneratedDocumentService systemGeneratedDocumentService;
    private final AssignCategoryId assignCategoryId;
    private final FeatureToggleService featureToggleService;

    @Override
    protected Map<String, Callback> callbacks() {
        return callbackMap;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse prepareDirectionsQuestionnaire(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        if (caseData.isFullAdmitClaimSpec()
            || caseData.isClaimantAcceptedClaimAmount()) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .build();
        }
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        CaseDocument sealedDQForm = directionQuestionnaireLipGeneratorFactory
            .getDirectionQuestionnaire()
            .generate(caseData, callbackParams.getParams().get(BEARER_TOKEN).toString());
        if (featureToggleService.isGaForWelshEnabled()
            && sealedDQForm.getDocumentName().contains("claimant")
            && (caseData.isClaimantBilingual() || caseData.isRespondentResponseBilingual())) {
            assignCategoryId.assignCategoryIdToCaseDocument(sealedDQForm, DocCategory.DQ_APP1.getValue());
            List<Element<CaseDocument>> translatedDocuments = callbackParams.getCaseData()
                .getPreTranslationDocuments();
            translatedDocuments.add(element(sealedDQForm));
            caseDataBuilder.preTranslationDocuments(translatedDocuments);
        } else {
            caseDataBuilder
                .systemGeneratedCaseDocuments(systemGeneratedDocumentService
                                                  .getSystemGeneratedDocumentsWithAddedDocument(
                                                      sealedDQForm,
                                                      caseData
                                                  ));
            if (sealedDQForm.getDocumentName().contains("defendant")) {
                assignCategoryId.assignCategoryIdToCaseDocument(sealedDQForm, DocCategory.DQ_DEF1.getValue());
            } else {
                assignCategoryId.assignCategoryIdToCaseDocument(sealedDQForm, DocCategory.DQ_APP1.getValue());
                CaseDocument sealedDQFormCopy = sealedDQForm.toBuilder()
                    .documentLink(sealedDQForm.getDocumentLink().toBuilder().build()).build();
                caseDataBuilder.systemGeneratedCaseDocuments(systemGeneratedDocumentService
                                                                 .getSystemGeneratedDocumentsWithAddedDocument(
                                                                     sealedDQFormCopy,
                                                                     caseData
                                                                 ));
                assignCategoryId.assignCategoryIdToCaseDocument(sealedDQFormCopy, DocCategory.APP1_DQ.getValue());
            }
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }
}
