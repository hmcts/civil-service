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
class UploadExpertEvidenceDocumentTypeBuilderTest {

    @InjectMocks
    private UploadExpertEvidenceDocumentTypeBuilder uploadExpertEvidenceDocumentTypeBuilder;

    @Test
    void shouldBuildElementTypeWithDocumentCopy() {
        Document originalDocument = new Document();
        originalDocument.setCategoryID("originalCategory")
                .setDocumentBinaryUrl("originalBinaryUrl")
                .setDocumentFileName("originalFileName.pdf")
                .setDocumentHash("originalHash")
                .setDocumentUrl("originalUrl");

        UploadEvidenceExpert originalExpert = new UploadEvidenceExpert();
        originalExpert.setExpertOptionName("Expert Name");
        originalExpert.setExpertOptionExpertise("Expertise");
        originalExpert.setExpertOptionExpertises("Expertises");
        originalExpert.setExpertOptionOtherParty("Other Party");
        originalExpert.setExpertDocumentQuestion("Question");
        originalExpert.setExpertDocumentAnswer("Answer");
        originalExpert.setExpertOptionUploadDate(LocalDate.parse("2022-02-10"));
        originalExpert.setExpertDocument(originalDocument);

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
