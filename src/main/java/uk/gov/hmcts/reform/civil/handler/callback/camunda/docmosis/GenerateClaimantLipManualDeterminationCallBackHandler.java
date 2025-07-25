package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.welshenhancements.PreTranslationDocumentType;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.SystemGeneratedDocumentService;
import uk.gov.hmcts.reform.civil.service.docmosis.manualdetermination.ClaimantLipManualDeterminationFormGenerator;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_LIP_CLAIMANT_MANUAL_DETERMINATION;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@Service
@RequiredArgsConstructor
public class GenerateClaimantLipManualDeterminationCallBackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(GENERATE_LIP_CLAIMANT_MANUAL_DETERMINATION);
    private static final String TASK_ID = "Generate_LIP_Claimant_MD";
    private final ObjectMapper objectMapper;
    private final ClaimantLipManualDeterminationFormGenerator claimantLipManualDeterminationFormGenerator;
    private final SystemGeneratedDocumentService systemGeneratedDocumentService;
    private final Map<String, Callback> callbackMap = Map.of(callbackKey(CallbackType.ABOUT_TO_SUBMIT),
            this::prepareClaimantLipManualDetermination);
    private final FeatureToggleService featureToggleService;

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    @Override
    protected Map<String, Callback> callbacks() {
        return callbackMap;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse prepareClaimantLipManualDetermination(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        if (!caseData.getRespondent1().isCompanyOROrganisation()) {
            return AboutToStartOrSubmitCallbackResponse.builder().build();
        }

        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        CaseDocument claimantResponseForm = claimantLipManualDeterminationFormGenerator.generate(callbackParams.getCaseData(),
                callbackParams.getParams().get(BEARER_TOKEN).toString());
        if (featureToggleService.isWelshEnabledForMainCase()
            && (caseData.isClaimantBilingual() || caseData.isRespondentResponseBilingual())) {
            List<Element<CaseDocument>> translatedDocuments = callbackParams.getCaseData()
                .getPreTranslationDocuments();
            translatedDocuments.add(element(claimantResponseForm));
            caseDataBuilder.preTranslationDocuments(translatedDocuments);
            caseDataBuilder.bilingualHint(YesOrNo.YES);
            caseDataBuilder.preTranslationDocumentType(PreTranslationDocumentType.MANUAL_DETERMINATION_DOCUMENT);
        } else {
            caseDataBuilder.systemGeneratedCaseDocuments(systemGeneratedDocumentService
                                                             .getSystemGeneratedDocumentsWithAddedDocument(
                                                                 claimantResponseForm,
                                                                 caseData
                                                             ));
        }
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataBuilder.build().toMap(objectMapper)).build();
    }

}
