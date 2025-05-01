package uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.respondenttwosolicitor;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadType;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.RespondentTwoSolicitorDocumentHandler;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.retriever.UploadEvidenceExpertRetriever;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceExpert;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;

import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_TWO_EXPERT_ANSWERS;

@Component
@Order(7)
public class RespondentTwoExpertAnswersDocumentHandler extends
    RespondentTwoSolicitorDocumentHandler<UploadEvidenceExpert> {

    public RespondentTwoExpertAnswersDocumentHandler(UploadEvidenceExpertRetriever uploadDocumentRetriever) {
        super(RESPONDENT_TWO_EXPERT_ANSWERS, EvidenceUploadType.ANSWERS_FOR_EXPERTS, uploadDocumentRetriever);
    }

    @Override
    protected List<Element<UploadEvidenceExpert>> getDocumentList(CaseData caseData) {
        return caseData.getDocumentAnswersRes2();
    }

    @Override
    protected void renameDocuments(List<Element<UploadEvidenceExpert>> documentUploads) {
        renameUploadEvidenceExpert(documentUploads, false);
    }
}
