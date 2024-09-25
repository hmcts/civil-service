package uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.applicanttwosolicitor;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadType;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.ApplicantSolicitorTwoDocumentHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceDocumentType;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.time.LocalDateTime;
import java.util.List;

import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.EvidenceUploadConstants.REFERRED_TO_IN_THE_STATEMENT_OF;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_TWO_WITNESS_REFERRED;

@Component
public class ApplicantTwoWitnessReferredDocumentHandler extends
    ApplicantSolicitorTwoDocumentHandler<UploadEvidenceDocumentType> {

    public ApplicantTwoWitnessReferredDocumentHandler() {
        super(APPLICANT_TWO_WITNESS_REFERRED, EvidenceUploadType.WITNESS_REFERRED);
    }

    @Override
    protected List<Element<UploadEvidenceDocumentType>> getDocumentList(CaseData caseData) {
        return caseData.getDocumentReferredInStatementApp2();
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
        renameUploadEvidenceDocumentTypeWithName(documentUploads, REFERRED_TO_IN_THE_STATEMENT_OF);

    }

}
