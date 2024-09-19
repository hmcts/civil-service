package uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.respondentsolicitorone;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.EvidenceUploadConstants;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.LegalRepresentativeOneDocumentHandler;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documentbuilder.DocumentTypeBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceDocumentType;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class RespondentOneDisclosureListDocumentHandler extends
    LegalRepresentativeOneDocumentHandler<UploadEvidenceDocumentType> {
    protected static final String RESPONDENT_ONE_DISCLOSURE_LIST_CATEGORY_ID = "RespondentOneDisclosureList";
    protected static final String RESPONDENT_TWO_DISCLOSURE_LIST_CATEGORY_ID = "RespondentTwoDisclosureList";

    public RespondentOneDisclosureListDocumentHandler(DocumentTypeBuilder<UploadEvidenceDocumentType> documentTypeBuilder) {
        super(RESPONDENT_ONE_DISCLOSURE_LIST_CATEGORY_ID, RESPONDENT_TWO_DISCLOSURE_LIST_CATEGORY_ID, EvidenceUploadConstants.DISCLOSURE_LIST_TEXT, documentTypeBuilder);
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
    protected LocalDateTime getDocumentDateTime(Element<UploadEvidenceDocumentType> element) {
        return element.getValue().getCreatedDatetime();
    }

    @Override
    protected void renameDocuments(List<Element<UploadEvidenceDocumentType>> documentUploads) {
        log.info("No rename required");
    }

    @Override
    protected void addDocumentsToCopyToCaseData(CaseData.CaseDataBuilder<?, ?> builder, List<Element<UploadEvidenceDocumentType>> evidenceDocsToAdd) {

    }

    @Override
    protected List<Element<UploadEvidenceDocumentType>> getCorrepsondingLegalRep2DocumentList(CaseData caseData) {
        return caseData.getDocumentDisclosureListRes2();
    }

}
