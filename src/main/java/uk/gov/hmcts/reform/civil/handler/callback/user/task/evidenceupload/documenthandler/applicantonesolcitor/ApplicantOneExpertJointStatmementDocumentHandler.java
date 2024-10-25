package uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.applicantonesolcitor;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadType;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documentbuilder.DocumentTypeBuilder;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.ApplicantOneSolicitorDocumentHandler;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.retriever.UploadEvidenceExpertRetriever;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceExpert;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;

@Component
@Order(5)
public class ApplicantOneExpertJointStatmementDocumentHandler extends
    ApplicantOneSolicitorDocumentHandler<UploadEvidenceExpert> {

    public ApplicantOneExpertJointStatmementDocumentHandler(DocumentTypeBuilder<UploadEvidenceExpert> documentTypeBuilder,
                                                            UploadEvidenceExpertRetriever uploadEvidenceExpertRetriever) {
        super(DocumentCategory.APPLICANT_ONE_EXPERT_JOINT_STATEMENT, DocumentCategory.APPLICANT_TWO_EXPERT_JOINT_STATEMENT,
            EvidenceUploadType.JOINT_STATEMENT, documentTypeBuilder, uploadEvidenceExpertRetriever);
    }

    @Override
    protected List<Element<UploadEvidenceExpert>> getDocumentList(CaseData caseData) {
        return caseData.getDocumentJointStatement();
    }

    @Override
    protected void renameDocuments(List<Element<UploadEvidenceExpert>> documentUploads) {
        renameUploadReportExpert(documentUploads, evidenceUploadType.getDocumentTypeDisplayName(), false);
    }

    @Override
    protected void addDocumentsToCopyToCaseData(CaseData.CaseDataBuilder<?, ?> builder, List<Element<UploadEvidenceExpert>> evidenceDocsToAdd) {
        builder.documentJointStatementApp2(evidenceDocsToAdd);

    }

    @Override
    protected List<Element<UploadEvidenceExpert>> getCorrespondingLegalRep2DocumentList(CaseData caseData) {
        return caseData.getDocumentJointStatementApp2();
    }
}
