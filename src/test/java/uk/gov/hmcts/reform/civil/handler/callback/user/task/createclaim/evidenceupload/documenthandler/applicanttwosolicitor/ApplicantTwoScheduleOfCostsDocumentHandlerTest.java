package uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.applicanttwosolicitor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.applicanttwosolicitor.ApplicantTwoScheduleOfCostsDocumentHandler;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.retriever.UploadEvidenceDocumentRetriever;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceDocumentType;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ApplicantTwoScheduleOfCostsDocumentHandlerTest {

    @Mock
    private UploadEvidenceDocumentRetriever uploadDocumentRetriever;

    @InjectMocks
    private ApplicantTwoScheduleOfCostsDocumentHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ApplicantTwoScheduleOfCostsDocumentHandler(uploadDocumentRetriever);
    }

    @Test
    void shouldHandleDocumentsWithoutRenaming() {
        Document document = Document.builder().documentFileName("OriginalName.pdf").build();
        when(uploadDocumentRetriever.getDocument(any())).thenReturn(document);

        LocalDateTime mockDateTime = LocalDateTime.of(2022, 2, 10, 10, 0);
        when(uploadDocumentRetriever.getDocumentDateTime(any())).thenReturn(mockDateTime);

        CaseData caseData = CaseData.builder()
                .documentCostsApp2(List.of(
                        Element.<UploadEvidenceDocumentType>builder()
                                .value(UploadEvidenceDocumentType.builder()
                                        .documentUpload(document)
                                        .build())
                                .build()))
                .build();

        handler.handleDocuments(caseData, "Applicant", new StringBuilder());

        assertEquals("OriginalName.pdf", caseData.getDocumentCostsApp2().get(0).getValue().getDocumentUpload().getDocumentFileName());
    }
}