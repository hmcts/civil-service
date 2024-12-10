package uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.applicantonesolcitor;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadType;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documentbuilder.DocumentTypeBuilder;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.ApplicantOneSolicitorDocumentHandler;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.retriever.UploadEvidenceWitnessRetriever;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceWitness;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;

import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_ONE_WITNESS_HEARSAY;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_TWO_WITNESS_HEARSAY;

@Component
public class ApplicantOneWitnessHearsayDocumentHandler extends
    ApplicantOneSolicitorDocumentHandler<UploadEvidenceWitness> {

    public ApplicantOneWitnessHearsayDocumentHandler(DocumentTypeBuilder<UploadEvidenceWitness> documentTypeBuilder,
                                                     UploadEvidenceWitnessRetriever uploadDocumentRetriever) {
        super(APPLICANT_ONE_WITNESS_HEARSAY, APPLICANT_TWO_WITNESS_HEARSAY,
            EvidenceUploadType.WITNESS_HEARSAY, documentTypeBuilder, uploadDocumentRetriever);
    }

    @Override
    protected List<Element<UploadEvidenceWitness>> getDocumentList(CaseData caseData) {
        return caseData.getDocumentHearsayNotice();
    }

    @Override
    protected void renameDocuments(List<Element<UploadEvidenceWitness>> documentUploads) {
        renameUploadEvidenceWitness(documentUploads, evidenceUploadType.getDocumentTypeDisplayName(), true);

    }

    @Override
    protected void addDocumentsToCopyToCaseData(CaseData.CaseDataBuilder<?, ?> builder, List<Element<UploadEvidenceWitness>> evidenceDocsToAdd) {
        builder.documentHearsayNoticeApp2(evidenceDocsToAdd);
    }

    @Override
    protected List<Element<UploadEvidenceWitness>> getCorrespondingLegalRep2DocumentList(CaseData caseData) {
        return caseData.getDocumentHearsayNoticeApp2();
    }
}
