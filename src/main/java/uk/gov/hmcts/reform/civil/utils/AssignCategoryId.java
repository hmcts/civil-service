package uk.gov.hmcts.reform.civil.utils;

import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.mediation.MediationDocumentsReferredInStatement;
import uk.gov.hmcts.reform.civil.model.mediation.MediationNonAttendanceStatement;

import java.util.List;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class AssignCategoryId {

    public static final String ORDER_DOCUMENTS = "ordersMadeOnApplications";
    public static final String APPLICATIONS = "applications";

    public <T> void assignCategoryIdToCollection(List<Element<T>> documentUpload, Function<Element<T>, Document> documentExtractor, String theID) {
        if (documentUpload == null) {
            return;
        }
        documentUpload.forEach(document -> documentExtractor.apply(document).setCategoryID(theID));
    }

    public void assignCategoryIdToCaseDocument(CaseDocument documentUpload, String theID) {
        if (documentUpload == null) {
            return;
        }
        documentUpload.getDocumentLink().setCategoryID(theID);
    }

    public CaseDocument copyCaseDocumentWithCategoryId(CaseDocument documentUpload, String theID) {
        if (documentUpload == null || documentUpload.getDocumentLink() == null) {
            return null;
        }
        Document document = new Document()
                .setCategoryID(theID)
                .setDocumentFileName(documentUpload.getDocumentLink().getDocumentFileName())
                .setDocumentBinaryUrl(documentUpload.getDocumentLink().getDocumentBinaryUrl())
                .setDocumentHash(documentUpload.getDocumentLink().getDocumentHash())
                .setDocumentUrl(documentUpload.getDocumentLink().getDocumentUrl());
        return new CaseDocument()
                .setDocumentName(documentUpload.getDocumentName())
                .setDocumentType(documentUpload.getDocumentType())
                .setDocumentLink(document)
                .setDocumentSize(documentUpload.getDocumentSize())
                .setCreatedBy(documentUpload.getCreatedBy())
                .setCreatedDatetime(documentUpload.getCreatedDatetime());
    }

    public List<Element<CaseDocument>> copyCaseDocumentListWithCategoryId(
            List<Element<CaseDocument>> source, String theID) {
        return source.stream().map(caseDocument -> {
            Document sourceDocument = caseDocument.getValue().getDocumentLink();
            Document document = new Document()
                    .setCategoryID(theID)
                    .setDocumentFileName(sourceDocument.getDocumentFileName())
                    .setDocumentBinaryUrl(sourceDocument.getDocumentBinaryUrl())
                    .setDocumentHash(sourceDocument.getDocumentHash())
                    .setDocumentUrl(sourceDocument.getDocumentUrl());
            CaseDocument clonedDocument = new CaseDocument()
                .setDocumentName(caseDocument.getValue().getDocumentName())
                .setDocumentType(caseDocument.getValue().getDocumentType())
                .setDocumentLink(document)
                .setDocumentSize(caseDocument.getValue().getDocumentSize())
                .setCreatedBy(caseDocument.getValue().getCreatedBy())
                .setCreatedDatetime(caseDocument.getValue().getCreatedDatetime());
            return element(clonedDocument);
        }).toList();
    }

    public List<Element<MediationNonAttendanceStatement>> copyCaseDocumentListWithCategoryIdMediationNonAtt(
        List<Element<MediationNonAttendanceStatement>> source, String theID) {
        return source.stream().map(documentElement -> {
            MediationNonAttendanceStatement statement = documentElement.getValue();
            Document value = statement.getDocument();
            Document updatedDocument = new Document()
                .setCategoryID(theID)
                .setDocumentFileName(value.getDocumentFileName())
                .setDocumentBinaryUrl(value.getDocumentBinaryUrl())
                .setDocumentHash(value.getDocumentHash())
                .setDocumentUrl(value.getDocumentUrl());
            MediationNonAttendanceStatement clone = new MediationNonAttendanceStatement();
            clone.setYourName(statement.getYourName());
            clone.setDocumentDate(statement.getDocumentDate());
            clone.setDocument(updatedDocument);
            clone.setDocumentUploadedDatetime(statement.getDocumentUploadedDatetime());
            return element(clone);
        }).toList();
    }

    public List<Element<MediationDocumentsReferredInStatement>> copyCaseDocumentListWithCategoryIdMediationDocRef(
        List<Element<MediationDocumentsReferredInStatement>> source, String theID) {
        return source.stream().map(documentElement -> {
            MediationDocumentsReferredInStatement statement = documentElement.getValue();
            Document value = statement.getDocument();
            Document updatedDocument = new Document()
                .setCategoryID(theID)
                .setDocumentFileName(value.getDocumentFileName())
                .setDocumentBinaryUrl(value.getDocumentBinaryUrl())
                .setDocumentHash(value.getDocumentHash())
                .setDocumentUrl(value.getDocumentUrl());
            MediationDocumentsReferredInStatement clone = new MediationDocumentsReferredInStatement();
            clone.setDocumentType(statement.getDocumentType());
            clone.setDocumentDate(statement.getDocumentDate());
            clone.setDocument(updatedDocument);
            clone.setDocumentUploadedDatetime(statement.getDocumentUploadedDatetime());
            return element(clone);
        }).toList();
    }

    public void assignCategoryIdToDocument(Document documentUpload, String theID) {
        if (documentUpload == null) {
            return;
        }
        documentUpload.setCategoryID(theID);
    }

}
