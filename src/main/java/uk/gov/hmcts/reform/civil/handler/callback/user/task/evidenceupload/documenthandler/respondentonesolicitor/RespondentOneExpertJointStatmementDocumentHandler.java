package uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.respondentonesolicitor;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.RespondentSolicitorOneDocumentHandler;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documentbuilder.DocumentTypeBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceExpert;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.time.LocalDateTime;
import java.util.List;

import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.EvidenceUploadConstants.EXPERT_JOINT_STATEMENT_TEXT;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.EvidenceUploadConstants.JOINT_REPORT;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_ONE_EXPERT_JOINT_STATEMENT;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_TWO_EXPERT_JOINT_STATEMENT;

@Component
public class RespondentOneExpertJointStatmementDocumentHandler extends
    RespondentSolicitorOneDocumentHandler<UploadEvidenceExpert> {

    public RespondentOneExpertJointStatmementDocumentHandler(DocumentTypeBuilder<UploadEvidenceExpert> documentTypeBuilder) {
        super(RESPONDENT_ONE_EXPERT_JOINT_STATEMENT, RESPONDENT_TWO_EXPERT_JOINT_STATEMENT,
            EXPERT_JOINT_STATEMENT_TEXT, documentTypeBuilder);
    }

    @Override
    protected List<Element<UploadEvidenceExpert>> getDocumentList(CaseData caseData) {
        return caseData.getDocumentJointStatementRes();
    }

    @Override
    protected Document getDocument(Element<UploadEvidenceExpert> element) {
        return element.getValue().getExpertDocument();
    }

    @Override
    protected LocalDateTime getDocumentDateTime(Element<UploadEvidenceExpert> element) {
        return element.getValue().getCreatedDatetime();
    }

    @Override
    protected void renameDocuments(List<Element<UploadEvidenceExpert>> documentUploads) {
        renameUploadReportExpert(documentUploads, JOINT_REPORT, true);
    }

    @Override
    protected void addDocumentsToCopyToCaseData(CaseData.CaseDataBuilder<?, ?> builder, List<Element<UploadEvidenceExpert>> evidenceDocsToAdd) {
        builder.documentJointStatementRes2(evidenceDocsToAdd);

    }

    @Override
    protected List<Element<UploadEvidenceExpert>> getCorrepsondingLegalRep2DocumentList(CaseData caseData) {
        return caseData.getDocumentJointStatementRes2();
    }
}
