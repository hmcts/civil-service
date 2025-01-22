package uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadType;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.retriever.UploadDocumentRetriever;
import uk.gov.hmcts.reform.civil.model.Bundle;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdValue;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceDocumentType;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceExpert;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceWitness;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@Slf4j
public abstract class DocumentHandler<T> {

    protected static final String SPACE = " ";
    private static final String HYPHEN = "-";
    protected static final String END = ".";
    protected static final String DATE_FORMAT = "dd-MM-yyyy";
    protected final DocumentCategory documentCategory;
    protected final EvidenceUploadType evidenceUploadType;
    private final UploadDocumentRetriever<T> uploadDocumentRetriever;

    public DocumentHandler(DocumentCategory documentCategory, EvidenceUploadType evidenceUploadType,
                           UploadDocumentRetriever<T> uploadDocumentRetriever) {
        this.documentCategory = documentCategory;
        this.evidenceUploadType = evidenceUploadType;
        this.uploadDocumentRetriever = uploadDocumentRetriever;
    }

    public <T> void handleDocuments(CaseData caseData, String litigantType, StringBuilder notificationStringBuilder) {
        if (getDocumentList(caseData) == null || getDocumentList(caseData).isEmpty()) {
            return;
        }
        renameDocuments(getDocumentList(caseData));
        LocalDateTime halfFivePmYesterday = LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.of(17, 30));
        getDocumentList(caseData).forEach(document -> {
            setCategoryId(document);
            LocalDateTime dateTime = uploadDocumentRetriever.getDocumentDateTime(document);
            buildNotificationText(litigantType, notificationStringBuilder, dateTime, halfFivePmYesterday);
        });
    }

    private void buildNotificationText(String litigantType, StringBuilder notificationStringBuilder, LocalDateTime dateTime,
                                       LocalDateTime halfFivePmYesterday) {
        if (dateTime.isAfter(halfFivePmYesterday)) {
            String updateNotificationText = format(evidenceUploadType.getNotifictationTextRegEx(), litigantType);
            if (!notificationStringBuilder.toString().contains(updateNotificationText)) {
                notificationStringBuilder.append("\n").append(updateNotificationText);
            }
        }
    }

    private void setCategoryId(Element<T> document) {
        Document documentToAddId = uploadDocumentRetriever.getDocument(document);
        documentToAddId.setCategoryID(documentCategory.getCategoryId());
    }

    protected <T> void renameUploadEvidenceBundleType(final List<Element<T>> documentUpload) {
        documentUpload.forEach(x -> {
            UploadEvidenceDocumentType type = (UploadEvidenceDocumentType) x.getValue();
            String ext = FilenameUtils.getExtension(type.getDocumentUpload().getDocumentFileName());
            String newName = type.getDocumentIssuedDate().format(DateTimeFormatter.ofPattern(DATE_FORMAT, Locale.UK))
                + HYPHEN
                + type.getBundleName()
                + END + ext;
            type.getDocumentUpload().setDocumentFileName(newName);
        });
    }

    protected <T> void renameUploadEvidenceDocumentType(final List<Element<T>> documentUpload, String prefix) {
        documentUpload.forEach(x -> {
            UploadEvidenceDocumentType type = (UploadEvidenceDocumentType) x.getValue();
            String ext = FilenameUtils.getExtension(type.getDocumentUpload().getDocumentFileName());
            String newName = prefix
                + SPACE
                + type.getTypeOfDocument()
                + SPACE
                + type.getDocumentIssuedDate()
                .format(DateTimeFormatter.ofPattern(DATE_FORMAT, Locale.UK))
                + END + ext;
            type.getDocumentUpload().setDocumentFileName(newName);
        });
    }

    protected <T> void renameUploadEvidenceExpert(final List<Element<T>> documentUpload, boolean question) {
        documentUpload.forEach(x -> {
            UploadEvidenceExpert type = (UploadEvidenceExpert) x.getValue();
            String ext = FilenameUtils.getExtension(type.getExpertDocument().getDocumentFileName());
            String newName = type.getExpertOptionName()
                + SPACE
                + type.getExpertOptionOtherParty()
                + SPACE
                + (question ? type.getExpertDocumentQuestion() : type.getExpertDocumentAnswer())
                + END + ext;
            type.getExpertDocument().setDocumentFileName(newName);
        });
    }

    protected <T> void renameUploadReportExpert(final List<Element<T>> documentUpload,
                                                String prefix, boolean single) {
        documentUpload.forEach(x -> {
            UploadEvidenceExpert type = (UploadEvidenceExpert) x.getValue();
            String ext = FilenameUtils.getExtension(type.getExpertDocument().getDocumentFileName());
            String newName = prefix
                + SPACE
                + type.getExpertOptionName()
                + SPACE
                + (single ? type.getExpertOptionExpertise() : type.getExpertOptionExpertises())
                + SPACE
                + type.getExpertOptionUploadDate()
                .format(DateTimeFormatter.ofPattern(DATE_FORMAT, Locale.UK))
                + END + ext;
            type.getExpertDocument().setDocumentFileName(newName);
        });
    }

    protected <T> void renameUploadEvidenceWitness(final List<Element<T>> documentUpload,
                                                   String prefix, boolean date) {
        documentUpload.forEach(x -> {
            UploadEvidenceWitness type = (UploadEvidenceWitness) x.getValue();
            String ext = FilenameUtils.getExtension(type.getWitnessOptionDocument().getDocumentFileName());
            String newName = prefix
                + SPACE
                + type.getWitnessOptionName()
                + (date ? SPACE + type.getWitnessOptionUploadDate()
                .format(DateTimeFormatter.ofPattern(DATE_FORMAT, Locale.UK)) : "")
                + END + ext;
            type.getWitnessOptionDocument().setDocumentFileName(newName);
        });
    }

    protected <T> void renameUploadEvidenceDocumentTypeWithName(final List<Element<T>> documentUpload, String body) {
        documentUpload.forEach(x -> {
            UploadEvidenceDocumentType type = (UploadEvidenceDocumentType) x.getValue();
            String ext = FilenameUtils.getExtension(type.getDocumentUpload().getDocumentFileName());
            String newName = type.getTypeOfDocument()
                + body
                + type.getWitnessOptionName()
                + SPACE
                + type.getDocumentIssuedDate()
                .format(DateTimeFormatter.ofPattern(DATE_FORMAT, Locale.UK))
                + END + ext;
            type.getDocumentUpload().setDocumentFileName(newName);
        });
    }

    protected void renameDocuments(List<Element<T>> documentUploads) {
        renameUploadEvidenceDocumentType(documentUploads, evidenceUploadType.getDocumentTypeDisplayName());
    }

    public void addUploadDocList(CaseData.CaseDataBuilder caseDataBuilder, CaseData caseData) {

        if (getDocumentList(caseData) == null || getDocumentList(caseData).isEmpty()) {
            return;
        }
        Optional<Bundle> bundleDetails = caseData.getCaseBundles().stream().map(IdValue::getValue)
            .max(Comparator.comparing(bundle -> bundle.getCreatedOn().orElse(null)));
        LocalDateTime trialBundleDate = null;
        if (bundleDetails.isPresent()) {
            Optional<LocalDateTime> createdOn = bundleDetails.get().getCreatedOn();
            if (createdOn.isPresent()) {
                trialBundleDate = createdOn.get();
            }
        }
        populateBundleCollection(
            caseData,
            caseDataBuilder,
            trialBundleDate
        );
    }

    private <T> void populateBundleCollection(CaseData caseData, CaseData.CaseDataBuilder<?, ?> caseDataBuilder,
                                              LocalDateTime trialBundleDate) {
        //List<Element<UploadEvidenceDocumentType>> additionalBundleDocs = null;
        // If either claimant or respondent additional bundle doc collection exists, we add to that
        List<Element<UploadEvidenceDocumentType>> additionalBundleDocs = getDocsUploadedAfterBundle(caseData);
        List<Element<UploadEvidenceDocumentType>> finalAdditionalBundleDocs = additionalBundleDocs;
        getDocumentList(caseData).forEach(uploadEvidenceDocumentType -> {
            Document documentToAdd = uploadDocumentRetriever.getDocument(uploadEvidenceDocumentType);
            LocalDateTime documentCreatedDateTime = uploadDocumentRetriever.getDocumentDateTime(uploadEvidenceDocumentType);
            // If document was uploaded after the trial bundle was created, it is added to additional bundle documents
            // via applicant or respondent collections
            if (documentCreatedDateTime != null
                && documentCreatedDateTime.isAfter(trialBundleDate)
            ) {
                // If a document already exists in the collection, it cannot be re-added.
                boolean containsValue = finalAdditionalBundleDocs.stream()
                    .map(Element::getValue)
                    .map(upload -> upload != null ? upload.getDocumentUpload() : null)
                    .filter(Objects::nonNull)
                    .map(Document::getDocumentUrl)
                    .anyMatch(docUrl -> docUrl.equals(documentToAdd.getDocumentUrl()));
                // When a bundle is created, applicantDocsUploadedAfterBundle and respondentDocsUploadedAfterBundle
                // are assigned as empty lists, in actuality they contain a single element (default builder) we remove
                // this as it is not required.
                finalAdditionalBundleDocs.removeIf(element -> {
                    UploadEvidenceDocumentType upload = element.getValue();
                    return upload == null || upload.getDocumentUpload() == null
                        || upload.getDocumentUpload().getDocumentUrl() == null;
                });
                if (!containsValue) {
                    var newDocument = UploadEvidenceDocumentType.builder()
                        .typeOfDocument(evidenceUploadType.getDocumentTypeDisplayName())
                        .createdDatetime(documentCreatedDateTime)
                        .documentUpload(documentToAdd)
                        .build();
                    finalAdditionalBundleDocs.add(element(newDocument));
                    applyDocumentUpdateToCollection(caseDataBuilder, finalAdditionalBundleDocs);
                }
            }
        });
    }

    protected abstract List<Element<T>> getDocumentList(CaseData caseData);

    protected abstract List<Element<UploadEvidenceDocumentType>> getDocsUploadedAfterBundle(CaseData caseData);

    protected abstract void applyDocumentUpdateToCollection(CaseData.CaseDataBuilder<?, ?> caseDetailsBuilder,
                                                            List<Element<UploadEvidenceDocumentType>> finalAdditionalBundleDoc);
}
