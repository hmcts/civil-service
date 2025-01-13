package uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.retriever;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.retriever.UploadEvidenceExpertRetriever;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceExpert;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class UploadEvidenceExpertRetrieverTest {

    private UploadEvidenceExpertRetriever uploadEvidenceExpertRetriever;

    @BeforeEach
    void setUp() {
        uploadEvidenceExpertRetriever = new UploadEvidenceExpertRetriever();
    }

    @Test
    void shouldReturnDocument() {
        Document document = Document.builder().documentFileName("OriginalName.pdf").build();
        UploadEvidenceExpert uploadEvidenceExpert = UploadEvidenceExpert.builder()
                .expertDocument(document)
                .build();
        Element<UploadEvidenceExpert> element = Element.<UploadEvidenceExpert>builder()
                .value(uploadEvidenceExpert)
                .build();

        Document result = uploadEvidenceExpertRetriever.getDocument(element);

        assertEquals(document, result);
    }

    @Test
    void shouldReturnDocumentDateTime() {
        LocalDateTime dateTime = LocalDateTime.of(2022, 2, 10, 10, 0);
        UploadEvidenceExpert uploadEvidenceExpert = UploadEvidenceExpert.builder()
                .createdDatetime(dateTime)
                .build();
        Element<UploadEvidenceExpert> element = Element.<UploadEvidenceExpert>builder()
                .value(uploadEvidenceExpert)
                .build();

        LocalDateTime result = uploadEvidenceExpertRetriever.getDocumentDateTime(element);

        assertEquals(dateTime, result);
    }
}