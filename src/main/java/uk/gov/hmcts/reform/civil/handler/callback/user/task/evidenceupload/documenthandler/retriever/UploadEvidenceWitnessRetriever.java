package uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.retriever;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceWitness;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.time.LocalDateTime;

@Component
public class UploadEvidenceWitnessRetriever implements UploadDocumentRetriever<UploadEvidenceWitness> {

    public Document getDocument(Element<UploadEvidenceWitness> element) {
        return element.getValue().getWitnessOptionDocument();
    }

    public LocalDateTime getDocumentDateTime(Element<UploadEvidenceWitness> element) {
        return element.getValue().getCreatedDatetime();
    }
}
