package uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.retriever;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.retriever.UploadEvidenceExpertRetriever;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceExpert;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class UploadEvidenceExpertRetrieverTest {

    @InjectMocks
    private UploadEvidenceExpertRetriever uploadEvidenceExpertRetriever;

    @Test
    void shouldReturnDocument() {
        Document document = new Document();
        document.setDocumentFileName("OriginalName.pdf");
        UploadEvidenceExpert uploadEvidenceExpert = new UploadEvidenceExpert();
        uploadEvidenceExpert.setExpertDocument(document);
        Element<UploadEvidenceExpert> element = new Element<>();
        element.setValue(uploadEvidenceExpert);

        Document result = uploadEvidenceExpertRetriever.getDocument(element);

        assertEquals(document, result);
    }

    @Test
    void shouldReturnDocumentDateTime() {
        LocalDateTime dateTime = LocalDateTime.of(2022, 2, 10, 10, 0);
        UploadEvidenceExpert uploadEvidenceExpert = new UploadEvidenceExpert();
        uploadEvidenceExpert.setCreatedDatetime(dateTime);
        Element<UploadEvidenceExpert> element = new Element<>();
        element.setValue(uploadEvidenceExpert);

        LocalDateTime result = uploadEvidenceExpertRetriever.getDocumentDateTime(element);

        assertEquals(dateTime, result);
    }
}
