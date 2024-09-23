package uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.applicantonesolcitor;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.ApplicantSolicitorOneDocumentHandler;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.LegalRepresentativeOneDocumentHandler;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documentbuilder.DocumentTypeBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceWitness;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.time.LocalDateTime;
import java.util.List;

import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.EvidenceUploadConstants.WITNESS_SUMMARY_OF;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.EvidenceUploadConstants.WITNESS_SUMMARY_TEXT;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_ONE_WITNESS_SUMMARY;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_TWO_WITNESS_SUMMARY;

@Component
public class ApplicantOneWitnessSummaryDocumentHandler extends
    ApplicantSolicitorOneDocumentHandler<UploadEvidenceWitness> {

    public ApplicantOneWitnessSummaryDocumentHandler(DocumentTypeBuilder<UploadEvidenceWitness> documentTypeBuilder) {
        super(APPLICANT_ONE_WITNESS_SUMMARY, APPLICANT_TWO_WITNESS_SUMMARY, WITNESS_SUMMARY_TEXT, documentTypeBuilder);
    }

    @Override
    protected List<Element<UploadEvidenceWitness>> getDocumentList(CaseData caseData) {
        return caseData.getDocumentWitnessSummary();
    }

    @Override
    protected Document getDocument(Element<UploadEvidenceWitness> element) {
        return element.getValue().getWitnessOptionDocument();
    }

    @Override
    protected LocalDateTime getDocumentDateTime(Element<UploadEvidenceWitness> element) {
        return element.getValue().getCreatedDatetime();
    }

    @Override
    protected void renameDocuments(List<Element<UploadEvidenceWitness>> documentUploads) {
        renameUploadEvidenceWitness(documentUploads, WITNESS_SUMMARY_OF, true);

    }

    @Override
    protected void addDocumentsToCopyToCaseData(CaseData.CaseDataBuilder<?, ?> builder, List<Element<UploadEvidenceWitness>> evidenceDocsToAdd) {
        builder.documentWitnessSummaryApp2(evidenceDocsToAdd);
    }

    @Override
    protected List<Element<UploadEvidenceWitness>> getCorrepsondingLegalRep2DocumentList(CaseData caseData) {
        return caseData.getDocumentWitnessSummaryApp2();
    }
}
