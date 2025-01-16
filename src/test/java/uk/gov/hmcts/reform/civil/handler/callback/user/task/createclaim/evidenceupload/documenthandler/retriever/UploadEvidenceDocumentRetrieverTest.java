package uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.retriever;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.retriever.UploadEvidenceDocumentRetriever;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceDocumentType;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class UploadEvidenceDocumentRetrieverTest {

    @InjectMocks
    private UploadEvidenceDocumentRetriever uploadEvidenceDocumentRetriever;

    @Test
    void shouldReturnDocument() {
        Document document = Document.builder().documentFileName("OriginalName.pdf").build();
        UploadEvidenceDocumentType uploadEvidenceDocumentType = UploadEvidenceDocumentType.builder()
                .documentUpload(document)
                .build();
        Element<UploadEvidenceDocumentType> element = Element.<UploadEvidenceDocumentType>builder()
                .value(uploadEvidenceDocumentType)
                .build();

        Document result = uploadEvidenceDocumentRetriever.getDocument(element);

        assertEquals(document, result);
    }

    @Test
    void shouldReturnDocumentDateTime() {
        LocalDateTime dateTime = LocalDateTime.of(2022, 2, 10, 10, 0);
        UploadEvidenceDocumentType uploadEvidenceDocumentType = UploadEvidenceDocumentType.builder()
                .createdDatetime(dateTime)
                .build();
        Element<UploadEvidenceDocumentType> element = Element.<UploadEvidenceDocumentType>builder()
                .value(uploadEvidenceDocumentType)
                .build();

        LocalDateTime result = uploadEvidenceDocumentRetriever.getDocumentDateTime(element);

        assertEquals(dateTime, result);
    }
}