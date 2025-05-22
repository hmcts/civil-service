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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.COURT_OFFICER_ORDER;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.FINAL_ORDER;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.STANDARD_DIRECTION_ORDER;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.ORDER_NOTICE;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.INTERLOCUTORY_JUDGMENT;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.SETTLEMENT_AGREEMENT;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.MANUAL_DETERMINATION;

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
        CaseData caseData = updateSystemGeneratedDocumentsWithOriginalDocuments(callbackParams);
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = updateDocumentCollectionsWithTranslationDocuments(
            caseData);
        CaseDataLiP caseDataLip = caseData.getCaseDataLiP();

        CaseEvent businessProcessEvent = getBusinessProcessEvent(caseData);

        if (Objects.nonNull(caseDataLip)) {
            caseDataLip.setTranslatedDocuments(null);
        }

        caseDataBuilder.caseDataLiP(caseDataLip);

        if (businessProcessEvent != null) {
            caseDataBuilder = caseDataBuilder.businessProcess(BusinessProcess.ready(businessProcessEvent));
        }
        CaseData updatedCaseData = caseDataBuilder.build();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData.toMap(objectMapper))
            .build();
    }

    private CaseData updateSystemGeneratedDocumentsWithOriginalDocuments(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        List<Element<TranslatedDocument>> translatedDocuments = caseData.getTranslatedDocuments();
        List<Element<CaseDocument>> preTranslatedDocuments = caseData.getPreTranslationDocuments();
        List<Element<CaseDocument>> sdoOrderDocuments = caseData.getPreTranslationSdoOrderDocuments();
        List<Element<CaseDocument>> preTranslationDocuments = caseData.getPreTranslationDocuments();
        List<Element<CaseDocument>> systemGeneratedDocuments = caseData.getSystemGeneratedCaseDocuments();
        List<Element<CaseDocument>> finalOrderDocuments = caseData.getFinalOrderDocumentCollection();
        List<Element<CaseDocument>> courtOfficerOrderDocuments = new ArrayList<>();
        if (featureToggleService.isCaseProgressionEnabled() && Objects.nonNull(translatedDocuments)) {
            translatedDocuments.forEach(document -> {
                if (Objects.nonNull(sdoOrderDocuments) && !sdoOrderDocuments.isEmpty()) {
                    Element<CaseDocument> originalSdo = sdoOrderDocuments.remove(0);
                    systemGeneratedDocuments.add(originalSdo);
                } else if (document.getValue().getDocumentType().equals(INTERLOCUTORY_JUDGMENT)) {
                    if (Objects.nonNull(preTranslationDocuments)) {
                        Optional<Element<CaseDocument>> preTranslationInterlocJudgment = preTranslationDocuments.stream()
                            .filter(item -> item.getValue().getDocumentType() == DocumentType.INTERLOCUTORY_JUDGEMENT)
                            .findFirst();
                        preTranslationInterlocJudgment.ifPresent(preTranslationDocuments::remove);
                        preTranslationInterlocJudgment.ifPresent(systemGeneratedDocuments::add);
                    }
                } else if (document.getValue().getDocumentType().equals(MANUAL_DETERMINATION)) {
                    if (Objects.nonNull(preTranslationDocuments)) {
                        Optional<Element<CaseDocument>> preTranslationManualDeterminationDoc = preTranslationDocuments.stream()
                            .filter(item -> item.getValue().getDocumentType() == DocumentType.LIP_MANUAL_DETERMINATION)
                            .findFirst();
                        preTranslationManualDeterminationDoc.ifPresent(preTranslationDocuments::remove);
                        preTranslationManualDeterminationDoc.ifPresent(systemGeneratedDocuments::add);
                    }
                } else if (document.getValue().getDocumentType().equals(FINAL_ORDER)) {
                    if (Objects.nonNull(preTranslationDocuments)) {
                        Optional<Element<CaseDocument>> preTranslationFinalOrderDoc = preTranslationDocuments.stream()
                            .filter(item -> item.getValue().getDocumentType() == DocumentType.JUDGE_FINAL_ORDER)
                            .findFirst();
                        preTranslationFinalOrderDoc.ifPresent(preTranslationDocuments::remove);
                        preTranslationFinalOrderDoc.ifPresent(finalOrderDocuments::add);
                    }
                } else if (document.getValue().getDocumentType().equals(SETTLEMENT_AGREEMENT)) {
                    if (Objects.nonNull(preTranslationDocuments)) {
                        Optional<Element<CaseDocument>> preTranslationSettlementAgreement = preTranslationDocuments.stream()
                            .filter(item -> item.getValue().getDocumentType() == DocumentType.SETTLEMENT_AGREEMENT)
                            .findFirst();
                        preTranslationSettlementAgreement.ifPresent(preTranslationDocuments::remove);
                        preTranslationSettlementAgreement.ifPresent(systemGeneratedDocuments::add);
                    }
                } else if (document.getValue().getDocumentType().equals(COURT_OFFICER_ORDER)) {
                    if (Objects.nonNull(preTranslationDocuments)) {
                        Optional<Element<CaseDocument>> preTranslationCourtOfficerOrder = preTranslationDocuments.stream()
                            .filter(item -> item.getValue().getDocumentType() == DocumentType.COURT_OFFICER_ORDER)
                            .findFirst();
                        preTranslationCourtOfficerOrder.ifPresent(preTranslationDocuments::remove);
                        preTranslationCourtOfficerOrder.ifPresent(courtOfficerOrderDocuments::add);
                    }
                } else if ((Objects.nonNull(preTranslatedDocuments) && !preTranslatedDocuments.isEmpty())) {
                    Element<CaseDocument> originalDocument = preTranslatedDocuments.remove(0);
                    if (originalDocument.getValue().getDocumentName().contains("claimant")) {
                        CaseDocument claimantSealedCopy = CaseDocument.toCaseDocument(originalDocument.getValue().getDocumentLink(),
                                                                                originalDocument.getValue().getDocumentType());
                        systemGeneratedDocuments.add(element(claimantSealedCopy));
                        assignCategoryId.assignCategoryIdToCaseDocument(claimantSealedCopy, DocCategory.APP1_DQ.getValue());
                    }
                }
            });
        }
        if (!courtOfficerOrderDocuments.isEmpty()) {
            caseData = caseData.toBuilder().previewCourtOfficerOrder(courtOfficerOrderDocuments.get(0).getValue()).build();
        }
        return caseData;
    }

    private CaseData.CaseDataBuilder<?, ?> updateDocumentCollectionsWithTranslationDocuments(CaseData caseData) {
        List<Element<TranslatedDocument>> translatedDocuments = caseData.getTranslatedDocuments();
        List<Element<TranslatedDocument>> addToSystemGenerated = new ArrayList<>();
        List<Element<TranslatedDocument>> addToFinalOrders = new ArrayList<>();
        List<Element<TranslatedDocument>> addToCourtOfficerOrders = new ArrayList<>();
        if (featureToggleService.isCaseProgressionEnabled() && Objects.nonNull(translatedDocuments)) {
            translatedDocuments.forEach(document -> {
                if (document.getValue().getDocumentType().equals(ORDER_NOTICE)) {
                    document.getValue().getFile().setCategoryID("orders");
                    addToSystemGenerated.add(document);
                } else if (document.getValue().getDocumentType().equals(STANDARD_DIRECTION_ORDER)) {
                    document.getValue().getFile().setCategoryID("caseManagementOrders");
                    addToSystemGenerated.add(document);
                } else if (document.getValue().getDocumentType().equals(FINAL_ORDER)) {
                    document.getValue().getFile().setCategoryID("caseManagementOrders");
                    addToFinalOrders.add(document);
                } else if (document.getValue().getDocumentType().equals(COURT_OFFICER_ORDER)) {
                    document.getValue().getFile().setCategoryID("caseManagementOrders");
                    addToCourtOfficerOrders.add(document);
                } else {
                    addToSystemGenerated.add(document);
                }
            });
        }
        List<Element<CaseDocument>> updatedSystemGeneratedDocuments =
            systemGeneratedDocumentService.getSystemGeneratedDocumentsWithAddedDocument(addToSystemGenerated, caseData);
        List<Element<CaseDocument>> updatedFinalOrderDocuments =
            systemGeneratedDocumentService.getFinalOrderDocumentsWithAddedDocument(addToFinalOrders, caseData);
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        if (!addToCourtOfficerOrders.isEmpty()) {
            CaseDocument translatedCourtOfficerOrder = CaseDocument.toCaseDocument(addToCourtOfficerOrders.get(0).getValue().getFile(),
                                                                    addToCourtOfficerOrders.get(0).getValue().getCorrespondingDocumentType(
                                                                        addToCourtOfficerOrders.get(0).getValue().getDocumentType()));
            caseDataBuilder.translatedCourtOfficerOrder(translatedCourtOfficerOrder);
        }
        caseDataBuilder
            .systemGeneratedCaseDocuments(updatedSystemGeneratedDocuments)
            .finalOrderDocumentCollection(updatedFinalOrderDocuments);
        return caseDataBuilder;
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
            } else if (Objects.nonNull(translatedDocuments)
                && (translatedDocuments.get(0).getValue().getDocumentType().equals(FINAL_ORDER))) {
                return CaseEvent.GENERATE_ORDER_NOTIFICATION;
            } else if (Objects.nonNull(translatedDocuments)
                && translatedDocuments.get(0).getValue().getDocumentType().equals(SETTLEMENT_AGREEMENT)) {
                return CaseEvent.UPLOAD_TRANSLATED_DOCUMENT_SETTLEMENT_AGREEMENT;
            }  else if (Objects.nonNull(translatedDocuments)
                && translatedDocuments.get(0).getValue().getDocumentType().equals(COURT_OFFICER_ORDER)) {
                return CaseEvent.COURT_OFFICER_ORDER;
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
