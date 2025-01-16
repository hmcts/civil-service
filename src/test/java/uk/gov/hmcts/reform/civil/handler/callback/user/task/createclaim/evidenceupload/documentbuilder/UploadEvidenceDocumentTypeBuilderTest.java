package uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documentbuilder;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documentbuilder.UploadEvidenceDocumentTypeBuilder;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceDocumentType;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class UploadEvidenceDocumentTypeBuilderTest {

    @InjectMocks
    private UploadEvidenceDocumentTypeBuilder uploadEvidenceDocumentTypeBuilder;

    @Test
    void shouldBuildElementTypeWithDocumentCopy() {
        Document originalDocument = Document.builder()
                .categoryID("originalCategory")
                .documentBinaryUrl("originalBinaryUrl")
                .documentFileName("originalFileName.pdf")
                .documentHash("originalHash")
                .documentUrl("originalUrl")
                .build();

        UploadEvidenceDocumentType originalType = UploadEvidenceDocumentType.builder()
                .witnessOptionName("Witness Name")
                .documentIssuedDate(LocalDate.parse("2022-02-10"))
                .typeOfDocument("Type")
                .createdDatetime(LocalDateTime.of(2022, 2, 10, 10, 0))
                .documentUpload(originalDocument)
                .build();

        UploadEvidenceDocumentType newType = uploadEvidenceDocumentTypeBuilder.buildElementTypeWithDocumentCopy(originalType, "newCategory");

        assertEquals("newCategory", newType.getDocumentUpload().getCategoryID());
        assertEquals("originalBinaryUrl", newType.getDocumentUpload().getDocumentBinaryUrl());
        assertEquals("originalFileName.pdf", newType.getDocumentUpload().getDocumentFileName());
        assertEquals("originalHash", newType.getDocumentUpload().getDocumentHash());
        assertEquals("originalUrl", newType.getDocumentUpload().getDocumentUrl());
        assertEquals("Witness Name", newType.getWitnessOptionName());
        assertEquals(LocalDate.parse("2022-02-10"), newType.getDocumentIssuedDate());
        assertEquals("Type", newType.getTypeOfDocument());
        assertEquals(LocalDateTime.of(2022, 2, 10, 10, 0), newType.getCreatedDatetime());
    }
}