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

import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_TWO_PRE_TRIAL_SUMMARY;

@Component
@Slf4j
@Order(7)
public class ApplicantTwoPreTrialSummaryDocumentHandler extends
    ApplicantTwoSolicitorDocumentHandler<UploadEvidenceDocumentType> {

    public ApplicantTwoPreTrialSummaryDocumentHandler(UploadEvidenceDocumentRetriever uploadDocumentRetriever) {
        super(APPLICANT_TWO_PRE_TRIAL_SUMMARY, EvidenceUploadType.PRE_TRIAL_SUMMARY, uploadDocumentRetriever);
    }

    @Override
    protected List<Element<UploadEvidenceDocumentType>> getDocumentList(CaseData caseData) {
        return caseData.getDocumentCaseSummaryApp2();
    }

    @Override
    protected void renameDocuments(List<Element<UploadEvidenceDocumentType>> documentUploads) {
        log.info("No rename required");
    }

}
