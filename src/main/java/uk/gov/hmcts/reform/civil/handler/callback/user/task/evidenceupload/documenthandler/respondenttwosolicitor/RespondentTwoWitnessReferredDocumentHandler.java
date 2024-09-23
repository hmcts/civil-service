package uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.respondenttwosolicitor;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceDocumentType;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.time.LocalDateTime;
import java.util.List;

import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.EvidenceUploadConstants.REFERRED_TO_IN_THE_STATEMENT_OF;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.EvidenceUploadConstants.WITNESS_REFERRED_TEXT;

@Component
public class RespondentTwoWitnessReferredDocumentHandler extends
    DocumentHandler<UploadEvidenceDocumentType> {
    protected static final String RESPONDENT_TWO_WITNESS_REFERRED_CATEGORY_ID = "RespondentTwoWitnessReferred";

    public RespondentTwoWitnessReferredDocumentHandler() {
        super(RESPONDENT_TWO_WITNESS_REFERRED, WITNESS_REFERRED_TEXT);
    }

    @Override
    protected List<Element<UploadEvidenceDocumentType>> getDocumentList(CaseData caseData) {
        return caseData.getDocumentReferredInStatementRes2();
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
