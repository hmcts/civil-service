package uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.respondenttwosolicitor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadType;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.RespondentSolicitorTwoDocumentHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceDocumentType;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.time.LocalDateTime;
import java.util.List;

import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_TWO_DISCLOSURE_LIST;

@Component
@Slf4j
public class RespondentTwoDisclosureListDocumentHandler extends
    RespondentSolicitorTwoDocumentHandler<UploadEvidenceDocumentType> {

    public RespondentTwoDisclosureListDocumentHandler() {
        super(RESPONDENT_TWO_DISCLOSURE_LIST, EvidenceUploadType.DISCLOSURE_LIST);
    }

    @Override
    protected List<Element<UploadEvidenceDocumentType>> getDocumentList(CaseData caseData) {
        return caseData.getDocumentDisclosureList();
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

}
