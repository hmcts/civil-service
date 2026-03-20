package uk.gov.hmcts.reform.civil.ga.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocument;
import uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.ga.service.documentmanagement.DocumentUploadException;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;
import uk.gov.hmcts.reform.civil.ga.utils.DocUploadUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPLOAD_TRANSLATED_DOCUMENT_GA_LIP;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.APPLICATION_SUMMARY_DOCUMENT;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.APPLICATION_SUMMARY_DOCUMENT_RESPONDED;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.REQUEST_MORE_INFORMATION_APPLICANT;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.REQUEST_MORE_INFORMATION_RESPONDENT;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.WRITTEN_REPRESENTATIONS_APPLICANT;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.WRITTEN_REPRESENTATIONS_RESPONDENT;

@RequiredArgsConstructor
@Service
public class UploadTranslatedDocumentService {

    private static final String WRITTEN_REPRESENTATION = "Written representation";
    private static final String ADDITIONAL_INFORMATION = "Additional information";

    private final AssignCategoryId assignCategoryId;
    private final GaForLipService gaForLipService;
    private final DocUploadDashboardNotificationService docUploadDashboardNotificationService;
    private final DeadlinesCalculator deadlinesCalculator;

    public GeneralApplicationCaseData processTranslatedDocument(GeneralApplicationCaseData caseData, String translator) {
        List<Element<TranslatedDocument>> translatedDocuments = caseData.getTranslatedDocuments();
        GeneralApplicationCaseData caseDataBuilder = caseData.copy();

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
        caseDataBuilder.translatedDocumentsBulkPrint(caseData.getTranslatedDocuments());
        caseDataBuilder.translatedDocuments(null);
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
                .add(new Element<CaseDocument>().setValue(caseDocument));
        }

        return categorizedDocuments;
    }

    private List<Element<CaseDocument>> getExistingDocumentsByType(GeneralApplicationCaseData caseData, DocumentType documentType) {
        return switch (documentType) {
            case REQUEST_FOR_INFORMATION, SEND_APP_TO_OTHER_PARTY ->
                ofNullable(caseData.getRequestForInformationDocument()).orElse(new ArrayList<>());
            case DIRECTION_ORDER -> ofNullable(caseData.getDirectionOrderDocument()).orElse(new ArrayList<>());
            case GENERAL_ORDER -> ofNullable(caseData.getGeneralOrderDocument()).orElse(new ArrayList<>());
            case HEARING_ORDER -> ofNullable(caseData.getHearingOrderDocument()).orElse(new ArrayList<>());
            case HEARING_NOTICE -> ofNullable(caseData.getHearingNoticeDocument()).orElse(new ArrayList<>());
            case DISMISSAL_ORDER -> ofNullable(caseData.getDismissalOrderDocument()).orElse(new ArrayList<>());
            case WRITTEN_REPRESENTATION_CONCURRENT ->
                ofNullable(caseData.getWrittenRepConcurrentDocument()).orElse(new ArrayList<>());
            case WRITTEN_REPRESENTATION_SEQUENTIAL ->
                ofNullable(caseData.getWrittenRepSequentialDocument()).orElse(new ArrayList<>());
            case GENERAL_APPLICATION_DRAFT -> ofNullable(caseData.getGaDraftDocument()).orElse(new ArrayList<>());
            default -> new ArrayList<>();
        };
    }

    private void updateCaseDataBuilderByType(GeneralApplicationCaseData caseData, GeneralApplicationCaseData caseDataBuilder,
                                             DocumentType documentType,
                                             List<Element<CaseDocument>> documents) {
        switch (documentType) {
            case DIRECTION_ORDER:
                caseDataBuilder.directionOrderDocument(documents);
                break;
            case DISMISSAL_ORDER:
                caseDataBuilder.dismissalOrderDocument(documents);
                break;
            case REQUEST_FOR_INFORMATION:
            case SEND_APP_TO_OTHER_PARTY:
                caseDataBuilder.requestForInformationDocument(documents);
                break;
            case GENERAL_ORDER:
                caseDataBuilder.generalOrderDocument(documents);
                break;
            case HEARING_ORDER:
                caseDataBuilder.hearingOrderDocument(documents);
                break;
            case HEARING_NOTICE:
                caseDataBuilder.hearingNoticeDocument(documents);
                break;
            case WRITTEN_REPRESENTATION_CONCURRENT:
                caseDataBuilder.writtenRepConcurrentDocument(documents);
                break;
            case WRITTEN_REPRESENTATION_SEQUENTIAL:
                caseDataBuilder.writtenRepSequentialDocument(documents);
                break;
            case GENERAL_APPLICATION_DRAFT:
                caseDataBuilder.gaDraftDocument(documents);
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

    public void updateGADocumentsWithOriginalDocuments(GeneralApplicationCaseData caseDataBuilder) {
        List<Element<TranslatedDocument>> translatedDocuments = caseDataBuilder.build().getTranslatedDocuments();
        List<Element<CaseDocument>> preTranslationGaDocuments = caseDataBuilder.build().getPreTranslationGaDocuments();
        if (Objects.isNull(translatedDocuments)) {
            return;
        }

        List<Element<CaseDocument>> bulkPrintOriginalDocuments = newArrayList();
        List<Element<CaseDocument>> applicantPreTranslation = getCaseDocuments(caseDataBuilder.build().getPreTranslationGaDocsApplicant());
        List<Element<CaseDocument>> respondentPreTranslation = getCaseDocuments(caseDataBuilder.build().getPreTranslationGaDocsRespondent());
        OriginalDocumentUpdateContext context = new OriginalDocumentUpdateContext(
            caseDataBuilder,
            preTranslationGaDocuments,
            bulkPrintOriginalDocuments,
            applicantPreTranslation,
            respondentPreTranslation
        );

        translatedDocuments.forEach(document -> handleOriginalDocumentUpdate(context, document.getValue()));
    }

    private void handleOriginalDocumentUpdate(OriginalDocumentUpdateContext context, TranslatedDocument translatedDocument) {
        TranslatedDocumentType documentType = translatedDocument.getDocumentType();
        if (isApplicationSummaryDocument(documentType)) {
            handleApplicationSummaryDocument(context.caseDataBuilder(), context.preTranslationGaDocuments(), documentType);
            return;
        }
        if (isOrderDocument(documentType)) {
            moveOrderDocument(context, documentType);
            return;
        }
        if (isApplicantTranslatedDocument(documentType)) {
            moveApplicantTranslatedDocument(
                context.caseDataBuilder(),
                context.preTranslationGaDocuments(),
                context.applicantPreTranslation(),
                getOriginalDocumentName(documentType)
            );
            return;
        }
        if (isRespondentTranslatedDocument(documentType)) {
            moveRespondentTranslatedDocument(
                context.caseDataBuilder(),
                context.preTranslationGaDocuments(),
                context.respondentPreTranslation(),
                getOriginalDocumentName(documentType)
            );
        }
    }

    private boolean isApplicationSummaryDocument(TranslatedDocumentType documentType) {
        return documentType == APPLICATION_SUMMARY_DOCUMENT || documentType == APPLICATION_SUMMARY_DOCUMENT_RESPONDED;
    }

    private boolean isOrderDocument(TranslatedDocumentType documentType) {
        return switch (documentType) {
            case WRITTEN_REPRESENTATIONS_ORDER_SEQUENTIAL,
                WRITTEN_REPRESENTATIONS_ORDER_CONCURRENT,
                GENERAL_ORDER,
                APPROVE_OR_EDIT_ORDER,
                HEARING_NOTICE,
                REQUEST_FOR_MORE_INFORMATION_ORDER,
                HEARING_ORDER,
                JUDGES_DIRECTIONS_ORDER,
                DISMISSAL_ORDER -> true;
            default -> false;
        };
    }

    private boolean isApplicantTranslatedDocument(TranslatedDocumentType documentType) {
        return documentType == WRITTEN_REPRESENTATIONS_APPLICANT
            || documentType == REQUEST_MORE_INFORMATION_APPLICANT;
    }

    private boolean isRespondentTranslatedDocument(TranslatedDocumentType documentType) {
        return documentType == WRITTEN_REPRESENTATIONS_RESPONDENT
            || documentType == REQUEST_MORE_INFORMATION_RESPONDENT;
    }

    private String getOriginalDocumentName(TranslatedDocumentType documentType) {
        return switch (documentType) {
            case WRITTEN_REPRESENTATIONS_APPLICANT, WRITTEN_REPRESENTATIONS_RESPONDENT -> WRITTEN_REPRESENTATION;
            case REQUEST_MORE_INFORMATION_APPLICANT, REQUEST_MORE_INFORMATION_RESPONDENT -> ADDITIONAL_INFORMATION;
            default -> "";
        };
    }

    private void moveOrderDocument(OriginalDocumentUpdateContext context, TranslatedDocumentType documentType) {
        switch (documentType) {
            case WRITTEN_REPRESENTATIONS_ORDER_SEQUENTIAL:
                moveSequentialWrittenRepresentation(
                    context.caseDataBuilder(),
                    context.preTranslationGaDocuments(),
                    context.bulkPrintOriginalDocuments()
                );
                break;
            case WRITTEN_REPRESENTATIONS_ORDER_CONCURRENT:
                moveConcurrentWrittenRepresentation(
                    context.caseDataBuilder(),
                    context.preTranslationGaDocuments(),
                    context.bulkPrintOriginalDocuments()
                );
                break;
            case GENERAL_ORDER, APPROVE_OR_EDIT_ORDER:
                moveGeneralOrder(
                    context.caseDataBuilder(),
                    context.preTranslationGaDocuments(),
                    context.bulkPrintOriginalDocuments()
                );
                break;
            case HEARING_NOTICE:
                moveHearingNotice(
                    context.caseDataBuilder(),
                    context.preTranslationGaDocuments(),
                    context.bulkPrintOriginalDocuments()
                );
                break;
            case REQUEST_FOR_MORE_INFORMATION_ORDER:
                moveRequestForInformation(
                    context.caseDataBuilder(),
                    context.preTranslationGaDocuments(),
                    context.bulkPrintOriginalDocuments()
                );
                break;
            case HEARING_ORDER:
                moveHearingOrder(
                    context.caseDataBuilder(),
                    context.preTranslationGaDocuments(),
                    context.bulkPrintOriginalDocuments()
                );
                break;
            case JUDGES_DIRECTIONS_ORDER:
                moveDirectionOrder(
                    context.caseDataBuilder(),
                    context.preTranslationGaDocuments(),
                    context.bulkPrintOriginalDocuments()
                );
                break;
            case DISMISSAL_ORDER:
                moveDismissalOrder(
                    context.caseDataBuilder(),
                    context.preTranslationGaDocuments(),
                    context.bulkPrintOriginalDocuments()
                );
                break;
            default:
                break;
        }
    }

    private void handleApplicationSummaryDocument(GeneralApplicationCaseData caseDataBuilder,
                                                  List<Element<CaseDocument>> preTranslationGaDocuments,
                                                  TranslatedDocumentType translatedDocumentType) {
        if (Objects.isNull(preTranslationGaDocuments)) {
            return;
        }

        if (translatedDocumentType.equals(APPLICATION_SUMMARY_DOCUMENT)) {
            caseDataBuilder.generalAppNotificationDeadlineDate(
                deadlinesCalculator.calculateApplicantResponseDeadline(LocalDateTime.now(), 5)
            );
        }

        Optional<Element<CaseDocument>> preTranslationDraftDocument = findAndRemovePreTranslationDocument(
            preTranslationGaDocuments,
            item -> item.getDocumentType() == DocumentType.GENERAL_APPLICATION_DRAFT
        );
        appendTranslatedOrderDocument(
            caseDataBuilder,
            preTranslationDraftDocument,
            getCaseDocuments(caseDataBuilder.build().getGaDraftDocument()),
            caseDataBuilder::gaDraftDocument,
            null
        );
    }

    private void moveSequentialWrittenRepresentation(GeneralApplicationCaseData caseDataBuilder,
                                                     List<Element<CaseDocument>> preTranslationGaDocuments,
                                                     List<Element<CaseDocument>> bulkPrintOriginalDocuments) {
        appendTranslatedOrderDocument(
            caseDataBuilder,
            findAndRemovePreTranslationDocument(
                preTranslationGaDocuments,
                item -> item.getDocumentType() == DocumentType.WRITTEN_REPRESENTATION_SEQUENTIAL
            ),
            getCaseDocuments(caseDataBuilder.build().getWrittenRepSequentialDocument()),
            caseDataBuilder::writtenRepSequentialDocument,
            bulkPrintOriginalDocuments
        );
    }

    private void moveConcurrentWrittenRepresentation(GeneralApplicationCaseData caseDataBuilder,
                                                     List<Element<CaseDocument>> preTranslationGaDocuments,
                                                     List<Element<CaseDocument>> bulkPrintOriginalDocuments) {
        appendTranslatedOrderDocument(
            caseDataBuilder,
            findAndRemovePreTranslationDocument(
                preTranslationGaDocuments,
                item -> item.getDocumentType() == DocumentType.WRITTEN_REPRESENTATION_CONCURRENT
            ),
            getCaseDocuments(caseDataBuilder.build().getWrittenRepConcurrentDocument()),
            caseDataBuilder::writtenRepConcurrentDocument,
            bulkPrintOriginalDocuments
        );
    }

    private void moveGeneralOrder(GeneralApplicationCaseData caseDataBuilder,
                                  List<Element<CaseDocument>> preTranslationGaDocuments,
                                  List<Element<CaseDocument>> bulkPrintOriginalDocuments) {
        appendTranslatedOrderDocument(
            caseDataBuilder,
            findAndRemovePreTranslationDocument(
                preTranslationGaDocuments,
                item -> item.getDocumentType() == DocumentType.GENERAL_ORDER
            ),
            getCaseDocuments(caseDataBuilder.build().getGeneralOrderDocument()),
            caseDataBuilder::generalOrderDocument,
            bulkPrintOriginalDocuments
        );
    }

    private void moveHearingNotice(GeneralApplicationCaseData caseDataBuilder,
                                   List<Element<CaseDocument>> preTranslationGaDocuments,
                                   List<Element<CaseDocument>> bulkPrintOriginalDocuments) {
        appendTranslatedOrderDocument(
            caseDataBuilder,
            findAndRemovePreTranslationDocument(
                preTranslationGaDocuments,
                item -> item.getDocumentType() == DocumentType.HEARING_NOTICE
            ),
            getCaseDocuments(caseDataBuilder.build().getHearingNoticeDocument()),
            caseDataBuilder::hearingNoticeDocument,
            bulkPrintOriginalDocuments
        );
    }

    private void moveRequestForInformation(GeneralApplicationCaseData caseDataBuilder,
                                           List<Element<CaseDocument>> preTranslationGaDocuments,
                                           List<Element<CaseDocument>> bulkPrintOriginalDocuments) {
        appendTranslatedOrderDocument(
            caseDataBuilder,
            findAndRemovePreTranslationDocument(
                preTranslationGaDocuments,
                item -> item.getDocumentType() == DocumentType.REQUEST_FOR_INFORMATION
            ),
            getCaseDocuments(caseDataBuilder.build().getRequestForInformationDocument()),
            caseDataBuilder::requestForInformationDocument,
            bulkPrintOriginalDocuments
        );
    }

    private void moveHearingOrder(GeneralApplicationCaseData caseDataBuilder,
                                  List<Element<CaseDocument>> preTranslationGaDocuments,
                                  List<Element<CaseDocument>> bulkPrintOriginalDocuments) {
        appendTranslatedOrderDocument(
            caseDataBuilder,
            findAndRemovePreTranslationDocument(
                preTranslationGaDocuments,
                item -> item.getDocumentType() == DocumentType.HEARING_ORDER
            ),
            getCaseDocuments(caseDataBuilder.build().getHearingOrderDocument()),
            caseDataBuilder::hearingOrderDocument,
            bulkPrintOriginalDocuments
        );
    }

    private void moveDirectionOrder(GeneralApplicationCaseData caseDataBuilder,
                                    List<Element<CaseDocument>> preTranslationGaDocuments,
                                    List<Element<CaseDocument>> bulkPrintOriginalDocuments) {
        appendTranslatedOrderDocument(
            caseDataBuilder,
            findAndRemovePreTranslationDocument(
                preTranslationGaDocuments,
                item -> item.getDocumentType() == DocumentType.DIRECTION_ORDER
            ),
            getCaseDocuments(caseDataBuilder.build().getDirectionOrderDocument()),
            caseDataBuilder::directionOrderDocument,
            bulkPrintOriginalDocuments
        );
    }

    private void moveDismissalOrder(GeneralApplicationCaseData caseDataBuilder,
                                    List<Element<CaseDocument>> preTranslationGaDocuments,
                                    List<Element<CaseDocument>> bulkPrintOriginalDocuments) {
        appendTranslatedOrderDocument(
            caseDataBuilder,
            findAndRemovePreTranslationDocument(
                preTranslationGaDocuments,
                item -> item.getDocumentType() == DocumentType.DISMISSAL_ORDER
            ),
            getCaseDocuments(caseDataBuilder.build().getDismissalOrderDocument()),
            caseDataBuilder::dismissalOrderDocument,
            bulkPrintOriginalDocuments
        );
    }

    private void moveApplicantTranslatedDocument(GeneralApplicationCaseData caseDataBuilder,
                                                 List<Element<CaseDocument>> preTranslationGaDocuments,
                                                 List<Element<CaseDocument>> applicantPreTranslation,
                                                 String documentName) {
        Optional<Element<CaseDocument>> applicantDocument = findAndRemovePreTranslationDocument(
            preTranslationGaDocuments,
            item -> documentName.equals(item.getDocumentName()) && DocUploadUtils.APPLICANT.equals(item.getCreatedBy())
        );
        applicantDocument.ifPresent(element ->
            DocUploadUtils.addToAddl(caseDataBuilder.build(), caseDataBuilder, List.of(element), DocUploadUtils.APPLICANT, false)
        );
        applicantDocument.ifPresent(applicantPreTranslation::remove);
        caseDataBuilder.preTranslationGaDocsApplicant(applicantPreTranslation);
    }

    private void moveRespondentTranslatedDocument(GeneralApplicationCaseData caseDataBuilder,
                                                  List<Element<CaseDocument>> preTranslationGaDocuments,
                                                  List<Element<CaseDocument>> respondentPreTranslation,
                                                  String documentName) {
        Optional<Element<CaseDocument>> respondentDocument = findAndRemovePreTranslationDocument(
            preTranslationGaDocuments,
            item -> documentName.equals(item.getDocumentName()) && DocUploadUtils.RESPONDENT_ONE.equals(item.getCreatedBy())
        );
        respondentDocument.ifPresent(element ->
            DocUploadUtils.addToAddl(caseDataBuilder.build(), caseDataBuilder, List.of(element), DocUploadUtils.RESPONDENT_ONE, false)
        );
        respondentDocument.ifPresent(respondentPreTranslation::remove);
        caseDataBuilder.preTranslationGaDocsRespondent(respondentPreTranslation);
    }

    private Optional<Element<CaseDocument>> findAndRemovePreTranslationDocument(List<Element<CaseDocument>> preTranslationGaDocuments,
                                                                                Predicate<CaseDocument> predicate) {
        if (Objects.isNull(preTranslationGaDocuments)) {
            return Optional.empty();
        }

        Optional<Element<CaseDocument>> preTranslationDocument = preTranslationGaDocuments.stream()
            .filter(item -> predicate.test(item.getValue()))
            .findFirst();
        preTranslationDocument.ifPresent(preTranslationGaDocuments::remove);
        return preTranslationDocument;
    }

    private void appendTranslatedOrderDocument(GeneralApplicationCaseData caseDataBuilder,
                                               Optional<Element<CaseDocument>> originalDocument,
                                               List<Element<CaseDocument>> targetDocuments,
                                               Consumer<List<Element<CaseDocument>>> targetSetter,
                                               List<Element<CaseDocument>> bulkPrintOriginalDocuments) {
        originalDocument.ifPresent(document -> {
            targetDocuments.add(document);
            targetSetter.accept(targetDocuments);
            if (bulkPrintOriginalDocuments != null) {
                bulkPrintOriginalDocuments.add(document);
                caseDataBuilder.originalDocumentsBulkPrint(bulkPrintOriginalDocuments);
            }
        });
    }

    private List<Element<CaseDocument>> getCaseDocuments(List<Element<CaseDocument>> documents) {
        return Objects.isNull(documents) ? newArrayList() : documents;
    }

    public CaseEvent getBusinessProcessEvent(GeneralApplicationCaseData caseData) {
        TranslatedDocumentType translatedDocumentType = getFirstTranslatedDocumentType(caseData);
        if (translatedDocumentType == null) {
            return UPLOAD_TRANSLATED_DOCUMENT_GA_LIP;
        }
        return switch (translatedDocumentType) {
            case APPLICATION_SUMMARY_DOCUMENT -> isFreeFeeApplicationSummary(caseData, translatedDocumentType)
                ? CaseEvent.UPLOAD_TRANSLATED_DOCUMENT_FOR_FREE_FEE_APPLICATION
                : CaseEvent.UPLOAD_TRANSLATED_DOCUMENT_GA_SUMMARY_DOC;
            case APPLICATION_SUMMARY_DOCUMENT_RESPONDED -> CaseEvent.UPLOAD_TRANSLATED_DOCUMENT_GA_SUMMARY_RESPONSE_DOC;
            case HEARING_NOTICE -> CaseEvent.UPLOAD_TRANSLATED_DOCUMENT_HEARING_SCHEDULED;
            case WRITTEN_REPRESENTATIONS_APPLICANT, WRITTEN_REPRESENTATIONS_RESPONDENT ->
                CaseEvent.RESPOND_TO_JUDGE_WRITTEN_REPRESENTATION;
            case REQUEST_MORE_INFORMATION_APPLICANT, REQUEST_MORE_INFORMATION_RESPONDENT ->
                CaseEvent.RESPOND_TO_JUDGE_ADDITIONAL_INFO;
            case GENERAL_ORDER -> resolveGeneralOrderBusinessProcessEvent(caseData);
            case APPROVE_OR_EDIT_ORDER -> resolveApproveOrEditBusinessProcessEvent(caseData);
            case WRITTEN_REPRESENTATIONS_ORDER_SEQUENTIAL,
                WRITTEN_REPRESENTATIONS_ORDER_CONCURRENT,
                REQUEST_FOR_MORE_INFORMATION_ORDER,
                JUDGES_DIRECTIONS_ORDER,
                DISMISSAL_ORDER,
                HEARING_ORDER -> CaseEvent.UPLOAD_TRANSLATED_DOCUMENT_JUDGE_DECISION;
            default -> UPLOAD_TRANSLATED_DOCUMENT_GA_LIP;
        };
    }

    private CaseEvent resolveGeneralOrderBusinessProcessEvent(GeneralApplicationCaseData caseData) {
        return caseData.getFinalOrderSelection() != null
            ? CaseEvent.UPLOAD_TRANSLATED_DOCUMENT_FINAL_ORDER
            : CaseEvent.UPLOAD_TRANSLATED_DOCUMENT_JUDGE_DECISION;
    }

    private CaseEvent resolveApproveOrEditBusinessProcessEvent(GeneralApplicationCaseData caseData) {
        return caseData.getFinalOrderSelection() != null
            ? UPLOAD_TRANSLATED_DOCUMENT_GA_LIP
            : CaseEvent.UPLOAD_TRANSLATED_DOCUMENT_JUDGE_DECISION;
    }

    public void sendUserUploadNotification(GeneralApplicationCaseData caseData, GeneralApplicationCaseData updatedCaseData, String authToken) {
        UploadNotificationTarget target = resolveUploadNotificationTarget(getFirstTranslatedDocumentType(caseData));
        if (target == null || !shouldSendUserUploadNotification(caseData, updatedCaseData, target)) {
            return;
        }

        docUploadDashboardNotificationService.createDashboardNotification(caseData, target.role(), authToken, false);
        if (target.responseParty() != null) {
            docUploadDashboardNotificationService.createResponseDashboardNotification(caseData, target.responseParty(), authToken);
        }
    }

    private TranslatedDocumentType getFirstTranslatedDocumentType(GeneralApplicationCaseData caseData) {
        List<Element<TranslatedDocument>> translatedDocuments = caseData.getTranslatedDocuments();
        return Objects.nonNull(translatedDocuments) && !translatedDocuments.isEmpty()
            ? translatedDocuments.getFirst().getValue().getDocumentType() : null;
    }

    private boolean isFreeFeeApplicationSummary(GeneralApplicationCaseData caseData, TranslatedDocumentType translatedDocumentType) {
        return translatedDocumentType.equals(APPLICATION_SUMMARY_DOCUMENT)
            && Objects.nonNull(caseData.getGeneralAppPBADetails())
            && "FREE".equals(caseData.getGeneralAppPBADetails().getFee().getCode());
    }

    private UploadNotificationTarget resolveUploadNotificationTarget(TranslatedDocumentType translatedDocumentType) {
        if (translatedDocumentType == null) {
            return null;
        }

        return switch (translatedDocumentType) {
            case WRITTEN_REPRESENTATIONS_APPLICANT ->
                new UploadNotificationTarget(WRITTEN_REPRESENTATION, DocUploadUtils.APPLICANT, "RESPONDENT");
            case WRITTEN_REPRESENTATIONS_RESPONDENT ->
                new UploadNotificationTarget(WRITTEN_REPRESENTATION, DocUploadUtils.RESPONDENT_ONE, "APPLICANT");
            case REQUEST_MORE_INFORMATION_APPLICANT ->
                new UploadNotificationTarget(ADDITIONAL_INFORMATION, DocUploadUtils.APPLICANT, null);
            case REQUEST_MORE_INFORMATION_RESPONDENT ->
                new UploadNotificationTarget(ADDITIONAL_INFORMATION, DocUploadUtils.RESPONDENT_ONE, null);
            default -> null;
        };
    }

    private boolean shouldSendUserUploadNotification(GeneralApplicationCaseData caseData,
                                                     GeneralApplicationCaseData updatedCaseData,
                                                     UploadNotificationTarget target) {
        return gaForLipService.isGaForLip(caseData)
            && !DocUploadUtils.uploadedDocumentAwaitingTranslation(updatedCaseData, target.role(), target.documentName());
    }

    private record OriginalDocumentUpdateContext(
        GeneralApplicationCaseData caseDataBuilder,
        List<Element<CaseDocument>> preTranslationGaDocuments,
        List<Element<CaseDocument>> bulkPrintOriginalDocuments,
        List<Element<CaseDocument>> applicantPreTranslation,
        List<Element<CaseDocument>> respondentPreTranslation
    ) {
    }

    private record UploadNotificationTarget(String documentName, String role, String responseParty) {
    }
}
