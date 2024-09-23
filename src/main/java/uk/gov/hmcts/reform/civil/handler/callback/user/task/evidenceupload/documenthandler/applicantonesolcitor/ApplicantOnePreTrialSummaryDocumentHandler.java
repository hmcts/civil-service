package uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.applicantonesolcitor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.ApplicantSolicitorOneDocumentHandler;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.LegalRepresentativeOneDocumentHandler;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documentbuilder.DocumentTypeBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceDocumentType;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.time.LocalDateTime;
import java.util.List;

import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.EvidenceUploadConstants.PRE_TRIAL_SUMMARY_TEXT;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_ONE_PRE_TRIAL_SUMMARY;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_TWO_PRE_TRIAL_SUMMARY;

@Component
@Slf4j
public class ApplicantOnePreTrialSummaryDocumentHandler extends
    ApplicantSolicitorOneDocumentHandler<UploadEvidenceDocumentType> {

    public ApplicantOnePreTrialSummaryDocumentHandler(DocumentTypeBuilder<UploadEvidenceDocumentType> documentTypeBuilder) {
        super(APPLICANT_ONE_PRE_TRIAL_SUMMARY, APPLICANT_TWO_PRE_TRIAL_SUMMARY, PRE_TRIAL_SUMMARY_TEXT, documentTypeBuilder);
    }

    @Override
    protected List<Element<UploadEvidenceDocumentType>> getDocumentList(CaseData caseData) {
        return caseData.getDocumentCaseSummary();
    }

    @Override
    protected Document getDocument(Element<UploadEvidenceDocumentType> element) {
        return element.getValue().getDocumentUpload();
    }

    @Override
    protected LocalDateTime getDocumentDateTime(Element<UploadEvidenceDocumentType> element) {
        return element.getValue().getCreatedDatetime();
    }

    @Override
    protected void renameDocuments(List<Element<UploadEvidenceDocumentType>> documentUploads) {
        log.info("No rename required");
    }

    @Override
    protected void addDocumentsToCopyToCaseData(CaseData.CaseDataBuilder<?, ?> builder, List<Element<UploadEvidenceDocumentType>> evidenceDocsToAdd) {
        builder.documentCaseSummaryApp2(evidenceDocsToAdd);

    }

    @Override
    protected List<Element<UploadEvidenceDocumentType>> getCorrepsondingLegalRep2DocumentList(CaseData caseData) {
        return caseData.getDocumentCaseSummaryApp2();
    }
}
