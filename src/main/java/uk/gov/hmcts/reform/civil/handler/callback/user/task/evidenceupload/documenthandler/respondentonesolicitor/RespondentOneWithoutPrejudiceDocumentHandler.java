package uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.respondentonesolicitor;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadType;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documentbuilder.DocumentTypeBuilder;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentWithDescriptionCategoryUtils;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.RespondentSolicitorOneDocumentHandler;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.retriever.UploadDocumentWithDescriptionRetriever;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.documents.DocumentWithDescription;

import java.util.List;

import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.WITHOUT_PREJUDICE;

@Component
public class RespondentOneWithoutPrejudiceDocumentHandler extends RespondentSolicitorOneDocumentHandler<DocumentWithDescription> {

    public RespondentOneWithoutPrejudiceDocumentHandler(DocumentTypeBuilder<DocumentWithDescription> documentTypeBuilder,
                                                        UploadDocumentWithDescriptionRetriever uploadDocumentRetriever) {
        super(WITHOUT_PREJUDICE, WITHOUT_PREJUDICE, EvidenceUploadType.WITHOUT_PREJUDICE,
            documentTypeBuilder, uploadDocumentRetriever);
    }

    @Override
    protected List<Element<DocumentWithDescription>> getDocumentList(CaseData caseData) {
        return caseData.getDocumentPart36RejectionRes();
    }

    @Override
    public void handleDocuments(CaseData caseData, String litigantType, StringBuilder notificationStringBuilder) {
        DocumentWithDescriptionCategoryUtils.applyCategoryId(getDocumentList(caseData), documentCategory);
    }

    @Override
    protected boolean shouldPopulatePostBundleUploadList() {
        return false;
    }

    @Override
    protected void addDocumentsToCopyToCaseData(CaseData caseData, List<Element<DocumentWithDescription>> evidenceDocsToAdd) {
        caseData.setDocumentPart36RejectionRes2(evidenceDocsToAdd);
    }

    @Override
    protected List<Element<DocumentWithDescription>> getCorrespondingLegalRep2DocumentList(CaseData caseData) {
        return caseData.getDocumentPart36RejectionRes2();
    }
}

