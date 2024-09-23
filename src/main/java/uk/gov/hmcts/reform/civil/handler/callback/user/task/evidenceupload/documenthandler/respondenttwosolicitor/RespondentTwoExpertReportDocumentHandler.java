package uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.respondenttwosolicitor;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documentbuilder.DocumentTypeBuilder;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceExpert;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.time.LocalDateTime;
import java.util.List;

import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.EvidenceUploadConstants.EXPERTS_REPORT;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.EvidenceUploadConstants.EXPERT_REPORT_TEXT;

@Component
public class ApplicantOneExpertReportDocumentHandler extends
    DocumentHandler<UploadEvidenceExpert> {

    protected static final String RESPONDENT_TWO_EXPERT_REPORT_CATEGORY_ID = "ApplicantExpertReport";
    protected static final String RESPONDENT_TWO_EXPERT_REPORT_CATEGORY_ID = "RespondentTwoExpertReport";

    public ApplicantOneExpertReportDocumentHandler(DocumentTypeBuilder<UploadEvidenceExpert> documentTypeBuilder) {
        super(RESPONDENT_TWO_EXPERT_REPORT, RESPONDENT_TWO_EXPERT_REPORT, EXPERT_REPORT_TEXT, documentTypeBuilder);
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

    @Override
    protected void addDocumentsToCopyToCaseData(CaseData.CaseDataBuilder<?, ?> builder, List<Element<UploadEvidenceExpert>> evidenceDocsToAdd) {
        builder.documentExpertReportApp2(evidenceDocsToAdd);
    }

    @Override
    protected List<Element<UploadEvidenceExpert>> getCorrepsondingLegalRep2DocumentList(CaseData caseData) {
        return caseData.getDocumentExpertReportRes2();
    }
}
