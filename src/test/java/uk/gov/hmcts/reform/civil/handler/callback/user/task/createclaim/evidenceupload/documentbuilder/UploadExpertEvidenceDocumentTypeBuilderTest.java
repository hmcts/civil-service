package uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documentbuilder;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documentbuilder.UploadExpertEvidenceDocumentTypeBuilder;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceExpert;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class UploadExpertEvidenceDocumentTypeBuilderTest {

    @InjectMocks
    private UploadExpertEvidenceDocumentTypeBuilder uploadExpertEvidenceDocumentTypeBuilder;

    @Test
    void shouldBuildElementTypeWithDocumentCopy() {
        Document originalDocument = Document.builder()
                .categoryID("originalCategory")
                .documentBinaryUrl("originalBinaryUrl")
                .documentFileName("originalFileName.pdf")
                .documentHash("originalHash")
                .documentUrl("originalUrl")
                .build();

        UploadEvidenceExpert originalExpert = UploadEvidenceExpert.builder()
                .expertOptionName("Expert Name")
                .expertOptionExpertise("Expertise")
                .expertOptionExpertises("Expertises")
                .expertOptionOtherParty("Other Party")
                .expertDocumentQuestion("Question")
                .expertDocumentAnswer("Answer")
                .expertOptionUploadDate(LocalDate.parse("2022-02-10"))
                .expertDocument(originalDocument)
                .build();

        UploadEvidenceExpert newExpert = uploadExpertEvidenceDocumentTypeBuilder.buildElementTypeWithDocumentCopy(originalExpert, "newCategory");

        assertEquals("newCategory", newExpert.getExpertDocument().getCategoryID());
        assertEquals("originalBinaryUrl", newExpert.getExpertDocument().getDocumentBinaryUrl());
        assertEquals("originalFileName.pdf", newExpert.getExpertDocument().getDocumentFileName());
        assertEquals("originalHash", newExpert.getExpertDocument().getDocumentHash());
        assertEquals("originalUrl", newExpert.getExpertDocument().getDocumentUrl());
        assertEquals("Expert Name", newExpert.getExpertOptionName());
        assertEquals("Expertise", newExpert.getExpertOptionExpertise());
        assertEquals("Expertises", newExpert.getExpertOptionExpertises());
        assertEquals("Other Party", newExpert.getExpertOptionOtherParty());
        assertEquals("Question", newExpert.getExpertDocumentQuestion());
        assertEquals("Answer", newExpert.getExpertDocumentAnswer());
        assertEquals(LocalDate.parse("2022-02-10"), newExpert.getExpertOptionUploadDate());
    }
}