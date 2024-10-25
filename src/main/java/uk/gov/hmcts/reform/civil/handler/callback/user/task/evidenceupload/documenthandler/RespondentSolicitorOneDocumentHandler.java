package uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler;

import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadType;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documentbuilder.DocumentTypeBuilder;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.retriever.UploadDocumentRetriever;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceDocumentType;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;

public abstract class RespondentSolicitorOneDocumentHandler<T> extends LegalRepresentativeOneDocumentHandler<T> {

    public RespondentSolicitorOneDocumentHandler(DocumentCategory documentCategory, DocumentCategory legalRepresentativeTwoDocumentCategory,
                                                 EvidenceUploadType evidenceUploadType,
                                                 DocumentTypeBuilder<T> documentTypeBuilder,
                                                 UploadDocumentRetriever<T> uploadDocumentRetriever) {
        super(documentCategory, legalRepresentativeTwoDocumentCategory, evidenceUploadType, documentTypeBuilder, uploadDocumentRetriever);
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
