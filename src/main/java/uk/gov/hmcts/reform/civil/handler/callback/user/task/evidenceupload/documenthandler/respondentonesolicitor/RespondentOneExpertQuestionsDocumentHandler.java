package uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.respondentonesolicitor;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadType;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documentbuilder.DocumentTypeBuilder;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.RespondentSolicitorOneDocumentHandler;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.retriever.UploadEvidenceExpertRetriever;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceExpert;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;

import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_ONE_EXPERT_QUESTIONS;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_TWO_EXPERT_QUESTIONS;

@Component
@Order(6)
public class RespondentOneExpertQuestionsDocumentHandler extends
    RespondentSolicitorOneDocumentHandler<UploadEvidenceExpert> {

    public RespondentOneExpertQuestionsDocumentHandler(DocumentTypeBuilder<UploadEvidenceExpert> documentTypeBuilder,
                                                       UploadEvidenceExpertRetriever documentTypeRetriever) {
        super(RESPONDENT_ONE_EXPERT_QUESTIONS, RESPONDENT_TWO_EXPERT_QUESTIONS,
            EvidenceUploadType.QUESTIONS_FOR_EXPERTS, documentTypeBuilder, documentTypeRetriever);
    }

    @Override
    protected List<Element<UploadEvidenceExpert>> getDocumentList(CaseData caseData) {
        return caseData.getDocumentQuestionsRes();
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
    protected List<Element<UploadEvidenceExpert>> getCorrespondingLegalRep2DocumentList(CaseData caseData) {
        return caseData.getDocumentQuestionsRes2();
    }
}
