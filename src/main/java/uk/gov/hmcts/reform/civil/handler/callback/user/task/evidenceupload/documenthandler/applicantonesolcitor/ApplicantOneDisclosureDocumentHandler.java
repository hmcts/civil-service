package uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.applicantonesolcitor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadType;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documentbuilder.DocumentTypeBuilder;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.ApplicantOneSolicitorDocumentHandler;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.retriever.UploadEvidenceDocumentRetriever;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceDocumentType;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;

import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_ONE_DISCLOSURE;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_TWO_DISCLOSURE;

@Component
@Slf4j
@Order(2)
public class ApplicantOneDisclosureDocumentHandler extends
    ApplicantOneSolicitorDocumentHandler<UploadEvidenceDocumentType> {

    public ApplicantOneDisclosureDocumentHandler(DocumentTypeBuilder<UploadEvidenceDocumentType> documentTypeBuilder,
                                                 UploadEvidenceDocumentRetriever uploadDocumentRetriever) {
        super(APPLICANT_ONE_DISCLOSURE, APPLICANT_TWO_DISCLOSURE, EvidenceUploadType.DOCUMENTS_FOR_DISCLOSURE, documentTypeBuilder,
            uploadDocumentRetriever);
    }

    @Override
    protected List<Element<UploadEvidenceDocumentType>> getDocumentList(CaseData caseData) {
        return caseData.getDocumentForDisclosure();
    }

    @Override
    protected void addDocumentsToCopyToCaseData(CaseData.CaseDataBuilder<?, ?> builder, List<Element<UploadEvidenceDocumentType>> evidenceDocsToAdd) {
        builder.documentForDisclosureApp2(evidenceDocsToAdd);
    }

    @Override
    protected List<Element<UploadEvidenceDocumentType>> getCorrespondingLegalRep2DocumentList(CaseData caseData) {
        return caseData.getDocumentForDisclosureApp2();
    }

}
