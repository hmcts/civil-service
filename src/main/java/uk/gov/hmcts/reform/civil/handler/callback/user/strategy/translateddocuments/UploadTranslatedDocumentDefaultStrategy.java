package uk.gov.hmcts.reform.civil.handler.callback.user.strategy.translateddocuments;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.DocCategory;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocument;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.SystemGeneratedDocumentService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.INTERLOCUTORY_JUDGMENT;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.CCJ_REQUEST_ADMISSION;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.MANUAL_DETERMINATION;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.ORDER_NOTICE;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.STANDARD_DIRECTION_ORDER;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@Component
@RequiredArgsConstructor
public class UploadTranslatedDocumentDefaultStrategy implements UploadTranslatedDocumentStrategy {

    private final SystemGeneratedDocumentService systemGeneratedDocumentService;
    private final ObjectMapper objectMapper;
    private final AssignCategoryId assignCategoryId;
    private final FeatureToggleService featureToggleService;

    @Override
    public CallbackResponse uploadDocument(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        updateSystemGeneratedDocumentsWithOriginalDocuments(callbackParams);
        List<Element<CaseDocument>> updatedDocumentList = updateSystemGeneratedDocumentsWithTranslationDocuments(
            callbackParams);
        CaseDataLiP caseDataLip = caseData.getCaseDataLiP();

        CaseEvent businessProcessEvent = getBusinessProcessEvent(caseData);

        if (Objects.nonNull(caseDataLip)) {
            caseDataLip.setTranslatedDocuments(null);
        }

        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder().systemGeneratedCaseDocuments(
                updatedDocumentList)
            .caseDataLiP(caseDataLip);

        if (businessProcessEvent != null) {
            caseDataBuilder = caseDataBuilder.businessProcess(BusinessProcess.ready(businessProcessEvent));
        }
        CaseData updatedCaseData = caseDataBuilder.build();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData.toMap(objectMapper))
            .build();
    }

    private void updateSystemGeneratedDocumentsWithOriginalDocuments(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        List<Element<TranslatedDocument>> translatedDocuments = caseData.getTranslatedDocuments();
        List<Element<CaseDocument>> preTranslatedDocuments = caseData.getPreTranslationDocuments();
        List<Element<CaseDocument>> sdoOrderDocuments = caseData.getPreTranslationSdoOrderDocuments();
        List<Element<CaseDocument>> preTranslationDocuments = caseData.getPreTranslationDocuments();
        if (featureToggleService.isCaseProgressionEnabled() && Objects.nonNull(translatedDocuments)) {
            translatedDocuments.forEach(document -> {
                if (Objects.nonNull(sdoOrderDocuments) && !sdoOrderDocuments.isEmpty()) {
                    Element<CaseDocument> originalSdo = sdoOrderDocuments.remove(0);
                    List<Element<CaseDocument>> systemGeneratedDocuments = caseData.getSystemGeneratedCaseDocuments();
                    systemGeneratedDocuments.add(originalSdo);
                } else if (document.getValue().getDocumentType().equals(INTERLOCUTORY_JUDGMENT)) {
                    if (Objects.nonNull(preTranslationDocuments)) {
                        Optional<Element<CaseDocument>> preTranslationInterlocJudgment = preTranslationDocuments.stream()
                            .filter(item -> item.getValue().getDocumentType() == DocumentType.INTERLOCUTORY_JUDGEMENT)
                            .findFirst();
                        preTranslationInterlocJudgment.ifPresent(preTranslationDocuments::remove);
                        preTranslationInterlocJudgment.ifPresent(caseData.getSystemGeneratedCaseDocuments()::add);
                    }
                } else if (document.getValue().getDocumentType().equals(MANUAL_DETERMINATION)) {
                    if (Objects.nonNull(preTranslationDocuments)) {
                        Optional<Element<CaseDocument>> preTranslationManualDeterminationDoc = preTranslationDocuments.stream()
                            .filter(item -> item.getValue().getDocumentType() == DocumentType.LIP_MANUAL_DETERMINATION)
                            .findFirst();
                        preTranslationManualDeterminationDoc.ifPresent(preTranslationDocuments::remove);
                        preTranslationManualDeterminationDoc.ifPresent(caseData.getSystemGeneratedCaseDocuments()::add);
                    }
                } else if (document.getValue().getDocumentType().equals(CCJ_REQUEST_ADMISSION)) {
                    if (Objects.nonNull(preTranslationDocuments)) {
                        Optional<Element<CaseDocument>> preTranslationDoc = preTranslationDocuments.stream()
                            .filter(item -> item.getValue().getDocumentType() == DocumentType.CCJ_REQUEST_ADMISSION)
                            .findFirst();
                        preTranslationDoc.ifPresent(preTranslationDocuments::remove);
                        preTranslationDoc.ifPresent(caseData.getSystemGeneratedCaseDocuments()::add);
                    }
                } else if ((Objects.nonNull(preTranslatedDocuments) && !preTranslatedDocuments.isEmpty())) {
                    Element<CaseDocument> originalDocument = preTranslatedDocuments.remove(0);
                    List<Element<CaseDocument>> systemGeneratedDocuments = caseData.getSystemGeneratedCaseDocuments();
                    if (originalDocument.getValue().getDocumentName().contains("claimant")) {
                        CaseDocument claimantSealedCopy = CaseDocument.toCaseDocument(originalDocument.getValue().getDocumentLink(),
                                                                                originalDocument.getValue().getDocumentType());
                        systemGeneratedDocuments.add(element(claimantSealedCopy));
                        assignCategoryId.assignCategoryIdToCaseDocument(claimantSealedCopy, DocCategory.APP1_DQ.getValue());
                    }
                    systemGeneratedDocuments.add(originalDocument);
                }
            });
        }
    }

    private List<Element<CaseDocument>> updateSystemGeneratedDocumentsWithTranslationDocuments(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        List<Element<TranslatedDocument>> translatedDocuments = caseData.getTranslatedDocuments();
        if (featureToggleService.isCaseProgressionEnabled() && Objects.nonNull(translatedDocuments)) {
            translatedDocuments.forEach(document -> {
                if (document.getValue().getDocumentType().equals(ORDER_NOTICE)) {
                    document.getValue().getFile().setCategoryID("orders");
                } else if (document.getValue().getDocumentType().equals(STANDARD_DIRECTION_ORDER)) {
                    document.getValue().getFile().setCategoryID("caseManagementOrders");
                }
            });
        }
        return systemGeneratedDocumentService.getSystemGeneratedDocumentsWithAddedDocument(translatedDocuments, callbackParams);
    }

    private CaseEvent getBusinessProcessEvent(CaseData caseData) {
        if (featureToggleService.isCaseProgressionEnabled()) {
            List<Element<TranslatedDocument>> translatedDocuments = caseData.getTranslatedDocuments();

            if (Objects.nonNull(translatedDocuments)
                && translatedDocuments.get(0).getValue().getDocumentType().equals(ORDER_NOTICE)) {
                return CaseEvent.UPLOAD_TRANSLATED_DOCUMENT_ORDER_NOTICE;
            } else if (Objects.nonNull(translatedDocuments)
                && translatedDocuments.get(0).getValue().getDocumentType().equals(STANDARD_DIRECTION_ORDER)) {
                return CaseEvent.UPLOAD_TRANSLATED_DOCUMENT_SDO;
            } else if (Objects.nonNull(translatedDocuments)
                && (translatedDocuments.get(0).getValue().getDocumentType().equals(INTERLOCUTORY_JUDGMENT)
                || (translatedDocuments.get(0).getValue().getDocumentType().equals(MANUAL_DETERMINATION)))) {
                return CaseEvent.UPLOAD_TRANSLATED_DOCUMENT_CLAIMANT_REJECTS_REPAYMENT_PLAN;
            }
        }

        if ((caseData.isLipvLipOneVOne() && featureToggleService.isLipVLipEnabled())
                || (caseData.isLipvLROneVOne() && featureToggleService.isDefendantNoCOnlineForCase(caseData))) {
            if (caseData.getCcdState() == CaseState.PENDING_CASE_ISSUED) {
                return CaseEvent.UPLOAD_TRANSLATED_DOCUMENT_CLAIM_ISSUE;
            } else if (caseData.getCcdState() == CaseState.AWAITING_APPLICANT_INTENTION) {
                return CaseEvent.UPLOAD_TRANSLATED_DOCUMENT_CLAIMANT_INTENTION;
            }
        }

        return CaseEvent.UPLOAD_TRANSLATED_DOCUMENT;
    }
}
