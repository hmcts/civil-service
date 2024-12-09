package uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler;

import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadType;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.retriever.UploadDocumentRetriever;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceDocumentType;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;

public abstract class RespondentTwoSolicitorDocumentHandler<T> extends DocumentHandler<T> {

    public RespondentTwoSolicitorDocumentHandler(DocumentCategory documentCategory, EvidenceUploadType evidenceUploadType,
                                                 UploadDocumentRetriever<T> documentRetriever) {
        super(documentCategory, evidenceUploadType, documentRetriever);
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
