package uk.gov.hmcts.reform.civil.utils;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;
import java.util.function.Function;

@Component
public class AssignCategoryId {

    public <T> void setCategoryIdCollection(List<Element<T>> documentUpload, Function<Element<T>,
        Document> documentExtractor, String theID) {
        if (documentUpload == null) {
            return;
        }
        documentUpload.forEach(document -> {
            Document documentToAddId = documentExtractor.apply(document);
            documentToAddId.setCategoryID(theID);
        });
    }

    public <T> void setCategoryIdCaseDocument(CaseDocument documentUpload, String theID) {
        if (documentUpload == null) {
            return;
        }
        documentUpload.getDocumentLink().setCategoryID(theID);
    }

    public <T> void setCategoryIdDocument(Document documentUpload, String theID) {
        if (documentUpload == null) {
            return;
        }
        documentUpload.setCategoryID(theID);
    }

}
