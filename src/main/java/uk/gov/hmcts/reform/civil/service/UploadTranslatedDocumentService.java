package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentUploadException;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocument;
import uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;
import uk.gov.hmcts.reform.civil.utils.DocUploadUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPLOAD_TRANSLATED_DOCUMENT_GA_LIP;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.APPLICATION_SUMMARY_DOCUMENT;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.APPLICATION_SUMMARY_DOCUMENT_RESPONDED;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.APPROVE_OR_EDIT_ORDER;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.DISMISSAL_ORDER;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.GENERAL_ORDER;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.HEARING_NOTICE;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.HEARING_ORDER;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.JUDGES_DIRECTIONS_ORDER;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.REQUEST_FOR_MORE_INFORMATION_ORDER;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.REQUEST_MORE_INFORMATION_APPLICANT;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.REQUEST_MORE_INFORMATION_RESPONDENT;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.WRITTEN_REPRESENTATIONS_APPLICANT;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.WRITTEN_REPRESENTATIONS_ORDER_CONCURRENT;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.WRITTEN_REPRESENTATIONS_ORDER_SEQUENTIAL;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.WRITTEN_REPRESENTATIONS_RESPONDENT;

@RequiredArgsConstructor
@Service
public class UploadTranslatedDocumentService {

    private final AssignCategoryId assignCategoryId;
    private final GaForLipService gaForLipService;
    private final DocUploadDashboardNotificationService docUploadDashboardNotificationService;
    private final DeadlinesCalculator deadlinesCalculator;

    public CaseData.CaseDataBuilder<?, ?> processTranslatedDocument(CaseData caseData, String translator) {
        List<Element<TranslatedDocument>> translatedDocuments = caseData.getTranslatedDocumentsGA();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();

        if (Objects.nonNull(translatedDocuments)) {
            Map<DocumentType, List<Element<CaseDocument>>>
                    categorizedDocuments = processTranslatedDocuments(translatedDocuments, translator);

            categorizedDocuments.forEach((documentType, caseDocuments) -> {
                if (!caseDocuments.isEmpty()) {
                    List<Element<CaseDocument>> existingDocuments = getExistingDocumentsByType(caseData, documentType);
                    existingDocuments.addAll(caseDocuments);
                    assignCategoryId.assignCategoryIdToCollection(
                            existingDocuments,
                            document -> document.getValue().getDocumentLink(),
                            AssignCategoryId.APPLICATIONS
                    );
                    updateCaseDataBuilderByType(caseData, caseDataBuilder, documentType, existingDocuments);
                }
            });
        }
        // Copy to different field so XUI data can be cleared
        caseDataBuilder.translatedDocumentsBulkPrint(caseData.getTranslatedDocumentsGA());
        caseDataBuilder.translatedDocumentsGA(null);
        return caseDataBuilder;
    }

    private Map<DocumentType, List<Element<CaseDocument>>> processTranslatedDocuments(
            List<Element<TranslatedDocument>> translatedDocuments, String translator) {
        Map<DocumentType, List<Element<CaseDocument>>> categorizedDocuments = new HashMap<>();

        for (Element<TranslatedDocument> translatedDocumentElement : translatedDocuments) {
            TranslatedDocument translatedDocument = translatedDocumentElement.getValue();
            DocumentType documentType =
                    translatedDocument.getCorrespondingDocumentTypeGA(translatedDocument.getDocumentType());
            Document translatedDocumentFile = Document.toDocument(translatedDocument.getFile(), documentType);
            CaseDocument caseDocument = CaseDocument.toCaseDocumentGA(
                    translatedDocumentFile,
                    documentType,
                    translator
            );

            categorizedDocuments.computeIfAbsent(documentType, k -> new ArrayList<>())
                    .add(Element.<CaseDocument>builder().value(caseDocument).build());
        }

        return categorizedDocuments;
    }

    private List<Element<CaseDocument>> getExistingDocumentsByType(CaseData caseData, DocumentType documentType) {
        return switch (documentType) {
            case REQUEST_FOR_INFORMATION, SEND_APP_TO_OTHER_PARTY -> ofNullable(caseData.getRequestForInformationDocumentGA()).orElse(new ArrayList<>());
            case DIRECTION_ORDER -> ofNullable(caseData.getDirectionOrderDocumentGA()).orElse(new ArrayList<>());
            case GENERAL_ORDER -> ofNullable(caseData.getGeneralOrderDocumentGA()).orElse(new ArrayList<>());
            case HEARING_ORDER -> ofNullable(caseData.getHearingOrderDocumentGA()).orElse(new ArrayList<>());
            case HEARING_NOTICE -> ofNullable(caseData.getHearingNoticeDocumentGA()).orElse(new ArrayList<>());
            case DISMISSAL_ORDER -> ofNullable(caseData.getDismissalOrderDocumentGA()).orElse(new ArrayList<>());
            case WRITTEN_REPRESENTATION_CONCURRENT -> ofNullable(caseData.getWrittenRepConcurrentDocumentGA()).orElse(new ArrayList<>());
            case WRITTEN_REPRESENTATION_SEQUENTIAL -> ofNullable(caseData.getWrittenRepSequentialDocumentGA()).orElse(new ArrayList<>());
            case GENERAL_APPLICATION_DRAFT -> ofNullable(caseData.getGaDraftDocumentGA()).orElse(new ArrayList<>());
            default -> new ArrayList<>();
        };
    }

    private void updateCaseDataBuilderByType(CaseData caseData, CaseData.CaseDataBuilder<?, ?> caseDataBuilder,
                                             DocumentType documentType,
                                             List<Element<CaseDocument>> documents) {
        switch (documentType) {
            case DIRECTION_ORDER:
                caseDataBuilder.directionOrderDocumentGA(documents);
                break;
            case DISMISSAL_ORDER:
                caseDataBuilder.dismissalOrderDocumentGA(documents);
                break;
            case REQUEST_FOR_INFORMATION:
            case SEND_APP_TO_OTHER_PARTY:
                caseDataBuilder.requestForInformationDocumentGA(documents);
                break;
            case GENERAL_ORDER:
                caseDataBuilder.generalOrderDocumentGA(documents);
                break;
            case HEARING_ORDER:
                caseDataBuilder.hearingOrderDocumentGA(documents);
                break;
            case HEARING_NOTICE:
                caseDataBuilder.hearingNoticeDocumentGA(documents);
                break;
            case WRITTEN_REPRESENTATION_CONCURRENT:
                caseDataBuilder.writtenRepConcurrentDocumentGA(documents);
                break;
            case WRITTEN_REPRESENTATION_SEQUENTIAL:
                caseDataBuilder.writtenRepSequentialDocumentGA(documents);
                break;
            case GENERAL_APPLICATION_DRAFT:
                caseDataBuilder.gaDraftDocumentGA(documents);
                break;
            case REQUEST_MORE_INFORMATION_APPLICANT_TRANSLATED:
            case JUDGES_DIRECTIONS_APPLICANT_TRANSLATED:
            case WRITTEN_REPRESENTATION_APPLICANT_TRANSLATED:
            case UPLOADED_DOCUMENT_APPLICANT:
                DocUploadUtils.addToAddl(caseData, caseDataBuilder, documents, DocUploadUtils.APPLICANT, false);
                break;
            case REQUEST_MORE_INFORMATION_RESPONDENT_TRANSLATED:
            case JUDGES_DIRECTIONS_RESPONDENT_TRANSLATED:
            case WRITTEN_REPRESENTATION_RESPONDENT_TRANSLATED:
            case UPLOADED_DOCUMENT_RESPONDENT:
                DocUploadUtils.addToAddl(caseData, caseDataBuilder, documents, DocUploadUtils.RESPONDENT_ONE, false);
                break;
            default:
                throw new DocumentUploadException("No document file type found for Translated document");
        }
    }

    public void updateGADocumentsWithOriginalDocuments(CaseData.CaseDataBuilder<?, ?> caseDataBuilder) {
        List<Element<CaseDocument>> bulkPrintOriginalDocuments = newArrayList();
        List<Element<TranslatedDocument>> translatedDocuments = caseDataBuilder.build().getTranslatedDocumentsGA();
        List<Element<CaseDocument>> preTranslationGaDocuments = caseDataBuilder.build().getPreTranslationGaDocuments();
        List<Element<CaseDocument>> gaDraftDocumentGA;
        if (Objects.isNull(caseDataBuilder.build().getGaDraftDocumentGA())) {
            gaDraftDocumentGA = newArrayList();
        } else {
            gaDraftDocumentGA = caseDataBuilder.build().getGaDraftDocumentGA();
        }

        List<Element<CaseDocument>> generalOrderDocs = Objects.isNull(caseDataBuilder.build().getGeneralOrderDocumentGA())
                ? newArrayList() : caseDataBuilder.build().getGeneralOrderDocumentGA();

        List<Element<CaseDocument>> writtenRepsSequentialDocs = Objects.isNull(caseDataBuilder.build().getWrittenRepSequentialDocumentGA())
                ? newArrayList() : caseDataBuilder.build().getWrittenRepSequentialDocumentGA();
        List<Element<CaseDocument>> writtenRepsConcurrentDocs = Objects.isNull(caseDataBuilder.build().getWrittenRepConcurrentDocumentGA())
                ? newArrayList() : caseDataBuilder.build().getWrittenRepConcurrentDocumentGA();
        List<Element<CaseDocument>> hearingNoticeDocs = Objects.isNull(caseDataBuilder.build().getHearingNoticeDocumentGA())
                ? newArrayList() : caseDataBuilder.build().getHearingNoticeDocumentGA();
        List<Element<CaseDocument>> requestMoreInformationDocs = Objects.isNull(caseDataBuilder.build().getRequestForInformationDocumentGA())
                ? newArrayList() : caseDataBuilder.build().getRequestForInformationDocumentGA();
        List<Element<CaseDocument>> directionOrder = Objects.isNull(caseDataBuilder.build().getDirectionOrderDocumentGA())
                ? newArrayList() : caseDataBuilder.build().getDirectionOrderDocumentGA();
        List<Element<CaseDocument>> dismissalOrderDocs = Objects.isNull(caseDataBuilder.build().getDismissalOrderDocumentGA())
                ? newArrayList() : caseDataBuilder.build().getDismissalOrderDocumentGA();
        List<Element<CaseDocument>> hearingOrders = Objects.isNull(caseDataBuilder.build().getHearingOrderDocumentGA())
                ? newArrayList() : caseDataBuilder.build().getHearingOrderDocumentGA();
        List<Element<CaseDocument>> applicantPreTranslation = Objects.isNull(caseDataBuilder.build().getPreTranslationGaDocsApplicant())
                ? newArrayList() : caseDataBuilder.build().getPreTranslationGaDocsApplicant();
        List<Element<CaseDocument>> respondentPreTranslation = Objects.isNull(caseDataBuilder.build().getPreTranslationGaDocsRespondent())
                ? newArrayList() : caseDataBuilder.build().getPreTranslationGaDocsRespondent();

        if (Objects.nonNull(translatedDocuments)) {
            translatedDocuments.forEach(document -> {
                if (document.getValue().getDocumentType().equals(APPLICATION_SUMMARY_DOCUMENT)
                        || document.getValue().getDocumentType().equals(APPLICATION_SUMMARY_DOCUMENT_RESPONDED)) {
                    if (Objects.nonNull(preTranslationGaDocuments)) {
                        Optional<Element<CaseDocument>> preTranslationGADraftDocument = preTranslationGaDocuments.stream()
                                .filter(item -> item.getValue().getDocumentType() == DocumentType.GENERAL_APPLICATION_DRAFT)
                                .findFirst();
                        if (document.getValue().getDocumentType().equals(APPLICATION_SUMMARY_DOCUMENT)) {
                            caseDataBuilder.generalAppNotificationDeadlineDate(deadlinesCalculator
                                    .calculateApplicantResponseDeadline(LocalDateTime.now(), 5));
                        }
                        preTranslationGADraftDocument.ifPresent(preTranslationGaDocuments::remove);
                        preTranslationGADraftDocument.ifPresent(gaDraftDocumentGA::add);
                        caseDataBuilder.gaDraftDocumentGA(gaDraftDocumentGA);
                    }
                } else if (document.getValue().getDocumentType().equals(WRITTEN_REPRESENTATIONS_ORDER_SEQUENTIAL)) {
                    Optional<Element<CaseDocument>> preTranslationWrittenRepsSequential = preTranslationGaDocuments.stream()
                            .filter(item -> item.getValue().getDocumentType() == DocumentType.WRITTEN_REPRESENTATION_SEQUENTIAL)
                            .findFirst();
                    preTranslationWrittenRepsSequential.ifPresent(preTranslationGaDocuments::remove);
                    preTranslationWrittenRepsSequential.ifPresent(writtenRepsSequentialDocs::add);
                    preTranslationWrittenRepsSequential.ifPresent(bulkPrintOriginalDocuments::add);
                    caseDataBuilder.writtenRepSequentialDocumentGA(writtenRepsSequentialDocs);
                    caseDataBuilder.originalDocumentsBulkPrint(
                            bulkPrintOriginalDocuments.stream()
                                    .map(Element::getValue)
                                    .toList()
                    );
                } else if (document.getValue().getDocumentType().equals(WRITTEN_REPRESENTATIONS_ORDER_CONCURRENT)) {
                    Optional<Element<CaseDocument>> preTranslationWrittenRepsConcurrent = preTranslationGaDocuments.stream()
                            .filter(item -> item.getValue().getDocumentType() == DocumentType.WRITTEN_REPRESENTATION_CONCURRENT)
                            .findFirst();
                    preTranslationWrittenRepsConcurrent.ifPresent(preTranslationGaDocuments::remove);
                    preTranslationWrittenRepsConcurrent.ifPresent(writtenRepsConcurrentDocs::add);
                    preTranslationWrittenRepsConcurrent.ifPresent(bulkPrintOriginalDocuments::add);
                    caseDataBuilder.writtenRepConcurrentDocumentGA(writtenRepsConcurrentDocs);
                    caseDataBuilder.originalDocumentsBulkPrint(
                            bulkPrintOriginalDocuments.stream()
                                    .map(Element::getValue)
                                    .toList()
                    );
                } else if (document.getValue().getDocumentType().equals(GENERAL_ORDER)
                        || document.getValue().getDocumentType().equals(APPROVE_OR_EDIT_ORDER)) {
                    Optional<Element<CaseDocument>> preTranslationGeneralOrder = preTranslationGaDocuments.stream()
                            .filter(item -> item.getValue().getDocumentType() == DocumentType.GENERAL_ORDER)
                            .findFirst();
                    preTranslationGeneralOrder.ifPresent(preTranslationGaDocuments::remove);
                    preTranslationGeneralOrder.ifPresent(generalOrderDocs::add);
                    preTranslationGeneralOrder.ifPresent(bulkPrintOriginalDocuments::add);
                    caseDataBuilder.generalOrderDocumentGA(generalOrderDocs);
                    caseDataBuilder.originalDocumentsBulkPrint(
                            bulkPrintOriginalDocuments.stream()
                                    .map(Element::getValue)
                                    .toList()
                    );
                } else if (document.getValue().getDocumentType().equals(HEARING_NOTICE)) {
                    Optional<Element<CaseDocument>> preTranslationHearingNotice = preTranslationGaDocuments.stream()
                            .filter(item -> item.getValue().getDocumentType() == DocumentType.HEARING_NOTICE)
                            .findFirst();
                    preTranslationHearingNotice.ifPresent(preTranslationGaDocuments::remove);
                    preTranslationHearingNotice.ifPresent(hearingNoticeDocs::add);
                    preTranslationHearingNotice.ifPresent(bulkPrintOriginalDocuments::add);
                    caseDataBuilder.hearingNoticeDocumentGA(hearingNoticeDocs);
                    caseDataBuilder.originalDocumentsBulkPrint(
                            bulkPrintOriginalDocuments.stream()
                                    .map(Element::getValue)
                                    .toList()
                    );
                } else if (document.getValue().getDocumentType().equals(REQUEST_FOR_MORE_INFORMATION_ORDER)) {
                    Optional<Element<CaseDocument>> preTranslationRequestMoreInformation = preTranslationGaDocuments.stream()
                            .filter(item -> item.getValue().getDocumentType() == DocumentType.REQUEST_FOR_INFORMATION)
                            .findFirst();
                    preTranslationRequestMoreInformation.ifPresent(preTranslationGaDocuments::remove);
                    preTranslationRequestMoreInformation.ifPresent(requestMoreInformationDocs::add);
                    preTranslationRequestMoreInformation.ifPresent(bulkPrintOriginalDocuments::add);
                    caseDataBuilder.requestForInformationDocumentGA(requestMoreInformationDocs);
                    caseDataBuilder.originalDocumentsBulkPrint(
                            bulkPrintOriginalDocuments.stream()
                                    .map(Element::getValue)
                                    .toList()
                    );
                } else if (document.getValue().getDocumentType().equals(HEARING_ORDER)) {
                    Optional<Element<CaseDocument>> preTranslationHearingOrder = preTranslationGaDocuments.stream()
                            .filter(item -> item.getValue().getDocumentType() == DocumentType.HEARING_ORDER)
                            .findFirst();
                    preTranslationHearingOrder.ifPresent(preTranslationGaDocuments::remove);
                    preTranslationHearingOrder.ifPresent(hearingOrders::add);
                    preTranslationHearingOrder.ifPresent(bulkPrintOriginalDocuments::add);
                    caseDataBuilder.hearingOrderDocumentGA(hearingOrders);
                    caseDataBuilder.originalDocumentsBulkPrint(
                            bulkPrintOriginalDocuments.stream()
                                    .map(Element::getValue)
                                    .toList()
                    );
                } else if (document.getValue().getDocumentType().equals(JUDGES_DIRECTIONS_ORDER)) {
                    Optional<Element<CaseDocument>> preTranslationDirectionOrder = preTranslationGaDocuments.stream()
                            .filter(item -> item.getValue().getDocumentType() == DocumentType.DIRECTION_ORDER)
                            .findFirst();
                    preTranslationDirectionOrder.ifPresent(preTranslationGaDocuments::remove);
                    preTranslationDirectionOrder.ifPresent(directionOrder::add);
                    preTranslationDirectionOrder.ifPresent(bulkPrintOriginalDocuments::add);
                    caseDataBuilder.directionOrderDocumentGA(directionOrder);
                    caseDataBuilder.originalDocumentsBulkPrint(
                            bulkPrintOriginalDocuments.stream()
                                    .map(Element::getValue)
                                    .toList()
                    );
                } else if (document.getValue().getDocumentType().equals(DISMISSAL_ORDER)) {
                    Optional<Element<CaseDocument>> preTranslationDismissalOrder = preTranslationGaDocuments.stream()
                            .filter(item -> item.getValue().getDocumentType() == DocumentType.DISMISSAL_ORDER)
                            .findFirst();
                    preTranslationDismissalOrder.ifPresent(preTranslationGaDocuments::remove);
                    preTranslationDismissalOrder.ifPresent(dismissalOrderDocs::add);
                    preTranslationDismissalOrder.ifPresent(bulkPrintOriginalDocuments::add);
                    caseDataBuilder.dismissalOrderDocumentGA(dismissalOrderDocs);
                    caseDataBuilder.originalDocumentsBulkPrint(
                            bulkPrintOriginalDocuments.stream()
                                    .map(Element::getValue)
                                    .toList()
                    );
                } else if (document.getValue().getDocumentType().equals(WRITTEN_REPRESENTATIONS_APPLICANT)) {
                    Optional<Element<CaseDocument>> preTranslationWrittenRepsApplicant = preTranslationGaDocuments.stream()
                            .filter(item -> "Written representation".equals(item.getValue().getDocumentName())
                                    && DocUploadUtils.APPLICANT.equals(item.getValue().getCreatedBy()))
                            .findFirst();
                    preTranslationWrittenRepsApplicant.ifPresent(preTranslationGaDocuments::remove);
                    preTranslationWrittenRepsApplicant.ifPresent(element ->
                            DocUploadUtils.addToAddl(caseDataBuilder.build(), caseDataBuilder, List.of(element), DocUploadUtils.APPLICANT, false)
                    );
                    preTranslationWrittenRepsApplicant.ifPresent(applicantPreTranslation::remove);
                    caseDataBuilder.preTranslationGaDocsApplicant(applicantPreTranslation);
                } else if (document.getValue().getDocumentType().equals(WRITTEN_REPRESENTATIONS_RESPONDENT)) {
                    Optional<Element<CaseDocument>> preTranslationWrittenRepsRespondent = preTranslationGaDocuments.stream()
                            .filter(item -> "Written representation".equals(item.getValue().getDocumentName())
                                    && DocUploadUtils.RESPONDENT_ONE.equals(item.getValue().getCreatedBy()))
                            .findFirst();
                    preTranslationWrittenRepsRespondent.ifPresent(preTranslationGaDocuments::remove);
                    preTranslationWrittenRepsRespondent.ifPresent(element ->
                            DocUploadUtils.addToAddl(caseDataBuilder.build(), caseDataBuilder, List.of(element), DocUploadUtils.RESPONDENT_ONE, false)
                    );
                    preTranslationWrittenRepsRespondent.ifPresent(respondentPreTranslation::remove);
                    caseDataBuilder.preTranslationGaDocsRespondent(respondentPreTranslation);
                } else if (document.getValue().getDocumentType().equals(REQUEST_MORE_INFORMATION_APPLICANT)) {
                    Optional<Element<CaseDocument>> preTranslationMoreInfoApplicant = preTranslationGaDocuments.stream()
                            .filter(item -> "Additional information".equals(item.getValue().getDocumentName())
                                    && DocUploadUtils.APPLICANT.equals(item.getValue().getCreatedBy()))
                            .findFirst();
                    preTranslationMoreInfoApplicant.ifPresent(preTranslationGaDocuments::remove);
                    preTranslationMoreInfoApplicant.ifPresent(element ->
                            DocUploadUtils.addToAddl(caseDataBuilder.build(), caseDataBuilder, List.of(element), DocUploadUtils.APPLICANT, false)
                    );
                    preTranslationMoreInfoApplicant.ifPresent(applicantPreTranslation::remove);
                    caseDataBuilder.preTranslationGaDocsApplicant(applicantPreTranslation);
                } else if (document.getValue().getDocumentType().equals(REQUEST_MORE_INFORMATION_RESPONDENT)) {
                    Optional<Element<CaseDocument>> preTranslationMoreInfoRespondent = preTranslationGaDocuments.stream()
                            .filter(item -> "Additional information".equals(item.getValue().getDocumentName())
                                    && DocUploadUtils.RESPONDENT_ONE.equals(item.getValue().getCreatedBy()))
                            .findFirst();
                    preTranslationMoreInfoRespondent.ifPresent(preTranslationGaDocuments::remove);
                    preTranslationMoreInfoRespondent.ifPresent(element ->
                            DocUploadUtils.addToAddl(caseDataBuilder.build(), caseDataBuilder, List.of(element), DocUploadUtils.RESPONDENT_ONE, false)
                    );
                    preTranslationMoreInfoRespondent.ifPresent(respondentPreTranslation::remove);
                    caseDataBuilder.preTranslationGaDocsRespondent(respondentPreTranslation);
                }
            });
        }
    }

    public CaseEvent getBusinessProcessEvent(CaseData caseData) {
        List<Element<TranslatedDocument>> translatedDocuments = caseData.getTranslatedDocumentsGA();

        if (Objects.nonNull(translatedDocuments)
                && translatedDocuments.get(0).getValue().getDocumentType().equals(APPLICATION_SUMMARY_DOCUMENT)
                && (Objects.nonNull(caseData.getGeneralAppPBADetails())
                && caseData.getGeneralAppPBADetails().getFee().getCode().equals("FREE"))) {
            return CaseEvent.UPLOAD_TRANSLATED_DOCUMENT_FOR_FREE_FEE_APPLICATION;
        } else if (Objects.nonNull(translatedDocuments)
                && translatedDocuments.get(0).getValue().getDocumentType().equals(APPLICATION_SUMMARY_DOCUMENT)) {
            return CaseEvent.UPLOAD_TRANSLATED_DOCUMENT_GA_SUMMARY_DOC;
        } else if (Objects.nonNull(translatedDocuments)
                && translatedDocuments.get(0).getValue().getDocumentType().equals(APPLICATION_SUMMARY_DOCUMENT_RESPONDED)) {
            return CaseEvent.UPLOAD_TRANSLATED_DOCUMENT_GA_SUMMARY_RESPONSE_DOC;
        } else if (Objects.nonNull(translatedDocuments)
                && translatedDocuments.get(0).getValue().getDocumentType().equals(GENERAL_ORDER)
                && caseData.getFinalOrderSelection() != null) {
            return CaseEvent.UPLOAD_TRANSLATED_DOCUMENT_FINAL_ORDER;
        } else if (Objects.nonNull(translatedDocuments)
                && (translatedDocuments.get(0).getValue().getDocumentType().equals(WRITTEN_REPRESENTATIONS_ORDER_SEQUENTIAL)
                || translatedDocuments.get(0).getValue().getDocumentType().equals(WRITTEN_REPRESENTATIONS_ORDER_CONCURRENT))
                || translatedDocuments.get(0).getValue().getDocumentType().equals(REQUEST_FOR_MORE_INFORMATION_ORDER)
                || translatedDocuments.get(0).getValue().getDocumentType().equals(JUDGES_DIRECTIONS_ORDER)
                || translatedDocuments.get(0).getValue().getDocumentType().equals(DISMISSAL_ORDER)
                || ((translatedDocuments.get(0).getValue().getDocumentType().equals(GENERAL_ORDER)
                || translatedDocuments.get(0).getValue().getDocumentType().equals(APPROVE_OR_EDIT_ORDER)) && Objects.isNull(caseData.getFinalOrderSelection())
                || translatedDocuments.get(0).getValue().getDocumentType().equals(HEARING_ORDER))) {
            return CaseEvent.UPLOAD_TRANSLATED_DOCUMENT_JUDGE_DECISION;
        } else if (Objects.nonNull(translatedDocuments)
                && (translatedDocuments.get(0).getValue().getDocumentType().equals(HEARING_NOTICE))) {
            return CaseEvent.UPLOAD_TRANSLATED_DOCUMENT_HEARING_SCHEDULED;
        } else if (Objects.nonNull(translatedDocuments)
                && (translatedDocuments.get(0).getValue().getDocumentType().equals(WRITTEN_REPRESENTATIONS_APPLICANT)
                || translatedDocuments.get(0).getValue().getDocumentType().equals(WRITTEN_REPRESENTATIONS_RESPONDENT))) {
            return CaseEvent.RESPOND_TO_JUDGE_WRITTEN_REPRESENTATION;
        } else if (Objects.nonNull(translatedDocuments)
                && (translatedDocuments.get(0).getValue().getDocumentType().equals(REQUEST_MORE_INFORMATION_APPLICANT)
                || translatedDocuments.get(0).getValue().getDocumentType().equals(REQUEST_MORE_INFORMATION_RESPONDENT))) {
            return CaseEvent.RESPOND_TO_JUDGE_ADDITIONAL_INFO;
        }
        return UPLOAD_TRANSLATED_DOCUMENT_GA_LIP;
    }

    public void sendUserUploadNotification(CaseData caseData, CaseData updatedCaseData, String authToken) {
        List<Element<TranslatedDocument>> translatedDocuments = caseData.getTranslatedDocumentsGA();
        if (gaForLipService.isGaForLip(caseData) && Objects.nonNull(translatedDocuments) && translatedDocuments.size() > 0) {
            TranslatedDocumentType translatedDocumentType = translatedDocuments.get(0).getValue().getDocumentType();
            String documentName = null;
            String role = null;
            switch (translatedDocumentType) {
                case WRITTEN_REPRESENTATIONS_APPLICANT -> {
                    documentName = "Written representation";
                    role = DocUploadUtils.APPLICANT;
                }
                case WRITTEN_REPRESENTATIONS_RESPONDENT -> {
                    documentName = "Written representation";
                    role = DocUploadUtils.RESPONDENT_ONE;
                }
                case REQUEST_MORE_INFORMATION_APPLICANT -> {
                    documentName = "Additional information";
                    role = DocUploadUtils.APPLICANT;
                }
                case REQUEST_MORE_INFORMATION_RESPONDENT -> {
                    documentName = "Additional information";
                    role = DocUploadUtils.RESPONDENT_ONE;
                }
                default -> {
                }
            }
            if (documentName != null && !DocUploadUtils.uploadedDocumentAwaitingTranslation(updatedCaseData, role, documentName)) {
                docUploadDashboardNotificationService.createDashboardNotification(caseData, role, authToken, false);
                if (translatedDocumentType == WRITTEN_REPRESENTATIONS_APPLICANT || translatedDocumentType == WRITTEN_REPRESENTATIONS_RESPONDENT) {
                    if (role.equals(DocUploadUtils.APPLICANT)) {
                        docUploadDashboardNotificationService.createResponseDashboardNotification(caseData, "RESPONDENT", authToken);
                    } else {
                        docUploadDashboardNotificationService.createResponseDashboardNotification(caseData, "APPLICANT", authToken);
                    }
                }
            }
        }
    }
}
