package uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.respondenttwosolicitor;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadType;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.RespondentTwoSolicitorDocumentHandler;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.retriever.UploadEvidenceDocumentRetriever;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceDocumentType;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;

import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.BUNDLE_EVIDENCE_UPLOAD;

@Component
public class RespondentTwoBundleDocumentHandler extends
    RespondentTwoSolicitorDocumentHandler<UploadEvidenceDocumentType> {

    public RespondentTwoBundleDocumentHandler(UploadEvidenceDocumentRetriever uploadDocumentRetriever) {
        super(BUNDLE_EVIDENCE_UPLOAD, EvidenceUploadType.BUNDLE_EVIDENCE, uploadDocumentRetriever);
    }

    @Override
    protected List<Element<UploadEvidenceDocumentType>> getDocumentList(CaseData caseData) {
        return caseData.getBundleEvidence();
    }

    @Override
    protected void renameDocuments(List<Element<UploadEvidenceDocumentType>> documentUploads) {
        renameUploadEvidenceBundleType(documentUploads);
    }

}

