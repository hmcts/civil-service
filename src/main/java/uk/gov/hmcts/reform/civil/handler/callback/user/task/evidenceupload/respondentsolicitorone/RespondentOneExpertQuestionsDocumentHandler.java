package uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.respondentsolicitorone;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.LegalRepresentativeOneDocumentHandler;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documentbuilder.DocumentTypeBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceExpert;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class RespondentOneExpertQuestionsDocumentHandler extends
    LegalRepresentativeOneDocumentHandler<UploadEvidenceExpert> {

    protected static final String RESPONDENT_ONE_EXPERT_QUESTIONS_CATEGORY_ID = "RespondentOneExpertQuestions";
    protected static final String RESPONDENT_TWO_EXPERT_QUESTIONS_CATEGORY_ID = "RespondentTwoExpertQuestions";

    public RespondentOneExpertQuestionsDocumentHandler(DocumentTypeBuilder<UploadEvidenceExpert> documentTypeBuilder) {
        super(RESPONDENT_ONE_EXPERT_QUESTIONS_CATEGORY_ID, RESPONDENT_TWO_EXPERT_QUESTIONS_CATEGORY_ID,
            "%s - Questions for other party's expert or joint experts", documentTypeBuilder);
    }

    @Override
    protected List<Element<UploadEvidenceExpert>> getDocumentList(CaseData caseData) {
        return caseData.getDocumentQuestionsRes();
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
        renameUploadEvidenceExpert(documentUploads, true);
    }

    @Override
    protected void addDocumentsToCopyToCaseData(CaseData.CaseDataBuilder<?, ?> builder, List<Element<UploadEvidenceExpert>> evidenceDocsToAdd) {
        builder.documentQuestionsRes2(evidenceDocsToAdd);
    }

    @Override
    protected List<Element<UploadEvidenceExpert>> getCorrepsondingLegalRep2DocumentList(CaseData caseData) {
        return caseData.getDocumentQuestionsRes2();
    }
}
