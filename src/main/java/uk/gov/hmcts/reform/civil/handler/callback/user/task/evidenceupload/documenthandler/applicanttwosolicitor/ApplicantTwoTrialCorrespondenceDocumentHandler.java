package uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.applicanttwosolicitor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documentbuilder.DocumentTypeBuilder;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.ApplicantSolicitorTwoDocumentHandler;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.LegalRepresentativeOneDocumentHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceDocumentType;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.time.LocalDateTime;
import java.util.List;

import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.EvidenceUploadConstants.DOCUMENTARY_EVIDENCE;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.EvidenceUploadConstants.TRIAL_DOC_CORRESPONDENCE_TEXT;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_TWO_TRIAL_DOC_CORRESPONDENCE;

@Component
@Slf4j
public class ApplicantTwoTrialCorrespondenceDocumentHandler extends
    ApplicantSolicitorTwoDocumentHandler<UploadEvidenceDocumentType> {

    public ApplicantTwoTrialCorrespondenceDocumentHandler() {
        super(APPLICANT_TWO_TRIAL_DOC_CORRESPONDENCE,
            TRIAL_DOC_CORRESPONDENCE_TEXT);
    }

    @Override
    protected List<Element<UploadEvidenceDocumentType>> getDocumentList(CaseData caseData) {
        return caseData.getDocumentEvidenceForTrialApp2();
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
        renameUploadEvidenceDocumentType(documentUploads, DOCUMENTARY_EVIDENCE);
    }

}
