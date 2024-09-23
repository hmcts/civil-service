package uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.applicanttwosolicitor;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documentbuilder.DocumentTypeBuilder;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.ApplicantSolicitorTwoDocumentHandler;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.LegalRepresentativeOneDocumentHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceWitness;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.time.LocalDateTime;
import java.util.List;

import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.EvidenceUploadConstants.HEARSAY_EVIDENCE;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.EvidenceUploadConstants.WITNESS_HEARSAY_TEXT;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_TWO_WITNESS_HEARSAY;

@Component
public class ApplicantTwoWitnessHearsayDocumentHandler extends
    ApplicantSolicitorTwoDocumentHandler<UploadEvidenceWitness> {

    public ApplicantTwoWitnessHearsayDocumentHandler(DocumentTypeBuilder<UploadEvidenceWitness> documentTypeBuilder) {
        super(APPLICANT_TWO_WITNESS_HEARSAY,
            WITNESS_HEARSAY_TEXT);
    }

    @Override
    protected List<Element<UploadEvidenceWitness>> getDocumentList(CaseData caseData) {
        return caseData.getDocumentHearsayNoticeApp2();
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
        renameUploadEvidenceWitness(documentUploads, HEARSAY_EVIDENCE, true);
    }

}
