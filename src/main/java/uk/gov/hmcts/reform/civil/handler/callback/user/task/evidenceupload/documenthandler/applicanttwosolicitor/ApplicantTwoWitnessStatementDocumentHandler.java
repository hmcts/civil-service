package uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.applicanttwosolicitor;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadType;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.ApplicantSolicitorTwoDocumentHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceWitness;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.time.LocalDateTime;
import java.util.List;

import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_TWO_WITNESS_STATEMENT;

@Component
public class ApplicantTwoWitnessStatementDocumentHandler extends
    ApplicantSolicitorTwoDocumentHandler<UploadEvidenceWitness> {

    public ApplicantTwoWitnessStatementDocumentHandler() {
        super(APPLICANT_TWO_WITNESS_STATEMENT, EvidenceUploadType.WITNESS_STATEMENT);
    }

    @Override
    protected List<Element<UploadEvidenceWitness>> getDocumentList(CaseData caseData) {
        return caseData.getDocumentWitnessStatementApp2();
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
        renameUploadEvidenceDocumentTypeWithName(documentUploads, evidenceUploadType.getDocumentTypeDisplayName());

    }

}
