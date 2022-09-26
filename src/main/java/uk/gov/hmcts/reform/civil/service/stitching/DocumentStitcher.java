package uk.gov.hmcts.reform.civil.service.stitching;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;
import uk.gov.hmcts.reform.civil.model.documents.DocumentMetaData;

import java.util.List;

public interface DocumentStitcher {

    CaseDocument bundle(
        List<DocumentMetaData> documents,
        String authorisation,
        String bundleTitle,
        String bundleFilename,
        CaseData caseData

    );
}
