package uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.applicantonesolicitor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documentbuilder.DocumentTypeBuilder;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.applicantonesolcitor.ApplicantOneExpertJointStatmementDocumentHandler;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.retriever.UploadEvidenceExpertRetriever;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceExpert;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ApplicantOneExpertJointStatmementDocumentHandlerTest {

    @Mock
    private DocumentTypeBuilder<UploadEvidenceExpert> documentTypeBuilder;

    @Mock
    private UploadEvidenceExpertRetriever uploadDocumentRetriever;

    @InjectMocks
    private ApplicantOneExpertJointStatmementDocumentHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ApplicantOneExpertJointStatmementDocumentHandler(documentTypeBuilder, uploadDocumentRetriever);
    }

    @Test
    void shouldCopyExpertJointStatementDocumentsToLegalRep2() {
        Document document = Document.builder().documentFileName("OriginalExpertJointStatement.pdf").build();
        UploadEvidenceExpert uploadEvidenceExpert = UploadEvidenceExpert.builder()
                .expertOptionUploadDate(LocalDate.of(2022, 2, 10))
                .expertOptionName("ExpertJointStatementBundle")
                .expertDocument(document)
                .build();

        CaseData caseData = CaseData.builder()
                .documentJointStatement(List.of(
                        Element.<UploadEvidenceExpert>builder()
                                .value(uploadEvidenceExpert)
                                .build()))
                .build();

        CaseData caseDataBefore = CaseData.builder()
                .documentJointStatement(List.of())
                .build();
        CaseData.CaseDataBuilder<?, ?> builder = CaseData.builder();

        handler.copyLegalRep1ChangesToLegalRep2(caseData, caseDataBefore, builder);

        assertEquals(1, builder.build().getDocumentJointStatementApp2().size());
    }

    @Test
    void shouldRenameDocumentsWithDateAndBundleName() {
        Document document = Document.builder().documentFileName("OriginalName.pdf").build();
        when(uploadDocumentRetriever.getDocument(any())).thenReturn(document);

        UploadEvidenceExpert uploadEvidenceExpert = UploadEvidenceExpert.builder()
                .expertOptionName("test")
                .expertOptionExpertises("expertises")
                .expertOptionUploadDate(LocalDate.of(2022, 2, 10))
                .expertDocument(document)
                .build();

        System.out.println("UploadEvidenceExpert: " + uploadEvidenceExpert);

        CaseData caseData = CaseData.builder()
                .documentJointStatement(List.of(
                        Element.<UploadEvidenceExpert>builder()
                                .value(uploadEvidenceExpert)
                                .build()))
                .build();

        LocalDateTime fixedDateTime = LocalDateTime.of(2022, 2, 10, 10, 0);
        when(uploadDocumentRetriever.getDocumentDateTime(any())).thenReturn(fixedDateTime);

        StringBuilder notificationBuilder = new StringBuilder();
        handler.handleDocuments(caseData, "Applicant", notificationBuilder);

        String expectedDocumentName = "Joint report test expertises 10-02-2022.pdf";
        assertEquals(expectedDocumentName, caseData.getDocumentJointStatement().get(0).getValue().getExpertDocument().getDocumentFileName());
    }
}
