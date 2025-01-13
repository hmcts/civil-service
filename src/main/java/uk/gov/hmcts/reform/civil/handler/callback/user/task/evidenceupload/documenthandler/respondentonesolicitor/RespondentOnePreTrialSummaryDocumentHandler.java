package uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.respondentonesolicitor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadType;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documentbuilder.DocumentTypeBuilder;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.RespondentSolicitorOneDocumentHandler;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.retriever.UploadEvidenceDocumentRetriever;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceDocumentType;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;

import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_ONE_PRE_TRIAL_SUMMARY;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_TWO_PRE_TRIAL_SUMMARY;

@Component
@Slf4j
@Order(8)
public class RespondentOnePreTrialSummaryDocumentHandler extends
    RespondentSolicitorOneDocumentHandler<UploadEvidenceDocumentType> {

    public RespondentOnePreTrialSummaryDocumentHandler(DocumentTypeBuilder<UploadEvidenceDocumentType> documentTypeBuilder,
                                                       UploadEvidenceDocumentRetriever uploadDocumentRetriever) {
        super(RESPONDENT_ONE_PRE_TRIAL_SUMMARY, RESPONDENT_TWO_PRE_TRIAL_SUMMARY, EvidenceUploadType.PRE_TRIAL_SUMMARY, documentTypeBuilder,
            uploadDocumentRetriever);
    }

    @Override
    protected List<Element<UploadEvidenceDocumentType>> getDocumentList(CaseData caseData) {
        return caseData.getDocumentCaseSummaryRes();
    }

    @Override
    protected void renameDocuments(List<Element<UploadEvidenceDocumentType>> documentUploads) {
        log.info("No rename required");
    }

    @Override
    protected void addDocumentsToCopyToCaseData(CaseData.CaseDataBuilder<?, ?> builder, List<Element<UploadEvidenceDocumentType>> evidenceDocsToAdd) {
        builder.documentCaseSummaryRes2(evidenceDocsToAdd);

    }

    @Override
    protected List<Element<UploadEvidenceDocumentType>> getCorrespondingLegalRep2DocumentList(CaseData caseData) {
        return caseData.getDocumentCaseSummaryRes2();
    }
}
