package uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.respondentonesolicitor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documentbuilder.DocumentTypeBuilder;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.RespondentSolicitorOneDocumentHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceDocumentType;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.time.LocalDateTime;
import java.util.List;

import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.EvidenceUploadConstants.DOCUMENTARY_EVIDENCE;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.EvidenceUploadConstants.TRIAL_DOC_CORRESPONDENCE_TEXT;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_ONE_TRIAL_DOC_CORRESPONDENCE;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_TWO_TRIAL_DOC_CORRESPONDENCE;

@Component
@Slf4j
public class RespondentOneTrialCorrespondenceDocumentHandler extends
    RespondentSolicitorOneDocumentHandler<UploadEvidenceDocumentType> {

    public RespondentOneTrialCorrespondenceDocumentHandler(DocumentTypeBuilder<UploadEvidenceDocumentType> documentTypeBuilder) {
        super(RESPONDENT_ONE_TRIAL_DOC_CORRESPONDENCE, RESPONDENT_TWO_TRIAL_DOC_CORRESPONDENCE,
            TRIAL_DOC_CORRESPONDENCE_TEXT, documentTypeBuilder);
    }

    @Override
    protected List<Element<UploadEvidenceDocumentType>> getDocumentList(CaseData caseData) {
        return caseData.getDocumentEvidenceForTrialRes();
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
        renameUploadEvidenceDocumentType(documentUploads, DOCUMENTARY_EVIDENCE);
    }

    @Override
    protected void addDocumentsToCopyToCaseData(CaseData.CaseDataBuilder<?, ?> builder, List<Element<UploadEvidenceDocumentType>> evidenceDocsToAdd) {
        builder.documentEvidenceForTrialRes2(evidenceDocsToAdd);

    }

    @Override
    protected List<Element<UploadEvidenceDocumentType>> getCorrepsondingLegalRep2DocumentList(CaseData caseData) {
        return caseData.getDocumentEvidenceForTrialRes2();
    }
}
