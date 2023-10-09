package uk.gov.hmcts.reform.civil.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.List;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class AssignCategoryId {

    private final FeatureToggleService featureToggleService;

    public <T> void assignCategoryIdToCollection(List<Element<T>> documentUpload, Function<Element<T>, Document> documentExtractor, String theID) {
        if (!featureToggleService.isCaseFileViewEnabled()) {
            return;
        }
        if (documentUpload == null) {
            return;
        }
        documentUpload.forEach(document -> documentExtractor.apply(document).setCategoryID(theID));
    }

    public void assignCategoryIdToCaseDocument(CaseDocument documentUpload, String theID) {
        if (!featureToggleService.isCaseFileViewEnabled()) {
            return;
        }
        if (documentUpload == null) {
            return;
        }
        documentUpload.getDocumentLink().setCategoryID(theID);
    }

    public void assignCategoryIdToDocument(Document documentUpload, String theID) {
        if (!featureToggleService.isCaseFileViewEnabled()) {
            return;
        }
        if (documentUpload == null) {
            return;
        }
        documentUpload.setCategoryID(theID);
    }

}
