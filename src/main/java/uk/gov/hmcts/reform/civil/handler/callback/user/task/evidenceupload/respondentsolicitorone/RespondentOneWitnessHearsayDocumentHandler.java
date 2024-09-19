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
public class RespondentOneWitnessHearsayDocumentHandler extends
    LegalRepresentativeOneDocumentHandler<UploadEvidenceWitness> {

    protected static final String RESPONDENT_ONE_WITNESS_HEARSAY_CATEGORY_ID = "RespondentOneWitnessHearsay";
    protected static final String RESPONDENT_TWO_WITNESS_HEARSAY_CATEGORY_ID = "RespondentTwoWitnessHearsay";

    public RespondentOneWitnessHearsayDocumentHandler(DocumentTypeBuilder<UploadEvidenceWitness> documentTypeBuilder) {
        super(RESPONDENT_ONE_WITNESS_HEARSAY_CATEGORY_ID, RESPONDENT_TWO_WITNESS_HEARSAY_CATEGORY_ID,
            "%s - Notice of the intention to rely on hearsay evidence", documentTypeBuilder);
    }

    @Override
    protected List<Element<UploadEvidenceWitness>> getDocumentList(CaseData caseData) {
        return caseData.getDocumentHearsayNoticeRes();
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
        renameUploadEvidenceWitness(documentUploads, "Hearsay evidence", true);

    }

    @Override
    protected void addDocumentsToCopyToCaseData(CaseData.CaseDataBuilder<?, ?> builder, List<Element<UploadEvidenceWitness>> evidenceDocsToAdd) {
        builder.documentHearsayNoticeRes2(evidenceDocsToAdd);
    }

    @Override
    protected List<Element<UploadEvidenceWitness>> getCorrepsondingLegalRep2DocumentList(CaseData caseData) {
        return caseData.getDocumentHearsayNoticeRes2();
    }
}
