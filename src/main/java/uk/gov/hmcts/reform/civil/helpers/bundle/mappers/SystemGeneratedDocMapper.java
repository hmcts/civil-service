package uk.gov.hmcts.reform.civil.helpers.bundle.mappers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.model.bundle.BundlingRequestDocument;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.civil.helpers.bundle.BundleUtils.buildBundlingRequestDoc;
import static uk.gov.hmcts.reform.civil.helpers.bundle.BundleUtils.generateDocName;

@Component
public class SystemGeneratedDocMapper {

    public List<BundlingRequestDocument> mapSystemGeneratedCaseDocument(
        List<Element<CaseDocument>> systemGeneratedCaseDocuments,
        String displayName
    ) {
        List<BundlingRequestDocument> bundlingSystemGeneratedCaseDocs = new ArrayList<>();
        if (systemGeneratedCaseDocuments != null) {
            systemGeneratedCaseDocuments.forEach(caseDocumentElement -> {
                String docName = generateDocName(displayName, null, null,
                    caseDocumentElement.getValue().getCreatedDatetime().toLocalDate());
                bundlingSystemGeneratedCaseDocs.add(buildBundlingRequestDoc(
                    docName,
                    caseDocumentElement.getValue().getDocumentLink(),
                    caseDocumentElement.getValue().getDocumentType().name()
                ));
            });
        }
        return bundlingSystemGeneratedCaseDocs;
    }
}
