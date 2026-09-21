package uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.applicantonesolicitor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.BaseDocumentHandlerTest;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.applicantonesolcitor.ApplicantOneDisclosureDocumentHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceDocumentType;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.BaseDocumentHandlerTest.DomainConstants.ORIGINAL_FILE_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.BaseDocumentHandlerTest.DomainConstants.RESPONDENT;

class ApplicantOneDisclosureDocumentHandlerTest extends BaseDocumentHandlerTest {

    @InjectMocks
    private ApplicantOneDisclosureDocumentHandler handler;

    @BeforeEach
    void setup() {
        setUpDocumentForDisclosure();
    }

    @Test
    void shouldCopyDisclosureDocumentsToLegalRep2() {
        handler.copyLegalRep1ChangesToLegalRep2(caseData, caseDataBefore);

        assertEquals(2, caseData.getDocumentForDisclosureApp2().size());
    }

    @Test
    void shouldNotRenameDocuments() {
        UploadEvidenceDocumentType uploadEvidenceDocumentType = new UploadEvidenceDocumentType();
        uploadEvidenceDocumentType.setDocumentIssuedDate(LocalDate.of(2022, Month.FEBRUARY, 10));
        uploadEvidenceDocumentType.setTypeOfDocument("typeOfDocument");
        uploadEvidenceDocumentType.setDocumentUpload(document);
        Element<UploadEvidenceDocumentType> element = new Element<>();
        element.setValue(uploadEvidenceDocumentType);
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setDocumentForDisclosureRes(List.of(element));

        StringBuilder notificationBuilder = new StringBuilder();
        handler.handleDocuments(caseData, RESPONDENT, notificationBuilder);

        assertEquals(ORIGINAL_FILE_NAME, caseData.getDocumentForDisclosureRes().get(0).getValue().getDocumentUpload().getDocumentFileName());
    }

    @Test
    void shouldSkipDocumentWhenUnderlyingDocumentIsMissing() {
        UploadEvidenceDocumentType uploadEvidenceDocumentType = new UploadEvidenceDocumentType();
        uploadEvidenceDocumentType.setDocumentIssuedDate(LocalDate.of(2022, 2, 10));
        uploadEvidenceDocumentType.setTypeOfDocument("typeOfDocument");
        Element<UploadEvidenceDocumentType> element = new Element<>();
        element.setValue(uploadEvidenceDocumentType);
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setDocumentForDisclosure(List.of(element));
        when(uploadEvidenceDocumentRetriever.getDocument(any())).thenReturn(null);

        StringBuilder notificationBuilder = new StringBuilder();

        assertDoesNotThrow(() -> handler.handleDocuments(caseData, RESPONDENT, notificationBuilder));
        assertEquals("", notificationBuilder.toString());
    }
}
