package uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documentbuilder;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documentbuilder.UploadEvidenceWitnessDocumentTypeBuilder;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceWitness;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class UploadEvidenceWitnessDocumentTypeBuilderTest {

    @InjectMocks
    private UploadEvidenceWitnessDocumentTypeBuilder uploadEvidenceWitnessDocumentTypeBuilder;

    @Test
    void shouldBuildElementTypeWithDocumentCopy() {
        Document originalDocument = Document.builder()
                .categoryID("originalCategory")
                .documentBinaryUrl("originalBinaryUrl")
                .documentFileName("originalFileName.pdf")
                .documentHash("originalHash")
                .documentUrl("originalUrl")
                .build();

        UploadEvidenceWitness originalWitness = UploadEvidenceWitness.builder()
                .witnessOptionName("Witness Name")
                .witnessOptionUploadDate(LocalDate.parse("2022-02-10"))
                .createdDatetime(LocalDateTime.of(2022, 2, 10, 10, 0))
                .witnessOptionDocument(originalDocument)
                .build();

        UploadEvidenceWitness newWitness = uploadEvidenceWitnessDocumentTypeBuilder.buildElementTypeWithDocumentCopy(originalWitness, "newCategory");

        assertEquals("newCategory", newWitness.getWitnessOptionDocument().getCategoryID());
        assertEquals("originalBinaryUrl", newWitness.getWitnessOptionDocument().getDocumentBinaryUrl());
        assertEquals("originalFileName.pdf", newWitness.getWitnessOptionDocument().getDocumentFileName());
        assertEquals("originalHash", newWitness.getWitnessOptionDocument().getDocumentHash());
        assertEquals("originalUrl", newWitness.getWitnessOptionDocument().getDocumentUrl());
        assertEquals("Witness Name", newWitness.getWitnessOptionName());
        assertEquals(LocalDate.parse("2022-02-10"), newWitness.getWitnessOptionUploadDate());
        assertEquals(LocalDateTime.of(2022, 2, 10, 10, 0), newWitness.getCreatedDatetime());
    }
}