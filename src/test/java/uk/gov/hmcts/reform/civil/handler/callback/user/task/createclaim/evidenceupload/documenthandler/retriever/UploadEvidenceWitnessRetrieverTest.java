package uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.retriever;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.retriever.UploadEvidenceWitnessRetriever;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceWitness;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class UploadEvidenceWitnessRetrieverTest {

    @InjectMocks
    private UploadEvidenceWitnessRetriever uploadEvidenceWitnessRetriever;

    @Test
    void shouldReturnDocument() {
        Document document = Document.builder().documentFileName("OriginalName.pdf").build();
        UploadEvidenceWitness uploadEvidenceWitness = UploadEvidenceWitness.builder()
                .witnessOptionDocument(document)
                .build();
        Element<UploadEvidenceWitness> element = Element.<UploadEvidenceWitness>builder()
                .value(uploadEvidenceWitness)
                .build();

        Document result = uploadEvidenceWitnessRetriever.getDocument(element);

        assertEquals(document, result);
    }

    @Test
    void shouldReturnDocumentDateTime() {
        LocalDateTime dateTime = LocalDateTime.of(2022, 2, 10, 10, 0);
        UploadEvidenceWitness uploadEvidenceWitness = UploadEvidenceWitness.builder()
                .createdDatetime(dateTime)
                .build();
        Element<UploadEvidenceWitness> element = Element.<UploadEvidenceWitness>builder()
                .value(uploadEvidenceWitness)
                .build();

        LocalDateTime result = uploadEvidenceWitnessRetriever.getDocumentDateTime(element);

        assertEquals(dateTime, result);
    }
}