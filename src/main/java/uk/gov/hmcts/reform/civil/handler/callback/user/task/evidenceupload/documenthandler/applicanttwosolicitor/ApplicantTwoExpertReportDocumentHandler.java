package uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.applicanttwosolicitor;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.ApplicantSolicitorTwoDocumentHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceExpert;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.time.LocalDateTime;
import java.util.List;

import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.EvidenceUploadConstants.EXPERTS_REPORT;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.EvidenceUploadConstants.EXPERT_REPORT_TEXT;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_TWO_EXPERT_REPORT;

@Component
public class ApplicantTwoExpertReportDocumentHandler extends
    ApplicantSolicitorTwoDocumentHandler<UploadEvidenceExpert> {

    public ApplicantTwoExpertReportDocumentHandler() {
        super(APPLICANT_TWO_EXPERT_REPORT, EXPERT_REPORT_TEXT);
    }

    @Override
    protected List<Element<UploadEvidenceExpert>> getDocumentList(CaseData caseData) {
        return caseData.getDocumentExpertReport();
    }

    @Override
    protected Document getDocument(Element<UploadEvidenceExpert> element) {
        return element.getValue().getExpertDocument();
    }

    @Override
    protected LocalDateTime getDocumentDateTime(Element<UploadEvidenceExpert> element) {
        return element.getValue().getCreatedDatetime();
    }

    @Override
    protected void renameDocuments(List<Element<UploadEvidenceExpert>> documentUploads) {
        renameUploadReportExpert(documentUploads, EXPERTS_REPORT, true);
    }

}
