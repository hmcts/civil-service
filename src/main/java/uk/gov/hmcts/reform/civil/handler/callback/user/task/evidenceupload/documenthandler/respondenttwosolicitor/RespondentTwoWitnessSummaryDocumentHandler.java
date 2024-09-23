package uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.respondenttwosolicitor;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceWitness;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.time.LocalDateTime;
import java.util.List;

import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.EvidenceUploadConstants.WITNESS_SUMMARY_OF;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.EvidenceUploadConstants.WITNESS_SUMMARY_TEXT;

@Component
public class RespondentTwoWitnessSummaryDocumentHandler extends
    DocumentHandler<UploadEvidenceWitness> {

    public RespondentTwoWitnessSummaryDocumentHandler() {
        super(DocumentCategory.RESPONDENT_TWO_WITNESS_SUMMARY, WITNESS_SUMMARY_TEXT);
    }

    @Override
    protected List<Element<UploadEvidenceWitness>> getDocumentList(CaseData caseData) {
        return caseData.getDocumentWitnessSummaryRes2();
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
}
