package uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documentbuilder.DocumentTypeBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceDocumentType;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceExpert;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceWitness;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import static java.lang.String.format;

@Slf4j
public abstract class DocumentHandler<T> {

    private static final String SPACE = " ";
    private static final String END = ".";
    private static final String DATE_FORMAT = "dd-MM-yyyy";
    protected final String documentCategory;
    protected final String documentNotificationText;

    public DocumentHandler(String documentCategory, String documentNotificationText) {
        this.documentCategory = documentCategory;
        this.documentNotificationText = documentNotificationText;
    }

    public <T> void handleDocuments(CaseData caseData, String litigantType, StringBuilder notificationStringBuilder) {
        if (getDocumentList(caseData) == null || getDocumentList(caseData).isEmpty()) {
            return;
        }
        renameDocuments(getDocumentList(caseData));
        LocalDateTime halfFivePmYesterday = LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.of(17, 30));
        getDocumentList(caseData).forEach(document -> {
            setCategoryId(document);
            LocalDateTime dateTime = getDocumentDateTime(document);
            buildNotificationText(litigantType, notificationStringBuilder, dateTime, halfFivePmYesterday);
        });
    }

    private void buildNotificationText(String litigantType, StringBuilder notificationStringBuilder, LocalDateTime dateTime,
                                       LocalDateTime halfFivePmYesterday) {
        if (dateTime.isAfter(halfFivePmYesterday)) {
            String updateNotificationText = format(documentNotificationText, litigantType);
            if (!notificationStringBuilder.toString().contains(updateNotificationText)) {
                notificationStringBuilder.append("\n").append(updateNotificationText);
            }
        }
    }

    private void setCategoryId(Element<T> document) {
        Document documentToAddId = getDocument(document);
        documentToAddId.setCategoryID(documentCategory);
    }

    abstract protected List<Element<T>> getDocumentList(CaseData caseData);


    abstract protected Document getDocument(Element<T> element);

    abstract protected LocalDateTime getDocumentDateTime(Element<T> element);

    abstract protected void renameDocuments(List<Element<T>> documentUploads);


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

}
