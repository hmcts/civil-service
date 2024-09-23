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

import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.EvidenceUploadConstants.HEARSAY_EVIDENCE;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.EvidenceUploadConstants.WITNESS_HEARSAY_TEXT;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_ONE_WITNESS_HEARSAY;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_TWO_WITNESS_HEARSAY;

@Component
public class RespondentOneWitnessHearsayDocumentHandler extends
    RespondentSolicitorOneDocumentHandler<UploadEvidenceWitness> {

    public RespondentOneWitnessHearsayDocumentHandler(DocumentTypeBuilder<UploadEvidenceWitness> documentTypeBuilder) {
        super(RESPONDENT_ONE_WITNESS_HEARSAY, RESPONDENT_TWO_WITNESS_HEARSAY,
            WITNESS_HEARSAY_TEXT, documentTypeBuilder);
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
        renameUploadEvidenceWitness(documentUploads, HEARSAY_EVIDENCE, true);

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
