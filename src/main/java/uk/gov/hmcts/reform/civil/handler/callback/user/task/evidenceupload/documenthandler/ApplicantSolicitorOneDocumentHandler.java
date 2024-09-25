package uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler;

import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadType;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documentbuilder.DocumentTypeBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceDocumentType;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;

public abstract class ApplicantSolicitorOneDocumentHandler<T> extends LegalRepresentativeOneDocumentHandler<T> {
    public ApplicantSolicitorOneDocumentHandler(DocumentCategory documentCategory, DocumentCategory legalRepresentativeTwoDocumentCategory,
                                                EvidenceUploadType evidenceUploadType,
                                                DocumentTypeBuilder documentTypeBuilder) {
        super(documentCategory, legalRepresentativeTwoDocumentCategory, evidenceUploadType, documentTypeBuilder);
    }

    @Override
    protected List<Element<UploadEvidenceDocumentType>> getDocsUploadedAfterBundle(CaseData caseData) {
        return caseData.getApplicantDocsUploadedAfterBundle();
    }

    @Override
    protected void applyDocumentUpdateToCollection(CaseData.CaseDataBuilder<?, ?> caseDetailsBuilder,
                                                   List<Element<UploadEvidenceDocumentType>> finalAdditionalBundleDoc) {
        caseDetailsBuilder.applicantDocsUploadedAfterBundle(finalAdditionalBundleDoc);

    }

}
