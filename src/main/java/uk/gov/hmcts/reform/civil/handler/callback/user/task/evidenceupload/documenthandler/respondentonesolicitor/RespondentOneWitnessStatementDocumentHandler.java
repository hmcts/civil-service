package uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.respondentonesolicitor;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.RespondentSolicitorOneDocumentHandler;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documentbuilder.DocumentTypeBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceWitness;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.time.LocalDateTime;
import java.util.List;

import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.EvidenceUploadConstants.WITNESS_STATEMENT_OF;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.EvidenceUploadConstants.WITNESS_STATEMENT_TEXT;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_ONE_WITNESS_STATEMENT;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_TWO_WITNESS_STATEMENT;

@Component
public class RespondentOneWitnessStatementDocumentHandler extends
    RespondentSolicitorOneDocumentHandler<UploadEvidenceWitness> {

    public RespondentOneWitnessStatementDocumentHandler(DocumentTypeBuilder<UploadEvidenceWitness> documentTypeBuilder) {
        super(RESPONDENT_ONE_WITNESS_STATEMENT, RESPONDENT_TWO_WITNESS_STATEMENT, WITNESS_STATEMENT_TEXT,
            documentTypeBuilder);
    }

    @Override
    protected List<Element<UploadEvidenceWitness>> getDocumentList(CaseData caseData) {
        return caseData.getDocumentWitnessStatementRes();
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
        renameUploadEvidenceDocumentTypeWithName(documentUploads, WITNESS_STATEMENT_OF);

    }

    @Override
    protected void addDocumentsToCopyToCaseData(CaseData.CaseDataBuilder<?, ?> builder, List<Element<UploadEvidenceWitness>> evidenceDocsToAdd) {
        builder.documentWitnessStatementRes2(evidenceDocsToAdd);
    }

    @Override
    protected List<Element<UploadEvidenceWitness>> getCorrepsondingLegalRep2DocumentList(CaseData caseData) {
        return caseData.getDocumentWitnessStatementRes2();
    }
}
