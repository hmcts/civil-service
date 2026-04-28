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
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocument;
import uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.SystemGeneratedDocumentService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static uk.gov.hmcts.reform.civil.enums.DocCategory.DQ_DEF1;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.CLAIMANT_INTENTION;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.COURT_OFFICER_ORDER;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.DECISION_MADE_ON_APPLICATIONS;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.DEFENDANT_RESPONSE;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.FINAL_ORDER;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.HEARING_NOTICE;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.INTERLOCUTORY_JUDGMENT;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.MANUAL_DETERMINATION;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.NOTICE_OF_DISCONTINUANCE_DEFENDANT;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.ORDER_NOTICE;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.SETTLEMENT_AGREEMENT;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.STANDARD_DIRECTION_ORDER;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@Component
@RequiredArgsConstructor
public class UploadTranslatedDocumentDefaultStrategy implements UploadTranslatedDocumentStrategy {

    private static final String TRANSLATED = "Translated_%s.%s";
    private static final Map<TranslatedDocumentType, CaseEvent> DOCUMENT_SPECIFIC_BUSINESS_PROCESS_EVENTS = Map.of(
        ORDER_NOTICE, CaseEvent.UPLOAD_TRANSLATED_DOCUMENT_ORDER_NOTICE,
        STANDARD_DIRECTION_ORDER, CaseEvent.UPLOAD_TRANSLATED_DOCUMENT_SDO,
        INTERLOCUTORY_JUDGMENT, CaseEvent.UPLOAD_TRANSLATED_DOCUMENT_CLAIMANT_REJECTS_REPAYMENT_PLAN,
        MANUAL_DETERMINATION, CaseEvent.UPLOAD_TRANSLATED_DOCUMENT_CLAIMANT_REJECTS_REPAYMENT_PLAN,
        FINAL_ORDER, CaseEvent.UPLOAD_TRANSLATED_DOCUMENT_ORDER,
        SETTLEMENT_AGREEMENT, CaseEvent.UPLOAD_TRANSLATED_DOCUMENT_SETTLEMENT_AGREEMENT,
        COURT_OFFICER_ORDER, CaseEvent.COURT_OFFICER_ORDER,
        DECISION_MADE_ON_APPLICATIONS, CaseEvent.DECISION_ON_RECONSIDERATION_REQUEST,
        HEARING_NOTICE, CaseEvent.UPLOAD_TRANSLATED_DOCUMENT_HEARING_NOTICE
    );
    private static final Set<TranslatedDocumentType> FIRST_DOCUMENT_PRIORITY_TYPES = Set.of(
        ORDER_NOTICE,
        STANDARD_DIRECTION_ORDER,
        INTERLOCUTORY_JUDGMENT,
        MANUAL_DETERMINATION,
        FINAL_ORDER
    );
    private final SystemGeneratedDocumentService systemGeneratedDocumentService;
    private final ObjectMapper objectMapper;
    private final AssignCategoryId assignCategoryId;
    private final FeatureToggleService featureToggleService;
    private final DeadlinesCalculator deadlinesCalculator;
    private static final String CATEGORY_ID = "caseManagementOrders";

    @Override
    public CallbackResponse uploadDocument(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        AboutToStartOrSubmitCallbackResponse.AboutToStartOrSubmitCallbackResponseBuilder
            aboutToStartOrSubmitCallbackResponseBuilder = AboutToStartOrSubmitCallbackResponse.builder();
        updateSystemGeneratedDocumentsWithOriginalDocuments(
            callbackParams,
            aboutToStartOrSubmitCallbackResponseBuilder
        );
        CaseEvent businessProcessEvent = getBusinessProcessEvent(caseData);
        updateNoticeOfDiscontinuanceTranslatedDoc(callbackParams);
        updateDocumentCollectionsWithTranslationDocuments(
            caseData);
        CaseDataLiP caseDataLip = caseData.getCaseDataLiP();

        if (businessProcessEvent != null) {
            caseData.setBusinessProcess(BusinessProcess.ready(businessProcessEvent));
        }

        if (Objects.nonNull(caseDataLip)) {
            caseDataLip.setTranslatedDocuments(null);
        }

        if (Objects.nonNull(caseData.getPreTranslationDocumentType())) {
            caseData.setPreTranslationDocumentType(null);
        }

        if (Objects.nonNull(caseData.getBilingualHint())) {
            caseData.setBilingualHint(YesOrNo.NO);
        }

        caseData.setCaseDataLiP(caseDataLip);

        aboutToStartOrSubmitCallbackResponseBuilder.data(caseData.toMap(objectMapper));
        return aboutToStartOrSubmitCallbackResponseBuilder.build();
    }

    private void updateSystemGeneratedDocumentsWithOriginalDocuments(CallbackParams callbackParams,
                                                                     AboutToStartOrSubmitCallbackResponse.AboutToStartOrSubmitCallbackResponseBuilder
                                                                         aboutToStartOrSubmitCallbackResponseBuilder) {
        CaseData caseData = callbackParams.getCaseData();
        List<Element<TranslatedDocument>> translatedDocuments = caseData.getTranslatedDocuments();
        List<Element<CaseDocument>> preTranslationDocuments = caseData.getPreTranslationDocuments();
        List<Element<CaseDocument>> courtOfficerOrderDocuments = new ArrayList<>();

        if (translatedDocuments != null) {
            translatedDocuments.forEach(document -> processTranslatedDocument(
                caseData,
                aboutToStartOrSubmitCallbackResponseBuilder,
                preTranslationDocuments,
                courtOfficerOrderDocuments,
                document
            ));
        }

        updateCourtOfficerOrderDocuments(caseData, courtOfficerOrderDocuments);
        updateDefendantResponseData(caseData, translatedDocuments);
    }

    private void updateNoticeOfDiscontinuanceTranslatedDoc(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        List<Element<TranslatedDocument>> translatedDocuments = caseData.getTranslatedDocuments();
        if (translatedDocuments == null) {
            return;
        }
        Iterator<Element<TranslatedDocument>> iterator = translatedDocuments.iterator();
        while (iterator.hasNext()) {
            Element<TranslatedDocument> translateDocument = iterator.next();
            if (translateDocument.getValue().getDocumentType().equals(NOTICE_OF_DISCONTINUANCE_DEFENDANT)) {
                translateDocument.getValue().getFile().setCategoryID(DocCategory.NOTICE_OF_DISCONTINUE.getValue());
                caseData.setRespondent1NoticeOfDiscontinueAllPartyTranslatedDoc(CaseDocument.toCaseDocument(
                    translateDocument.getValue().getFile(),
                    translateDocument.getValue()
                        .getCorrespondingDocumentType(translateDocument.getValue().getDocumentType())
                ));
                iterator.remove();
            }
        }
    }

    private void updateDocumentCollectionsWithTranslationDocuments(CaseData caseData) {
        List<Element<TranslatedDocument>> translatedDocuments = caseData.getTranslatedDocuments();
        List<Element<TranslatedDocument>> addToSystemGenerated = new ArrayList<>();
        List<Element<TranslatedDocument>> addToHearingDocuments = new ArrayList<>();
        List<Element<TranslatedDocument>> addToFinalOrders = new ArrayList<>();
        List<Element<TranslatedDocument>> addToCourtOfficerOrders = new ArrayList<>();
        if (Objects.nonNull(translatedDocuments)) {
            translatedDocuments.forEach(document -> {
                if (document.getValue().getDocumentType().equals(ORDER_NOTICE)) {
                    document.getValue().getFile().setCategoryID("orders");
                    addToSystemGenerated.add(document);
                } else if (document.getValue().getDocumentType().equals(STANDARD_DIRECTION_ORDER)) {
                    document.getValue().getFile().setCategoryID(CATEGORY_ID);
                    addToSystemGenerated.add(document);
                } else if (document.getValue().getDocumentType().equals(HEARING_NOTICE)) {
                    document.getValue().getFile().setCategoryID("hearingNotices");
                    addToHearingDocuments.add(document);
                } else if (document.getValue().getDocumentType().equals(FINAL_ORDER)) {
                    document.getValue().getFile().setCategoryID(CATEGORY_ID);
                    addToFinalOrders.add(document);
                } else if (document.getValue().getDocumentType().equals(COURT_OFFICER_ORDER)) {
                    document.getValue().getFile().setCategoryID(CATEGORY_ID);
                    addToCourtOfficerOrders.add(document);
                } else if (isTranslationForLipVsLRDefendantSealedForm(document, caseData)) {
                    document.getValue().getFile().setCategoryID(DQ_DEF1.getValue());
                    addToSystemGenerated.add(document);
                } else if (isTranslationForLrVsLipApplicantDq(document, caseData)) {
                    document.getValue().getFile().setCategoryID(DocCategory.APP1_DQ.getValue());
                    addToSystemGenerated.add(document);
                } else {
                    addToSystemGenerated.add(document);
                }
            });
        }
        List<Element<CaseDocument>> updatedSystemGeneratedDocuments =
            systemGeneratedDocumentService.getSystemGeneratedDocumentsWithAddedDocument(addToSystemGenerated, caseData);

        if (!addToCourtOfficerOrders.isEmpty()) {
            List<Element<CaseDocument>> updatedCourtOfficeOrder =
                systemGeneratedDocumentService.getCourtOfficerOrdersWithAddedDocument(addToCourtOfficerOrders, caseData);
            caseData.setCourtOfficersOrders(updatedCourtOfficeOrder);
        }

        if (!addToHearingDocuments.isEmpty()) {
            List<Element<CaseDocument>> updatedHearingDocumentsWelsh =
                systemGeneratedDocumentService.getHearingDocumentsWithAddedDocumentWelsh(
                    addToHearingDocuments,
                    caseData
                );
            caseData.setHearingDocumentsWelsh(updatedHearingDocumentsWelsh);
        }
        List<Element<CaseDocument>> updatedFinalOrderDocuments =
            systemGeneratedDocumentService.getFinalOrderDocumentsWithAddedDocument(addToFinalOrders, caseData);

        caseData.setSystemGeneratedCaseDocuments(updatedSystemGeneratedDocuments);
        caseData.setFinalOrderDocumentCollection(updatedFinalOrderDocuments);
    }

    private CaseEvent getBusinessProcessEvent(CaseData caseData) {
        return Optional.ofNullable(getTranslatedDocumentBusinessProcessEvent(caseData))
            .orElseGet(() -> Optional.ofNullable(getLipBusinessProcessEvent(caseData))
                .orElse(getFallbackBusinessProcessEvent(caseData)));
    }

    private boolean isContainsSpecifiedDocType(List<Element<TranslatedDocument>> translatedDocuments,
                                               TranslatedDocumentType translatedDocumentType) {
        return translatedDocuments != null && translatedDocuments.stream()
            .map(Element::getValue)
            .map(TranslatedDocument::getDocumentType)
            .anyMatch(translatedDocumentType::equals
            );
    }

    private Optional<Element<CaseDocument>> getPreTranslationDocumentBasedOnDocType(
        List<Element<CaseDocument>> preTranslationDocuments, DocumentType documentType) {

        return Optional.ofNullable(preTranslationDocuments)
            .orElse(Collections.emptyList())
            .stream()
            .filter(Objects::nonNull)
            .filter(item -> item.getValue() != null && documentType.equals(item.getValue().getDocumentType()))
            .findFirst();

    }

    private boolean isTranslationForLipVsLRDefendantSealedForm(Element<TranslatedDocument> document,
                                                               CaseData caseData) {
        return DEFENDANT_RESPONSE.equals(document.getValue().getDocumentType())
            && caseData.isLipvLROneVOne() && featureToggleService.isWelshEnabledForMainCase()
            && CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT.equals(caseData.getCcdState());
    }

    private boolean isTranslationForLrVsLipApplicantDq(Element<TranslatedDocument> document,
                                                        CaseData caseData) {
        return CLAIMANT_INTENTION.equals(document.getValue().getDocumentType())
            && caseData.isLRvLipOneVOne() && featureToggleService.isWelshEnabledForMainCase()
            && CaseState.AWAITING_APPLICANT_INTENTION.equals(caseData.getCcdState());
    }

    private String getFileType(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return fileName.substring(dotIndex + 1);
    }

    private String getBaseFileName(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return fileName.substring(0, dotIndex);
    }

    private void renameTranslatedDocument(Element<CaseDocument> originalDocument,
                                          Element<TranslatedDocument> translatedDocument) {
        renameTranslatedDocument(originalDocument.getValue().getDocumentName(), translatedDocument);
    }

    private void renameTranslatedDocument(String originalFileName,
                                          Element<TranslatedDocument> translatedDocument) {
        translatedDocument.getValue().getFile().setDocumentFileName(
            String.format(
                TRANSLATED,
                getBaseFileName(originalFileName),
                getFileType(translatedDocument.getValue().getFile().getDocumentFileName())
            )
        );
    }

    private void processTranslatedDocument(CaseData caseData,
                                           AboutToStartOrSubmitCallbackResponse.AboutToStartOrSubmitCallbackResponseBuilder
                                               aboutToStartOrSubmitCallbackResponseBuilder,
                                           List<Element<CaseDocument>> preTranslationDocuments,
                                           List<Element<CaseDocument>> courtOfficerOrderDocuments,
                                           Element<TranslatedDocument> document) {
        if (moveSpecialDocument(
            caseData,
            aboutToStartOrSubmitCallbackResponseBuilder,
            preTranslationDocuments,
            courtOfficerOrderDocuments,
            document
        )) {
            return;
        }
        moveFirstPreTranslationDocument(caseData, preTranslationDocuments, document);
    }

    private boolean moveSpecialDocument(CaseData caseData,
                                        AboutToStartOrSubmitCallbackResponse.AboutToStartOrSubmitCallbackResponseBuilder
                                            aboutToStartOrSubmitCallbackResponseBuilder,
                                        List<Element<CaseDocument>> preTranslationDocuments,
                                        List<Element<CaseDocument>> courtOfficerOrderDocuments,
                                        Element<TranslatedDocument> document) {
        if (isTranslationForLipVsLRDefendantSealedForm(document, caseData)) {
            moveDefendantSealedForm(
                aboutToStartOrSubmitCallbackResponseBuilder,
                caseData,
                preTranslationDocuments,
                document
            );
            return true;
        }

        return switch (document.getValue().getDocumentType()) {
            case STANDARD_DIRECTION_ORDER -> {
                movePreTranslationDocument(
                    preTranslationDocuments,
                    DocumentType.SDO_ORDER,
                    document,
                    caseData.getSystemGeneratedCaseDocuments()
                );
                yield true;
            }
            case INTERLOCUTORY_JUDGMENT -> {
                movePreTranslationDocument(
                    preTranslationDocuments,
                    DocumentType.INTERLOCUTORY_JUDGEMENT,
                    document,
                    caseData.getSystemGeneratedCaseDocuments()
                );
                yield true;
            }
            case MANUAL_DETERMINATION -> {
                movePreTranslationDocument(
                    preTranslationDocuments,
                    DocumentType.LIP_MANUAL_DETERMINATION,
                    document,
                    caseData.getSystemGeneratedCaseDocuments()
                );
                yield true;
            }
            case FINAL_ORDER -> {
                movePreTranslationDocumentFromLink(
                    preTranslationDocuments,
                    DocumentType.JUDGE_FINAL_ORDER,
                    document,
                    caseData.getFinalOrderDocumentCollection()
                );
                yield true;
            }
            case NOTICE_OF_DISCONTINUANCE_DEFENDANT -> {
                moveNoticeOfDiscontinuanceDocument(caseData, preTranslationDocuments, document);
                yield true;
            }
            case SETTLEMENT_AGREEMENT -> {
                movePreTranslationDocument(
                    preTranslationDocuments,
                    DocumentType.SETTLEMENT_AGREEMENT,
                    document,
                    caseData.getSystemGeneratedCaseDocuments()
                );
                yield true;
            }
            case COURT_OFFICER_ORDER -> {
                moveCourtOfficerOrder(caseData, preTranslationDocuments, courtOfficerOrderDocuments, document);
                yield true;
            }
            case DECISION_MADE_ON_APPLICATIONS -> {
                movePreTranslationDocument(
                    preTranslationDocuments,
                    DocumentType.DECISION_MADE_ON_APPLICATIONS,
                    document,
                    caseData.getSystemGeneratedCaseDocuments()
                );
                yield true;
            }
            case HEARING_NOTICE -> {
                movePreTranslationDocument(
                    preTranslationDocuments,
                    DocumentType.HEARING_FORM,
                    document,
                    caseData.getHearingDocuments()
                );
                yield true;
            }
            default -> false;
        };
    }

    private void movePreTranslationDocument(List<Element<CaseDocument>> preTranslationDocuments,
                                            DocumentType documentType,
                                            Element<TranslatedDocument> translatedDocument,
                                            List<Element<CaseDocument>> destinationDocuments) {
        getPreTranslationDocumentBasedOnDocType(preTranslationDocuments, documentType)
            .ifPresent(originalDocument -> {
                renameTranslatedDocument(originalDocument, translatedDocument);
                preTranslationDocuments.remove(originalDocument);
                destinationDocuments.add(originalDocument);
            });
    }

    private void movePreTranslationDocumentFromLink(List<Element<CaseDocument>> preTranslationDocuments,
                                                    DocumentType documentType,
                                                    Element<TranslatedDocument> translatedDocument,
                                                    List<Element<CaseDocument>> destinationDocuments) {
        getPreTranslationDocumentBasedOnDocType(preTranslationDocuments, documentType)
            .ifPresent(originalDocument -> {
                renameTranslatedDocument(
                    originalDocument.getValue().getDocumentLink().getDocumentFileName(),
                    translatedDocument
                );
                preTranslationDocuments.remove(originalDocument);
                destinationDocuments.add(originalDocument);
            });
    }

    private void moveNoticeOfDiscontinuanceDocument(CaseData caseData,
                                                    List<Element<CaseDocument>> preTranslationDocuments,
                                                    Element<TranslatedDocument> document) {
        getPreTranslationDocumentBasedOnDocType(preTranslationDocuments, DocumentType.NOTICE_OF_DISCONTINUANCE_DEFENDANT)
            .ifPresent(noticeOfDiscontinuance -> {
                renameTranslatedDocument(noticeOfDiscontinuance, document);
                preTranslationDocuments.remove(noticeOfDiscontinuance);
                caseData.setApplicant1NoticeOfDiscontinueAllPartyViewDoc(caseData.getApplicant1NoticeOfDiscontinueCWViewDoc());
                caseData.setApplicant1NoticeOfDiscontinueCWViewDoc(null);
                caseData.setRespondent1NoticeOfDiscontinueCWViewDoc(null);
                caseData.setRespondent1NoticeOfDiscontinueAllPartyViewDoc(noticeOfDiscontinuance.getValue());
            });
    }

    private void moveCourtOfficerOrder(CaseData caseData,
                                       List<Element<CaseDocument>> preTranslationDocuments,
                                       List<Element<CaseDocument>> courtOfficerOrderDocuments,
                                       Element<TranslatedDocument> document) {
        if (preTranslationDocuments != null) {
            caseData.setUrgentFlag(null);
            movePreTranslationDocumentFromLink(
                preTranslationDocuments,
                DocumentType.COURT_OFFICER_ORDER,
                document,
                courtOfficerOrderDocuments
            );
        }
    }

    private void moveDefendantSealedForm(AboutToStartOrSubmitCallbackResponse.AboutToStartOrSubmitCallbackResponseBuilder
                                             aboutToStartOrSubmitCallbackResponseBuilder,
                                         CaseData caseData,
                                         List<Element<CaseDocument>> preTranslationDocuments,
                                         Element<TranslatedDocument> document) {
        getPreTranslationDocumentBasedOnDocType(preTranslationDocuments, DocumentType.SEALED_CLAIM)
            .ifPresent(sealedForm -> {
                renameTranslatedDocument(sealedForm, document);
                preTranslationDocuments.remove(sealedForm);
                caseData.getSystemGeneratedCaseDocuments().add(sealedForm);
                aboutToStartOrSubmitCallbackResponseBuilder.state(CaseState.AWAITING_APPLICANT_INTENTION.toString());
            });
    }

    private void moveFirstPreTranslationDocument(CaseData caseData,
                                                 List<Element<CaseDocument>> preTranslationDocuments,
                                                 Element<TranslatedDocument> document) {
        if (preTranslationDocuments == null || preTranslationDocuments.isEmpty()) {
            return;
        }

        Element<CaseDocument> originalDocument = preTranslationDocuments.removeFirst();
        renameTranslatedDocument(originalDocument, document);

        if (isClaimantDocument(originalDocument)) {
            addClaimantSealedCopy(caseData, originalDocument);
            return;
        }

        if (shouldCopyOriginalDocument(originalDocument)) {
            if (DocumentType.DEFENDANT_DEFENCE.equals(originalDocument.getValue().getDocumentType())) {
                caseData.setRespondent1ClaimResponseDocumentSpec(originalDocument.getValue());
            }
            caseData.getSystemGeneratedCaseDocuments().add(originalDocument);
        }
    }

    private boolean isClaimantDocument(Element<CaseDocument> originalDocument) {
        return Objects.nonNull(originalDocument.getValue().getDocumentName())
            && originalDocument.getValue().getDocumentName().contains("claimant");
    }

    private void addClaimantSealedCopy(CaseData caseData, Element<CaseDocument> originalDocument) {
        CaseDocument claimantSealedCopy = CaseDocument.toCaseDocument(
            originalDocument.getValue().getDocumentLink(),
            originalDocument.getValue().getDocumentType()
        );
        caseData.getSystemGeneratedCaseDocuments().add(element(claimantSealedCopy));
        assignCategoryId.assignCategoryIdToCaseDocument(
            claimantSealedCopy,
            DocCategory.APP1_DQ.getValue()
        );
    }

    private boolean shouldCopyOriginalDocument(Element<CaseDocument> originalDocument) {
        return originalDocument.getValue().getDocumentType() != DocumentType.SEALED_CLAIM
            || featureToggleService.isWelshEnabledForMainCase();
    }

    private void updateCourtOfficerOrderDocuments(CaseData caseData,
                                                  List<Element<CaseDocument>> courtOfficerOrderDocuments) {
        if (!courtOfficerOrderDocuments.isEmpty()) {
            caseData.setCourtOfficersOrders(courtOfficerOrderDocuments);
        }
    }

    private void updateDefendantResponseData(CaseData caseData,
                                             List<Element<TranslatedDocument>> translatedDocuments) {
        boolean isDefendantResponse = isContainsSpecifiedDocType(translatedDocuments, DEFENDANT_RESPONSE);
        if (featureToggleService.isWelshEnabledForMainCase()
            && caseData.getRespondent1OriginalDqDoc() != null
            && isDefendantResponse) {
            caseData.getSystemGeneratedCaseDocuments().add(element(caseData.getRespondent1OriginalDqDoc()));
            caseData.setRespondent1OriginalDqDoc(null);
        }
        if (isDefendantResponse) {
            LocalDateTime applicant1ResponseDeadline =
                deadlinesCalculator.calculateApplicantResponseDeadlineSpec(LocalDateTime.now());
            caseData.setApplicant1ResponseDeadline(applicant1ResponseDeadline);
            caseData.setNextDeadline(applicant1ResponseDeadline.toLocalDate());
        }
    }

    private CaseEvent getTranslatedDocumentBusinessProcessEvent(CaseData caseData) {
        List<Element<TranslatedDocument>> translatedDocuments = caseData.getTranslatedDocuments();
        return translatedDocuments == null || translatedDocuments.isEmpty()
            ? null
            : getDocumentSpecificBusinessProcessEvent(caseData, translatedDocuments);
    }

    private CaseEvent getDocumentSpecificBusinessProcessEvent(CaseData caseData,
                                                              List<Element<TranslatedDocument>> translatedDocuments) {
        TranslatedDocumentType firstDocumentType = translatedDocuments.getFirst().getValue().getDocumentType();
        if (FIRST_DOCUMENT_PRIORITY_TYPES.contains(firstDocumentType)) {
            return DOCUMENT_SPECIFIC_BUSINESS_PROCESS_EVENTS.get(firstDocumentType);
        }
        if (isContainsSpecifiedDocType(translatedDocuments, NOTICE_OF_DISCONTINUANCE_DEFENDANT)) {
            return CaseEvent.UPLOAD_TRANSLATED_DISCONTINUANCE_DOC;
        }
        CaseEvent documentSpecificEvent = DOCUMENT_SPECIFIC_BUSINESS_PROCESS_EVENTS.get(firstDocumentType);
        if (documentSpecificEvent != null) {
            return documentSpecificEvent;
        }
        return isDefendantSealedFormBusinessProcessEvent(caseData, translatedDocuments)
            ? CaseEvent.UPLOAD_TRANSLATED_DEFENDANT_SEALED_FORM
            : null;
    }

    private CaseEvent getLipBusinessProcessEvent(CaseData caseData) {
        if ((caseData.isLipvLipOneVOne())
            || (caseData.isLipvLROneVOne() && featureToggleService.isDefendantNoCOnlineForCase(caseData))) {
            if (caseData.getCcdState() == CaseState.PENDING_CASE_ISSUED) {
                return CaseEvent.UPLOAD_TRANSLATED_DOCUMENT_CLAIM_ISSUE;
            }
            if (caseData.getCcdState() == CaseState.AWAITING_APPLICANT_INTENTION) {
                return CaseEvent.UPLOAD_TRANSLATED_DOCUMENT_CLAIMANT_INTENTION;
            }
        }
        return null;
    }

    private CaseEvent getFallbackBusinessProcessEvent(CaseData caseData) {
        return caseData.isLRvLipOneVOne()
            && featureToggleService.isWelshEnabledForMainCase()
            && caseData.getCcdState() == CaseState.AWAITING_APPLICANT_INTENTION
            ? CaseEvent.UPLOAD_TRANSLATED_DOCUMENT_CLAIMANT_LR_INTENTION
            : CaseEvent.UPLOAD_TRANSLATED_DOCUMENT;
    }

    private boolean isDefendantSealedFormBusinessProcessEvent(CaseData caseData,
                                                              List<Element<TranslatedDocument>> translatedDocuments) {
        return isContainsSpecifiedDocType(translatedDocuments, DEFENDANT_RESPONSE)
            && caseData.isLipvLROneVOne()
            && featureToggleService.isWelshEnabledForMainCase();
    }
}
