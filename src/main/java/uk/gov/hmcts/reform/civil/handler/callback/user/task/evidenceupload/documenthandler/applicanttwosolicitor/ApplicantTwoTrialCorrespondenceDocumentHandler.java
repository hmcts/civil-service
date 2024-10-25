package uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.applicanttwosolicitor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadType;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.ApplicantTwoSolicitorDocumentHandler;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.retriever.UploadEvidenceDocumentRetriever;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceDocumentType;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;

import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_TWO_TRIAL_DOC_CORRESPONDENCE;

@Component
@Slf4j
@Order(11)
public class ApplicantTwoTrialCorrespondenceDocumentHandler extends
    ApplicantTwoSolicitorDocumentHandler<UploadEvidenceDocumentType> {

    public ApplicantTwoTrialCorrespondenceDocumentHandler(UploadEvidenceDocumentRetriever uploadDocumentRetriever) {
        super(APPLICANT_TWO_TRIAL_DOC_CORRESPONDENCE,
            EvidenceUploadType.TRIAL_CORRESPONDENCE, uploadDocumentRetriever);
    }

    @Override
    protected List<Element<UploadEvidenceDocumentType>> getDocumentList(CaseData caseData) {
        return caseData.getDocumentEvidenceForTrialApp2();
    }

    @Override
    protected void renameDocuments(List<Element<UploadEvidenceDocumentType>> documentUploads) {
        renameUploadEvidenceDocumentType(documentUploads, evidenceUploadType.getDocumentTypeDisplayName());
    }

}
