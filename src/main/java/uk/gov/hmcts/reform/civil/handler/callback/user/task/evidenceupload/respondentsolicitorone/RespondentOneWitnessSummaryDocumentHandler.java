package uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.respondentsolicitorone;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.LegalRepresentativeOneDocumentHandler;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documentbuilder.DocumentTypeBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceWitness;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class RespondentOneWitnessSummaryDocumentHandler extends
    LegalRepresentativeOneDocumentHandler<UploadEvidenceWitness> {

    protected static final String RESPONDENT_ONE_WITNESS_SUMMARY_CATEGORY_ID = "RespondentOneWitnessSummary";
    protected static final String RESPONDENT_TWO_WITNESS_SUMMARY_CATEGORY_ID = "RespondentTwoWitnessSummary";

    public RespondentOneWitnessSummaryDocumentHandler(DocumentTypeBuilder<UploadEvidenceWitness> documentTypeBuilder) {
        super(RESPONDENT_ONE_WITNESS_SUMMARY_CATEGORY_ID, RESPONDENT_TWO_WITNESS_SUMMARY_CATEGORY_ID, "%s - Witness summary", documentTypeBuilder);
    }

    @Override
    protected List<Element<UploadEvidenceWitness>> getDocumentList(CaseData caseData) {
        return caseData.getDocumentWitnessSummaryRes();
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
        renameUploadEvidenceWitness(documentUploads, "Witness Summary of", true);

    }

    @Override
    protected void addDocumentsToCopyToCaseData(CaseData.CaseDataBuilder<?, ?> builder, List<Element<UploadEvidenceWitness>> evidenceDocsToAdd) {
        builder.documentWitnessSummaryRes2(evidenceDocsToAdd);
    }

    @Override
    protected List<Element<UploadEvidenceWitness>> getCorrepsondingLegalRep2DocumentList(CaseData caseData) {
        return caseData.getDocumentWitnessSummaryRes2();
    }
}
