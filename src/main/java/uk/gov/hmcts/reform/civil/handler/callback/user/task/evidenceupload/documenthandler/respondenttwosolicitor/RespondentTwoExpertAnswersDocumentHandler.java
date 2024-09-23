package uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.respondenttwosolicitor;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.EvidenceUploadConstants;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceExpert;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.time.LocalDateTime;
import java.util.List;

import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_TWO_EXPERT_ANSWERS;

@Component
public class RespondentTwoExpertAnswersDocumentHandler extends
    DocumentHandler<UploadEvidenceExpert> {

    public RespondentTwoExpertAnswersDocumentHandler() {
        super(RESPONDENT_TWO_EXPERT_ANSWERS, EvidenceUploadConstants.EXPERT_ANSWERS_TEXT);
    }

    @Override
    protected List<Element<UploadEvidenceExpert>> getDocumentList(CaseData caseData) {
        return caseData.getDocumentAnswersRes2();
    }

    @Override
    protected Document getDocument(Element<UploadEvidenceExpert> element) {
        return element.getValue().getExpertDocument();
    }

    @Override
    protected LocalDateTime getDocumentDateTime(Element<UploadEvidenceExpert> element) {
        return element.getValue().getCreatedDatetime();
    }

    @Override
    protected void renameDocuments(List<Element<UploadEvidenceExpert>> documentUploads) {
        renameUploadEvidenceExpert(documentUploads, false);
    }


}
