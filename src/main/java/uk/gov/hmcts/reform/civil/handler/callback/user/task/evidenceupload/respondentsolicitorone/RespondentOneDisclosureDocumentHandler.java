package uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.respondentsolicitorone;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.LegalRepresentativeOneDocumentHandler;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documentbuilder.DocumentTypeBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceDocumentType;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class RespondentOneDisclosureDocumentHandler extends
    LegalRepresentativeOneDocumentHandler<UploadEvidenceDocumentType> {

    protected static final String RESPONDENT_ONE_DISCLOSURE_CATEGORY_ID = "RespondentOneDisclosure";
    protected static final String RESPONDENT_TWO_DISCLOSURE_CATEGORY_ID = "RespondentTwoDisclosure";

    public RespondentOneDisclosureDocumentHandler(DocumentTypeBuilder<UploadEvidenceDocumentType> documentTypeBuilder) {
        super(RESPONDENT_ONE_DISCLOSURE_CATEGORY_ID, RESPONDENT_TWO_DISCLOSURE_CATEGORY_ID, "%s - Disclosure list", documentTypeBuilder);
    }

    @Override
    protected List<Element<UploadEvidenceDocumentType>> getDocumentList(CaseData caseData) {
        return caseData.getDocumentDisclosureListRes();
    }


    @Override
    protected Document getDocument(Element<UploadEvidenceDocumentType> element) {
        return element.getValue().getDocumentUpload();
    }


    @Override
    protected void addDocumentsToCopyToCaseData(CaseData.CaseDataBuilder<?, ?> builder, List<Element<UploadEvidenceDocumentType>> evidenceDocsToAdd) {
        builder.documentDisclosureListRes2(evidenceDocsToAdd);

    }

    @Override
    protected List<Element<UploadEvidenceDocumentType>> getCorrepsondingLegalRep2DocumentList(CaseData caseData) {
        return caseData.getDocumentDisclosureListRes2();
    }

    @Override
    protected LocalDateTime getDocumentDateTime(Element<UploadEvidenceDocumentType> element) {
        return element.getValue().getCreatedDatetime();
    }

    @Override
    protected void renameDocuments(List<Element<UploadEvidenceDocumentType>> documentUploads) {
        renameUploadEvidenceDocumentType(documentUploads, "Document for disclosure");

    }

}
