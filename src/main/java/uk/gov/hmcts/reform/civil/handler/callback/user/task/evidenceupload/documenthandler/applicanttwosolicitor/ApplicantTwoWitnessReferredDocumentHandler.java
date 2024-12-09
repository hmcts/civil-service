package uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.applicanttwosolicitor;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadType;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.ApplicantTwoSolicitorDocumentHandler;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.retriever.UploadEvidenceDocumentRetriever;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceDocumentType;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;

import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.EvidenceUploadConstants.REFERRED_TO_IN_THE_STATEMENT_OF;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_TWO_WITNESS_REFERRED;

@Component
@Order(3)
public class ApplicantTwoWitnessReferredDocumentHandler extends
    ApplicantTwoSolicitorDocumentHandler<UploadEvidenceDocumentType> {

    public ApplicantTwoWitnessReferredDocumentHandler(UploadEvidenceDocumentRetriever uploadDocumentRetriever) {
        super(APPLICANT_TWO_WITNESS_REFERRED, EvidenceUploadType.WITNESS_REFERRED, uploadDocumentRetriever);
    }

    @Override
    protected List<Element<UploadEvidenceDocumentType>> getDocumentList(CaseData caseData) {
        return caseData.getDocumentReferredInStatementApp2();
    }

    @Override
    protected void renameDocuments(List<Element<UploadEvidenceDocumentType>> documentUploads) {
        renameUploadEvidenceDocumentTypeWithName(documentUploads, REFERRED_TO_IN_THE_STATEMENT_OF);

    }

}
