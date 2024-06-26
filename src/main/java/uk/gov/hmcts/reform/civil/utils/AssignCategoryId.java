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
        Document document = Document.builder()
                .categoryID(theID)
                .documentFileName(documentUpload.getDocumentLink().getDocumentFileName())
                .documentBinaryUrl(documentUpload.getDocumentLink().getDocumentBinaryUrl())
                .documentHash(documentUpload.getDocumentLink().getDocumentHash())
                .documentUrl(documentUpload.getDocumentLink().getDocumentUrl())
                .build();
        return CaseDocument.builder()
                .documentName(documentUpload.getDocumentName())
                .documentType(documentUpload.getDocumentType())
                .documentLink(document)
                .documentSize(documentUpload.getDocumentSize())
                .createdBy(documentUpload.getCreatedBy())
                .createdDatetime(documentUpload.getCreatedDatetime())
                .build();
    }

    public List<Element<CaseDocument>> copyCaseDocumentListWithCategoryId(
            List<Element<CaseDocument>> source, String theID) {
        return source.stream().map(caseDocument -> {
            Document sourceDocument = caseDocument.getValue().getDocumentLink();
            Document document = Document.builder()
                    .categoryID(theID)
                    .documentFileName(sourceDocument.getDocumentFileName())
                    .documentBinaryUrl(sourceDocument.getDocumentBinaryUrl())
                    .documentHash(sourceDocument.getDocumentHash())
                    .documentUrl(sourceDocument.getDocumentUrl())
                    .build();
            return element(CaseDocument.builder()
                    .documentName(caseDocument.getValue().getDocumentName())
                    .documentType(caseDocument.getValue().getDocumentType())
                    .documentLink(document)
                    .documentSize(caseDocument.getValue().getDocumentSize())
                    .createdBy(caseDocument.getValue().getCreatedBy())
                    .createdDatetime(caseDocument.getValue().getCreatedDatetime())
                    .build());
        }).toList();
    }

    public List<Element<MediationNonAttendanceStatement>> copyCaseDocumentListWithCategoryIdMediationNonAtt(
        List<Element<MediationNonAttendanceStatement>> source, String theID) {
        return source.stream().map(documentElement -> {
            MediationNonAttendanceStatement statement = documentElement.getValue();
            Document value = statement.getDocument();
            Document updatedDocument = Document.builder()
                .categoryID(theID)
                .documentFileName(value.getDocumentFileName())
                .documentBinaryUrl(value.getDocumentBinaryUrl())
                .documentHash(value.getDocumentHash())
                .documentUrl(value.getDocumentUrl())
                .build();
            return element(MediationNonAttendanceStatement.builder()
                        .yourName(statement.getYourName())
                        .documentDate(statement.getDocumentDate())
                        .document(updatedDocument)
                        .documentUploadedDatetime(statement.getDocumentUploadedDatetime())
                        .build());
        }).toList();
    }

    public List<Element<MediationDocumentsReferredInStatement>> copyCaseDocumentListWithCategoryIdMediationDocRef(
        List<Element<MediationDocumentsReferredInStatement>> source, String theID) {
        return source.stream().map(documentElement -> {
            MediationDocumentsReferredInStatement statement = documentElement.getValue();
            Document value = statement.getDocument();
            Document updatedDocument = Document.builder()
                .categoryID(theID)
                .documentFileName(value.getDocumentFileName())
                .documentBinaryUrl(value.getDocumentBinaryUrl())
                .documentHash(value.getDocumentHash())
                .documentUrl(value.getDocumentUrl())
                .build();
            return element(MediationDocumentsReferredInStatement.builder()
                               .documentType(statement.getDocumentType())
                               .documentDate(statement.getDocumentDate())
                               .document(updatedDocument)
                               .documentUploadedDatetime(statement.getDocumentUploadedDatetime())
                               .build());
        }).toList();
    }

    public void assignCategoryIdToDocument(Document documentUpload, String theID) {
        if (documentUpload == null) {
            return;
        }
        documentUpload.setCategoryID(theID);
    }

}
