package uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler;

import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceDocumentType;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;

public abstract class RespondentSolicitorTwoDocumentHandler<T> extends DocumentHandler<T> {
    public RespondentSolicitorTwoDocumentHandler(DocumentCategory documentCategory, EvidenceUploadType evidenceUploadType) {
        super(documentCategory, evidenceUploadType);
    }

    @Override
    protected List<Element<UploadEvidenceDocumentType>> getDocsUploadedAfterBundle(CaseData caseData) {
        return caseData.getRespondentDocsUploadedAfterBundle();
    }

    @Override
    protected void applyDocumentUpdateToCollection(CaseData.CaseDataBuilder<?, ?> caseDetailsBuilder,
                                                   List<Element<UploadEvidenceDocumentType>> finalAdditionalBundleDoc) {
        caseDetailsBuilder.respondentDocsUploadedAfterBundle(finalAdditionalBundleDoc);

    }
}
