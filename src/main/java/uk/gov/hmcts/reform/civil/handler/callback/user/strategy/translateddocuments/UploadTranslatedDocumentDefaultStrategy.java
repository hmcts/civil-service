package uk.gov.hmcts.reform.civil.handler.callback.user.strategy.translateddocuments;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocument;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.SystemGeneratedDocumentService;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class UploadTranslatedDocumentDefaultStrategy implements UploadTranslatedDocumentStrategy {

    private final SystemGeneratedDocumentService systemGeneratedDocumentService;
    private final ObjectMapper objectMapper;
    private final FeatureToggleService featureToggleService;

    @Override
    public CallbackResponse uploadDocument(CallbackParams callbackParams) {
        List<Element<CaseDocument>> updatedDocumentList = updateSystemGeneratedDocumentsWithTranslationDocuments(
            callbackParams);
        CaseDataLiP caseDataLip = callbackParams.getCaseData().getCaseDataLiP();

        if (Objects.nonNull(caseDataLip)) {
            caseDataLip.setTranslatedDocuments(null);
        }
        CaseData caseData = callbackParams.getCaseData();
        CaseData updatedCaseData = caseData.toBuilder().systemGeneratedCaseDocuments(
                updatedDocumentList)
            .caseDataLiP(caseDataLip)
            .businessProcess(BusinessProcess.ready(getBussinessProcessEvent(caseData))).build();

        AboutToStartOrSubmitCallbackResponse.AboutToStartOrSubmitCallbackResponseBuilder response =
                AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData.toMap(objectMapper));

        updateClaimEndState(response, caseData);

        return response.build();
    }

    private List<Element<CaseDocument>> updateSystemGeneratedDocumentsWithTranslationDocuments(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        List<Element<TranslatedDocument>> translatedDocuments = caseData.getTranslatedDocuments();
        return systemGeneratedDocumentService.getSystemGeneratedDocumentsWithAddedDocument(translatedDocuments, callbackParams);
    }

    private void updateClaimEndState(AboutToStartOrSubmitCallbackResponse.AboutToStartOrSubmitCallbackResponseBuilder response, CaseData updatedData) {
        if (updatedData.isLipvLipOneVOne() && featureToggleService.isLipVLipEnabled()) {
            if (!updatedData.getCcdState().name().equals(CaseState.PENDING_CASE_ISSUED.name())) {
                response.state(CaseState.AWAITING_APPLICANT_INTENTION.name());
            }
        } else {
            response.state(CaseState.AWAITING_APPLICANT_INTENTION.name());
        }
    }

    private CaseEvent getBussinessProcessEvent(CaseData caseData) {
        if (caseData.isLipvLipOneVOne() && featureToggleService.isLipVLipEnabled()
                && caseData.getCcdState().name().equals(CaseState.PENDING_CASE_ISSUED.name())) {
            return CaseEvent.UPLOAD_TRANSLATED_DOCUMENT_LIP;
        }
        return CaseEvent.UPLOAD_TRANSLATED_DOCUMENT;
    }
}
