package uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.applicantonesolcitor;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadType;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documentbuilder.DocumentTypeBuilder;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.ApplicantOneSolicitorDocumentHandler;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.retriever.UploadEvidenceExpertRetriever;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceExpert;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;

import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_ONE_EXPERT_REPORT;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_TWO_EXPERT_REPORT;

@Component
@Order(4)
public class ApplicantOneExpertReportDocumentHandler extends
    ApplicantOneSolicitorDocumentHandler<UploadEvidenceExpert> {

    public ApplicantOneExpertReportDocumentHandler(DocumentTypeBuilder<UploadEvidenceExpert> documentTypeBuilder,
                                                   UploadEvidenceExpertRetriever uploadDocumentRetriever) {
        super(APPLICANT_ONE_EXPERT_REPORT, APPLICANT_TWO_EXPERT_REPORT, EvidenceUploadType.EXPERT_REPORT, documentTypeBuilder,
            uploadDocumentRetriever);
    }

    @Override
    protected List<Element<UploadEvidenceExpert>> getDocumentList(CaseData caseData) {
        return caseData.getDocumentExpertReport();
    }

    @Override
    protected void renameDocuments(List<Element<UploadEvidenceExpert>> documentUploads) {
        renameUploadReportExpert(documentUploads, evidenceUploadType.getDocumentTypeDisplayName(), true);
    }

    @Override
    protected void addDocumentsToCopyToCaseData(CaseData.CaseDataBuilder<?, ?> builder, List<Element<UploadEvidenceExpert>> evidenceDocsToAdd) {
        builder.documentExpertReportApp2(evidenceDocsToAdd);
    }

    @Override
    protected List<Element<UploadEvidenceExpert>> getCorrespondingLegalRep2DocumentList(CaseData caseData) {
        return caseData.getDocumentExpertReportApp2();
    }
}
