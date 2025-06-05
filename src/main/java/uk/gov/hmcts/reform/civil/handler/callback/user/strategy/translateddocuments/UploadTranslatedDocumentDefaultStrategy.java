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
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.COURT_OFFICER_ORDER;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.FINAL_ORDER;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.NOTICE_OF_DISCONTINUANCE_DEFENDANT;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.STANDARD_DIRECTION_ORDER;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.ORDER_NOTICE;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.INTERLOCUTORY_JUDGMENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPLOAD_TRANSLATED_DOCUMENT_HEARING_NOTICE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPLOAD_TRANSLATED_DOCUMENT_SETTLEMENT_AGREEMENT;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.SETTLEMENT_AGREEMENT;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.MANUAL_DETERMINATION;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.HEARING_NOTICE;

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
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        updateSystemGeneratedDocumentsWithOriginalDocuments(callbackParams, caseDataBuilder);
        CaseEvent businessProcessEvent = getBusinessProcessEvent(caseData);
        updateNoticeOfDiscontinuanceTranslatedDoc(callbackParams, caseDataBuilder);
        updateDocumentCollectionsWithTranslationDocuments(
            caseData, caseDataBuilder);
        CaseDataLiP caseDataLip = caseData.getCaseDataLiP();

        if (businessProcessEvent != null) {
            caseDataBuilder = caseDataBuilder.businessProcess(BusinessProcess.ready(businessProcessEvent));
        }

        if (Objects.nonNull(caseDataLip)) {
            caseDataLip.setTranslatedDocuments(null);
        }

        caseDataBuilder.caseDataLiP(caseDataLip);
        CaseData updatedCaseData = caseDataBuilder.build();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData.toMap(objectMapper))
            .build();
    }

    private void updateSystemGeneratedDocumentsWithOriginalDocuments(CallbackParams callbackParams,
                                                                     CaseData.CaseDataBuilder<?, ?> caseDataBuilder) {
        CaseData caseData = callbackParams.getCaseData();
        List<Element<TranslatedDocument>> translatedDocuments = caseData.getTranslatedDocuments();
        List<Element<CaseDocument>> preTranslatedDocuments = caseData.getPreTranslationDocuments();
        List<Element<CaseDocument>> sdoOrderDocuments = caseData.getPreTranslationSdoOrderDocuments();
        List<Element<CaseDocument>> preTranslationDocuments = caseData.getPreTranslationDocuments();
        List<Element<CaseDocument>> systemGeneratedDocuments = caseData.getSystemGeneratedCaseDocuments();
        List<Element<CaseDocument>> finalOrderDocuments = caseData.getFinalOrderDocumentCollection();
        List<Element<CaseDocument>> courtOfficerOrderDocuments = new ArrayList<>();
        List<Element<CaseDocument>> hearingDocuments = caseData.getHearingDocuments();
        if (featureToggleService.isCaseProgressionEnabled() && Objects.nonNull(translatedDocuments)) {
            translatedDocuments.forEach(document -> {
                if (document.getValue().getDocumentType().equals(STANDARD_DIRECTION_ORDER)) {
                    if (Objects.nonNull(sdoOrderDocuments) && !sdoOrderDocuments.isEmpty()) {
                        Element<CaseDocument> originalSdo = sdoOrderDocuments.remove(0);
                        systemGeneratedDocuments.add(originalSdo);
                    }
                } else if (document.getValue().getDocumentType().equals(INTERLOCUTORY_JUDGMENT)) {
                    if (Objects.nonNull(preTranslationDocuments)) {
                        Optional<Element<CaseDocument>> preTranslationInterlocJudgment =
                            preTranslationDocuments.stream()
                                .filter(item -> item.getValue().getDocumentType()
                                    == DocumentType.INTERLOCUTORY_JUDGEMENT)
                                .findFirst();
                        preTranslationInterlocJudgment.ifPresent(preTranslationDocuments::remove);
                        preTranslationInterlocJudgment.ifPresent(systemGeneratedDocuments::add);
                    }
                } else if (document.getValue().getDocumentType().equals(MANUAL_DETERMINATION)) {
                    if (Objects.nonNull(preTranslationDocuments)) {
                        Optional<Element<CaseDocument>> preTranslationManualDeterminationDoc =
                            preTranslationDocuments.stream()
                                .filter(item -> item.getValue().getDocumentType()
                                    == DocumentType.LIP_MANUAL_DETERMINATION)
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
                } else if (document.getValue().getDocumentType().equals(NOTICE_OF_DISCONTINUANCE_DEFENDANT)) {
                    Optional<Element<CaseDocument>> noticeOfDiscontinuanceOpt = preTranslationDocuments.stream()
                        .filter(item -> item.getValue().getDocumentType()
                            == DocumentType.NOTICE_OF_DISCONTINUANCE_DEFENDANT).findFirst();
                    noticeOfDiscontinuanceOpt.ifPresent(noticeOfDiscontinuance -> {
                        preTranslationDocuments.remove(noticeOfDiscontinuance);
                        if (!caseData.isJudgeOrderVerificationRequired()) {
                            caseDataBuilder.applicant1NoticeOfDiscontinueAllPartyViewDoc(caseData.getApplicant1NoticeOfDiscontinueCWViewDoc());
                            caseDataBuilder.applicant1NoticeOfDiscontinueCWViewDoc(null);
                            caseDataBuilder.respondent1NoticeOfDiscontinueCWViewDoc(null);
                            caseDataBuilder.respondent1NoticeOfDiscontinueAllPartyViewDoc(noticeOfDiscontinuance.getValue());
                        }
                    });
                } else if (document.getValue().getDocumentType().equals(SETTLEMENT_AGREEMENT)) {
                    if (Objects.nonNull(preTranslationDocuments)) {
                        Optional<Element<CaseDocument>> preTranslationSettlementAgreement =
                            preTranslationDocuments.stream()
                                .filter(item -> item.getValue().getDocumentType() == DocumentType.SETTLEMENT_AGREEMENT)
                                .findFirst();
                        preTranslationSettlementAgreement.ifPresent(preTranslationDocuments::remove);
                        preTranslationSettlementAgreement.ifPresent(systemGeneratedDocuments::add);
                    }
                } else if (document.getValue().getDocumentType().equals(COURT_OFFICER_ORDER)) {
                    if (Objects.nonNull(preTranslationDocuments)) {
                        Optional<Element<CaseDocument>> preTranslationCourtOfficerOrder =
                            preTranslationDocuments.stream()
                                .filter(item -> item.getValue().getDocumentType() == DocumentType.COURT_OFFICER_ORDER)
                                .findFirst();
                        preTranslationCourtOfficerOrder.ifPresent(preTranslationDocuments::remove);
                        preTranslationCourtOfficerOrder.ifPresent(courtOfficerOrderDocuments::add);
                    }
                } else if (document.getValue().getDocumentType().equals(HEARING_NOTICE)) {
                    if (Objects.nonNull(preTranslationDocuments)) {
                        Optional<Element<CaseDocument>> preTranslationHearingForm = preTranslationDocuments.stream()
                            .filter(item -> item.getValue().getDocumentType() == DocumentType.HEARING_FORM)
                            .findFirst();
                        preTranslationHearingForm.ifPresent(preTranslationDocuments::remove);
                        preTranslationHearingForm.ifPresent(hearingDocuments::add);
                    }
                } else if ((Objects.nonNull(preTranslatedDocuments) && !preTranslatedDocuments.isEmpty())) {
                    Element<CaseDocument> originalDocument = preTranslatedDocuments.remove(0);
                    if (Objects.nonNull(originalDocument.getValue().getDocumentName())
                        && originalDocument.getValue().getDocumentName().contains("claimant")) {
                        CaseDocument claimantSealedCopy = CaseDocument.toCaseDocument(
                            originalDocument.getValue().getDocumentLink(),
                            originalDocument.getValue().getDocumentType()
                        );
                        systemGeneratedDocuments.add(element(claimantSealedCopy));
                        assignCategoryId.assignCategoryIdToCaseDocument(
                            claimantSealedCopy,
                            DocCategory.APP1_DQ.getValue()
                        );
                    } else if (originalDocument.getValue().getDocumentType() != DocumentType.SEALED_CLAIM) {
                        systemGeneratedDocuments.add(originalDocument);
                    }
                }

            });
        }
        if (!courtOfficerOrderDocuments.isEmpty()) {
            caseDataBuilder.previewCourtOfficerOrder(courtOfficerOrderDocuments.get(0).getValue());
        }
    }

    private void updateNoticeOfDiscontinuanceTranslatedDoc(CallbackParams callbackParams,
                                                           CaseData.CaseDataBuilder<?, ?> caseDataBuilder) {
        CaseData caseData = callbackParams.getCaseData();
        List<Element<TranslatedDocument>> translatedDocuments = caseData.getTranslatedDocuments();
        Iterator<Element<TranslatedDocument>> iterator = translatedDocuments.iterator();
        while (iterator.hasNext()) {
            Element<TranslatedDocument> translateDocument = iterator.next();
            if (translateDocument.getValue().getDocumentType().equals(NOTICE_OF_DISCONTINUANCE_DEFENDANT)) {
                translateDocument.getValue().getFile().setCategoryID(DocCategory.NOTICE_OF_DISCONTINUE.getValue());
                caseDataBuilder.respondent1NoticeOfDiscontinueAllPartyTranslatedDoc(CaseDocument.toCaseDocument(
                    translateDocument.getValue().getFile(),
                    translateDocument.getValue()
                        .getCorrespondingDocumentType(translateDocument.getValue().getDocumentType())
                ));
                iterator.remove();
            }
        }
    }

    private void updateDocumentCollectionsWithTranslationDocuments(CaseData caseData,
                                                                   CaseData.CaseDataBuilder<?, ?> caseDataBuilder) {
        List<Element<TranslatedDocument>> translatedDocuments = caseData.getTranslatedDocuments();
        List<Element<TranslatedDocument>> addToSystemGenerated = new ArrayList<>();
        List<Element<TranslatedDocument>> addToHearingDocuments = new ArrayList<>();
        List<Element<TranslatedDocument>> addToFinalOrders = new ArrayList<>();
        List<Element<TranslatedDocument>> addToCourtOfficerOrders = new ArrayList<>();
        if (featureToggleService.isCaseProgressionEnabled() && Objects.nonNull(translatedDocuments)) {
            translatedDocuments.forEach(document -> {
                if (document.getValue().getDocumentType().equals(ORDER_NOTICE)) {
                    document.getValue().getFile().setCategoryID("orders");
                    addToSystemGenerated.add(document);
                } else if (document.getValue().getDocumentType().equals(STANDARD_DIRECTION_ORDER)) {
                    document.getValue().getFile().setCategoryID("caseManagementOrders");
                } else if (document.getValue().getDocumentType().equals(HEARING_NOTICE)) {
                    document.getValue().getFile().setCategoryID("hearingNotices");
                    addToHearingDocuments.add(document);
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
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();

        if (!addToCourtOfficerOrders.isEmpty()) {
            CaseDocument translatedCourtOfficerOrder = CaseDocument.toCaseDocument(
                addToCourtOfficerOrders.get(0).getValue().getFile(),
                addToCourtOfficerOrders.get(0).getValue().getCorrespondingDocumentType(
                    addToCourtOfficerOrders.get(0).getValue().getDocumentType())
            );
            caseDataBuilder.translatedCourtOfficerOrder(translatedCourtOfficerOrder);
        }

        if (!addToHearingDocuments.isEmpty()) {
            List<Element<CaseDocument>> updatedHearingDocumentsWelsh =
                systemGeneratedDocumentService.getHearingDocumentsWithAddedDocumentWelsh(
                    addToHearingDocuments,
                    caseData
                );
            caseDataBuilder.hearingDocumentsWelsh(updatedHearingDocumentsWelsh);
        }
        List<Element<CaseDocument>> updatedFinalOrderDocuments =
            systemGeneratedDocumentService.getFinalOrderDocumentsWithAddedDocument(addToFinalOrders, caseData);

        caseDataBuilder.systemGeneratedCaseDocuments(updatedSystemGeneratedDocuments);
        caseDataBuilder.finalOrderDocumentCollection(updatedFinalOrderDocuments);
        return caseDataBuilder;
    }

    private CaseEvent getBusinessProcessEvent(CaseData caseData) {
        if (featureToggleService.isCaseProgressionEnabled()) {
            List<Element<TranslatedDocument>> translatedDocuments = caseData.getTranslatedDocuments();

            if (Objects.nonNull(translatedDocuments) && translatedDocuments.size() > 0
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
            } else if (Objects.nonNull(translatedDocuments) && isContainsNoticeOfDiscontinuance(translatedDocuments)) {
                return caseData.isJudgeOrderVerificationRequired() ? null :
                    CaseEvent.UPLOAD_TRANSLATED_DISCONTINUANCE_DOC;
            } else if (Objects.nonNull(translatedDocuments)
                && translatedDocuments.get(0).getValue().getDocumentType().equals(SETTLEMENT_AGREEMENT)) {
                return CaseEvent.UPLOAD_TRANSLATED_DOCUMENT_SETTLEMENT_AGREEMENT;
            } else if (Objects.nonNull(translatedDocuments)
                && translatedDocuments.get(0).getValue().getDocumentType().equals(COURT_OFFICER_ORDER)) {
                return CaseEvent.COURT_OFFICER_ORDER;
            } else if (Objects.nonNull(translatedDocuments)
                && translatedDocuments.get(0).getValue().getDocumentType().equals(HEARING_NOTICE)) {
                return UPLOAD_TRANSLATED_DOCUMENT_HEARING_NOTICE;
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

    private boolean isContainsNoticeOfDiscontinuance(List<Element<TranslatedDocument>> translatedDocuments) {
        return translatedDocuments.stream()
            .map(Element::getValue)
            .map(TranslatedDocument::getDocumentType)
            .anyMatch(type -> NOTICE_OF_DISCONTINUANCE_DEFENDANT.equals(type)
            );
    }
}
