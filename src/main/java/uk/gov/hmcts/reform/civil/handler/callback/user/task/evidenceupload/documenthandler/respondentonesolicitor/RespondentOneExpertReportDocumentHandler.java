package uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.respondentonesolicitor;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadType;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documentbuilder.DocumentTypeBuilder;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.RespondentSolicitorOneDocumentHandler;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.retriever.UploadEvidenceExpertRetriever;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceExpert;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;

import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_ONE_EXPERT_REPORT;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_TWO_EXPERT_REPORT;

@Component
@Order(4)
public class RespondentOneExpertReportDocumentHandler extends
    RespondentSolicitorOneDocumentHandler<UploadEvidenceExpert> {

    public RespondentOneExpertReportDocumentHandler(DocumentTypeBuilder<UploadEvidenceExpert> documentTypeBuilder,
                                                    UploadEvidenceExpertRetriever documentTypeRetriever) {
        super(RESPONDENT_ONE_EXPERT_REPORT, RESPONDENT_TWO_EXPERT_REPORT, EvidenceUploadType.EXPERT_REPORT, documentTypeBuilder,
            documentTypeRetriever);
    }

    @Override
    protected List<Element<UploadEvidenceExpert>> getDocumentList(CaseData caseData) {
        return caseData.getDocumentExpertReportRes();
    }

    @Override
    protected void addDocumentsToCopyToCaseData(CaseData.CaseDataBuilder<?, ?> builder, List<Element<UploadEvidenceExpert>> evidenceDocsToAdd) {
        builder.documentExpertReportRes2(evidenceDocsToAdd);
    }

    @Override
    protected List<Element<UploadEvidenceExpert>> getCorrespondingLegalRep2DocumentList(CaseData caseData) {
        return caseData.getDocumentExpertReportRes2();
    }

    @Override
    protected void renameDocuments(List<Element<UploadEvidenceExpert>> documentUploads) {
        renameUploadReportExpert(documentUploads, evidenceUploadType.getDocumentTypeDisplayName(), true);
    }

}
