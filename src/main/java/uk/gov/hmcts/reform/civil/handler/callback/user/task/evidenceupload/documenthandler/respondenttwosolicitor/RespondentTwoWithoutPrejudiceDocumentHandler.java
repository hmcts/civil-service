package uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.respondenttwosolicitor;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadType;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.RespondentTwoSolicitorDocumentHandler;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.retriever.UploadDocumentWithDescriptionRetriever;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.documents.DocumentWithDescription;

import java.util.List;

import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.WITHOUT_PREJUDICE;

@Component
public class RespondentTwoWithoutPrejudiceDocumentHandler extends RespondentTwoSolicitorDocumentHandler<DocumentWithDescription> {

    public RespondentTwoWithoutPrejudiceDocumentHandler(UploadDocumentWithDescriptionRetriever uploadDocumentRetriever) {
        super(WITHOUT_PREJUDICE, EvidenceUploadType.WITHOUT_PREJUDICE, uploadDocumentRetriever);
    }

    @Override
    protected List<Element<DocumentWithDescription>> getDocumentList(CaseData caseData) {
        return caseData.getDocumentPart36RejectionRes2();
    }

    @Override
    public void handleDocuments(CaseData caseData, String litigantType, StringBuilder notificationStringBuilder) {
        if (getDocumentList(caseData) == null || getDocumentList(caseData).isEmpty()) {
            return;
        }
        getDocumentList(caseData).forEach(element -> {
            if (element.getValue() != null && element.getValue().getDocument() != null) {
                element.getValue().getDocument().setCategoryID(documentCategory.getCategoryId());
            }
        });
    }

    @Override
    @SuppressWarnings("java:S1186")
    public void addUploadDocList(CaseData caseData) {
    }
}

